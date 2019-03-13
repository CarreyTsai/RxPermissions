package com.carrey.permissions

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc:
 */
data class RequestPermissionsResult(
    val permissionResults: List<PermissionResult>,
    val isGranted: Boolean = permissionResults.all { it.granted }
)