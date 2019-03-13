package com.carrey.permissions

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import com.carrey.permissions.RxPermissions.Companion.reportLog
import io.reactivex.subjects.PublishSubject

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc:
 *
 *  权限申请的fragment。没有页面。
 *
 *
 *
 */
class PermissionsFragment : Fragment() {

    private val subjects = HashMap<String, PublishSubject<PermissionResult>>()
    //获取完之后 置空
    private var tempShouldShowRationales: BooleanArray? = null
        get() {
            val temp = field
            if (field != null) {
                field = null
            }
            return temp
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 110
    }

    //发起权限申请请求
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<String>) {
        reportLog.d("requestPermissionsFromFragment: $permissions")
        reportLog.report("permissions_request_send")
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }


    //fragment中申请权限的回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            //
            val grantedArray = getGrantedArray(permissions)
            val postShouldShowRationales = getShouldShowRationaleArray(permissions)
            val preShouldShowRationales = tempShouldShowRationales ?: BooleanArray(permissions.size) { false }

            realRequestPermissionsResult(permissions, grantedArray, postShouldShowRationales, preShouldShowRationales)
        }
    }


    private fun realRequestPermissionsResult(
        permissions: Array<out String>,
        granted: BooleanArray,
        postShouldShowRationales: BooleanArray,
        preShouldShowRationales: BooleanArray
    ) {
        permissions.forEachIndexed { index, permission ->
            reportLog.d("realRequestPermissionsResult: $permission")
            val subject = subjects[permission]
            if (subject == null) {
                reportLog.e("PermissionsFragment.onRequestPermissionsResult 没有找到需要申请的权限.")
            } else {
                subjects.remove(permission)
                subject.onNext(
                    PermissionResult(
                        permission,
                        granted[index],
                        postShouldShowRationales[index],
                        preShouldShowRationales[index]
                    )
                )
                subject.onComplete()
            }
        }
    }


    //判断所有的权限是否需要显示说明
    private fun getShouldShowRationaleArray(permissions: Array<out String>) = permissions.map {
        activity?.let { context ->
            ActivityCompat.shouldShowRequestPermissionRationale(context, it)

        } ?: false
    }.toBooleanArray()

    // 判断权限是否授权
    private fun getGrantedArray(permissions: Array<out String>) = permissions.map {
        activity?.let { context ->
            ActivityCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED

        } ?: false
    }.toBooleanArray()

    //设置需要请求的权限
    internal fun setPermission(permission: String, subject: PublishSubject<PermissionResult>) =
        subjects.put(permission, subject)
}