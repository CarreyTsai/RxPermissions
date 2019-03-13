package com.carrey.permissions

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import com.carrey.permissions.snackbar.CustomSnackBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc:
 */


class RxPermissions private constructor(private val activity: FragmentActivity) {
    companion object {
        internal var reportLog: IPermissionLog = DefaultPermissionLog

        const val PERMISSIONS_TAG = "Permissions"

        @JvmStatic
        fun newInstance(activity: FragmentActivity): RxPermissions {
            checkMainThread()
            return RxPermissions(activity)
        }

        @JvmStatic
        fun newInstance(activity: Activity) =
            if (activity is FragmentActivity) RxPermissions(activity)
            else throw IllegalArgumentException("activity must be a FragmentActivity")

        private fun checkMainThread() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                val exception = IllegalStateException("request permissions must use in main thread")
                reportLog.e(exception)
                throw exception
            }
        }

        @JvmStatic
        fun checkEachPermissions(activity: Activity, vararg permissions: String) = permissions.map { permission ->
            ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED
        }.toBooleanArray()

        @JvmStatic
        fun checkAllPermissions(activity: Activity, vararg permissions: String) = permissions.all { permission ->
            ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED
        }
    }

    private val permissionsFragment: PermissionsFragment? = try {
        if (activity.isDestroyed) {
            throw IllegalStateException("activity is destroyed")
        } else {
            val fragmentManager = activity.supportFragmentManager
            findPermissionsFragment(fragmentManager) ?: createPermissionsFragment(fragmentManager)
        }
    } catch (e: Throwable) {
        reportLog.boom(
            PERMISSIONS_TAG, IllegalStateException(
                "Permissions.getPermissionsFragment failed because ${e.message}"
            )
        )
        null
    }

    private var onIntroduceClickedListener: OnIntroduceClickedListener? = null
    private var onExplainClickedListener: OnExplainClickedListener? = null

    private fun findPermissionsFragment(fragmentManager: FragmentManager) =
        fragmentManager.findFragmentByTag(PERMISSIONS_TAG) as? PermissionsFragment

    private fun createPermissionsFragment(fragmentManager: FragmentManager) =
        PermissionsFragment().apply {
            fragmentManager.beginTransaction().add(this, PERMISSIONS_TAG).commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }

    fun onIntroduceClicked(onIntroduceClickedListener: OnIntroduceClickedListener?) = apply {
        this.onIntroduceClickedListener = onIntroduceClickedListener
    }

    fun onExplainClicked(onExplainClickedListener: OnExplainClickedListener?) = apply {
        this.onExplainClickedListener = onExplainClickedListener
    }

    fun request(
        introduce: String?,
        explain: String?,
        vararg permissions: String,
        callback: PermissionsCallback
    ): Disposable =
        request(introduce, explain, *permissions).subscribe { result ->
            callback.onRequestPermissionsResult(result)
        }

    fun request(
        introduce: String?,
        explain: String?,
        vararg permissions: String
    ): Observable<RequestPermissionsResult> = try {
        if (permissions.isEmpty()) {
            throw IllegalArgumentException(" Permissions.request requires at least one input permission")
        }
        if (activity.isDestroyed) {
            throw IllegalStateException(" Permissions.request failed because activity is destroyed")
        }
        if (permissionsFragment == null) {
            throw NullPointerException(" Permissions.request failed because permissionFragment is null")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkMainThread()
            requestImplementation(introduce, explain, *permissions)
        } else {
            Observable.just(RequestPermissionsResult(emptyList(), true))
        }
    } catch (e: Exception) {
        reportLog.boom(PERMISSIONS_TAG, e)
        Observable.just(RequestPermissionsResult(emptyList(), false))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestImplementation(
        introduce: String?,
        explain: String?,
        vararg permissions: String
    ): Observable<RequestPermissionsResult> {
        var isIntroduceShown = false
        var isExplainShown = false
        val unrequestedPermissions = ArrayList<String>()
        val observables = permissions.map { permission ->
            reportLog.d("Requesting permission $permission")
            when {
                isGranted(permission) -> Observable.just(
                    PermissionResult(permission, true)
                )
                isRevoked(permission) -> Observable.just(
                    PermissionResult(permission, false)
                )
                else -> PublishSubject.create<PermissionResult>().apply {
                    permissionsFragment?.setPermission(permission, this)
                    unrequestedPermissions.add(permission)
                }
            }
        }
        if (unrequestedPermissions.isNotEmpty()) {
            val permissionArray = unrequestedPermissions.toTypedArray()
            if (introduce != null && introduce.isNotEmpty() && shouldShowIntroduce(unrequestedPermissions)) {
                showIntroduce(introduce, permissionArray)
                isIntroduceShown = true
            } else {
                permissionsFragment?.requestPermissions(permissionArray)
            }
        }
        return Observable.concat(Observable.fromIterable(observables))
            .buffer(permissions.size)
            .doOnNext { results ->
                if (explain != null && explain.isNotEmpty() && shouldShowExplain(results)) {
                    showExplain(explain)
                    isExplainShown = true
                }
                results.forEach { result ->

                    reportLog.report(
                        "permissions.permission.result:" + mapOf(
                            "name" to result.name,
                            "granted" to result.granted,
                            "shouldShowRationale" to result.shouldShowRationale,
                            "neverAskAgain" to (!result.granted && !result.shouldShowRationale),
                            "isIntroduceShown" to isIntroduceShown,
                            "isExplainShown" to isExplainShown
                        )
                    )
                }
            }.map { results -> RequestPermissionsResult(results) }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showIntroduce(introduce: String, permissionArray: Array<String>) {
        var isClick = false
        reportLog.report("permissions_introduce_show")
        CustomSnackBar.make(activity, introduce, Snackbar.LENGTH_INDEFINITE)
            .setAction("ok") {
                isClick = true
                reportLog.report("permissions_introduce_click")
                onIntroduceClickedListener?.onClickIntroduce()
                permissionsFragment?.requestPermissions(permissionArray)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    super.onDismissed(snackbar, event)
                    if (!isClick) {
                        reportLog.report("permissions_introduce_cancel")
                    }
                }
            }).show()
    }

    private fun showExplain(explain: String) {
        var isClick = false
        reportLog.report("permissions_explain_show")
        CustomSnackBar.make(activity, explain, LENGTH_LONG)
            .setAction("设置") {
                isClick = true
                reportLog.report("permissions_explain_click")
                onExplainClickedListener?.onClickExplain()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:" + activity.packageName)
                }
                activity.startActivity(intent)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    super.onDismissed(snackbar, event)
                    if (!isClick) {
                        reportLog.report("permissions_explain_cancel")
                    }
                }
            }).show()
    }

    //存在没有授予又允许再次询问的权限
    private fun shouldShowIntroduce(permissions: List<String>) =
        permissions.any { permission -> shouldShowRationale(permission) }

    //存在没有授予又不许再次询问的权限
    private fun shouldShowExplain(results: List<PermissionResult>) =
        results.any { result -> !result.granted && !result.preShouldShowRationale && !result.postShouldShowRationale }

    private fun isGranted(permission: String) =
        ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED

    private fun shouldShowRationale(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    @TargetApi(Build.VERSION_CODES.M)
    private fun isRevoked(permission: String) = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            activity.packageManager.isPermissionRevokedByPolicy(permission, activity.packageName)
}

interface PermissionsCallback {
    fun onRequestPermissionsResult(result: RequestPermissionsResult)
}

interface OnIntroduceClickedListener {
    fun onClickIntroduce()
}

interface OnExplainClickedListener {
    fun onClickExplain()
}