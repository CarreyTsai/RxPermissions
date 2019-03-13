package com.carrey.permissions.snackbar

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import com.carrey.permissions.RxPermissions.Companion.reportLog
import java.lang.Exception

/**
 * Created by carrey on 2019/3/14.
 *
 * Desc:
 */
object CustomSnackBar {

    fun show(
        activity: Activity, title: String, action: String?, listener: View.OnClickListener?,
        duration: Int
    ) {
        try {
            val content = activity.window.findViewById<View>(android.R.id.content) ?: return
            val actionColor = Color.parseColor("#2681ff")
            val bg = Color.parseColor("#B2000000")
            val snackbar = Snackbar.make(content, title, duration).setAction(action, listener)
                .setActionTextColor(actionColor)
            snackbar.view.setBackgroundColor(bg)

            val snackBarText = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            snackBarText.maxLines = 3
            try {
                val parent = snackBarText.parent
                val mMaxInlineActionWidth = parent.javaClass.getDeclaredField("mMaxInlineActionWidth")
                mMaxInlineActionWidth.isAccessible = true
                mMaxInlineActionWidth.set(parent, dp2px(activity, 100f))
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

            snackbar.show()
        } catch (e: Throwable) {
           reportLog.boom("CustomSnackBar", Exception(e))
        }

    }

    internal fun make(activity: Activity, title: String, duration: Int): Snackbar {
        val parent = activity.window.findViewById<View>(android.R.id.content)
        return Snackbar.make(parent, title, duration).apply {
            try {
                setActionTextColor(Color.parseColor("#2681ff"))
                view.apply {
                    setBackgroundColor(Color.parseColor("#B2000000"))
                }
                val text = view.findViewById<TextView>(android.support.design.R.id.snackbar_text).apply {
                    maxLines = 3
                }
                val content = text.parent
                content.javaClass.getDeclaredField("mMaxInlineActionWidth").apply {
                    isAccessible = true
                    set(content, dp2px(activity, 100f))
                }
                addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                    }
                })
            } catch (e: Throwable) {
                reportLog.boom("CustomSnackBar", Exception(e))
            }
        }
    }
}

private fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}


