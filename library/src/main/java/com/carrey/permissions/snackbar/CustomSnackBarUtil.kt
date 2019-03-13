package com.carrey.permissions.snackbar

import android.app.Activity
import android.support.design.widget.BaseTransientBottomBar
import android.view.View

object CustomSnackBarUtil {

    @JvmStatic
    fun showShort(
        activity: Activity,
        title: String
    ) {
        show(
            activity,
            title,
            null,
            null,
            BaseTransientBottomBar.LENGTH_SHORT
        )
    }

    @JvmStatic
    fun showShort(
        activity: Activity,
        title: String,
        action: String?,
        listener: View.OnClickListener?
    ) {
        show(
            activity,
            title,
            action,
            listener,
            BaseTransientBottomBar.LENGTH_SHORT
        )
    }

    @JvmStatic
    fun showLong(
        activity: Activity,
        title: String
    ) {
        show(
            activity,
            title,
            null,
            null,
            BaseTransientBottomBar.LENGTH_LONG
        )
    }

    @JvmStatic
    fun showLong(
        activity: Activity,
        title: String,
        action: String?,
        listener: View.OnClickListener?
    ) {
        show(
            activity,
            title,
            action,
            listener,
            BaseTransientBottomBar.LENGTH_LONG
        )
    }

    @JvmStatic
    fun showIndefinite(
        activity: Activity,
        title: String
    ) {
        show(
            activity,
            title,
            null,
            null,
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
    }

    @JvmStatic
    fun showIndefinite(
        activity: Activity,
        title: String,
        action: String?,
        listener: View.OnClickListener?
    ) {
        show(
            activity,
            title,
            action,
            listener,
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
    }

    /**
     * 按照UED MD风格设计，action颜色为主题蓝，snackbar背景为70%黑
     *
     * @param activity
     * @param title
     * @param action
     * @param listener
     * @param duration
     */
    @JvmStatic
    fun show(
        activity: Activity,
        title: String,
        action: String?,
        listener: View.OnClickListener?,
        duration: Int
    ) {
        CustomSnackBar.show(activity, title, action, listener, duration)
    }
}
