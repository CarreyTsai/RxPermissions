package com.carrey.permissions

import java.lang.Exception

/**
 * Created by carrey on 2019/3/13.
 *
 * Desc:
 */
object DefaultPermissionLog : IPermissionLog {
    override fun boom(tag: String, e: Exception) {
        println(tag + e)

    }

    override fun d(any: Any) {
        println(any)
    }

    override fun e(any: Any) {
        println(any)
    }

    override fun report(any: Any) {
        println(any)
    }
}