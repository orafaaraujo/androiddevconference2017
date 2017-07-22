package com.orafaaraujo.androiddevconference2017.ui

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.helper.PermissionHelper
import com.orafaaraujo.androiddevconference2017.helper.PermissionJavaHelper

/**
 * Created by rafael on 7/22/17.
 */

class MainNewWayActivity : AppCompatActivity(), PermissionHelper.PermissionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helper: PermissionJavaHelper = PermissionJavaHelper()

        findViewById(R.id.activity_main_camera_title).
                setOnClickListener {
                    helper.requestPermissions(
                            supportFragmentManager,
                            arrayOf(Manifest.permission.CAMERA),
                            "retry", "retryyyy",
                            "config", "configggg")
                }
    }

    override fun onPermissionGranted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionDenied() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}