package com.carrey.permissions

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc: 权限返回的结果 名称，权限结果，是否需要重试
 */
data class PermissionResult @JvmOverloads internal constructor(
    val name: String,
    val granted: Boolean = false,
    internal val postShouldShowRationale: Boolean = false,
    internal val preShouldShowRationale: Boolean = false
) {

    val shouldShowRationale: Boolean
        get() = postShouldShowRationale


    override fun toString(): String {
        return "PermissionResult(name='$name', granted=$granted, postShouldShowRationale=$postShouldShowRationale, preShouldShowRationale=$preShouldShowRationale)"
    }
}