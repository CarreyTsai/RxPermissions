package com.carrey.permissions

import java.lang.Exception

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc:
 */
interface IPermissionLog {

    fun d(any: Any)
    fun e(any: Any)
    fun report(any: Any)
    fun boom(tag: String, e: Exception)


}