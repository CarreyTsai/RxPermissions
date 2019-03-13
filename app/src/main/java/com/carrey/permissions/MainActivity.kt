package com.carrey.permissions

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn).setOnClickListener {
            RxPermissions.newInstance(this)
                .request(
                    "xxx", "xxx2",
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                )
                .subscribe {
                    println(it.isGranted)
                }
        }

    }
}
