package com.orafaaraujo.androiddevconference2017.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.helper.PermissionHelper

/**
 * Created by rafael on 7/22/17.
 */

class MainNewWayActivity : AppCompatActivity(), PermissionHelper.PermissionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helper: PermissionHelper = PermissionHelper()

        findViewById(R.id.activity_main_camera_title).
                setOnClickListener {
                    helper.requestPermissionIfNeeded(supportFragmentManager)
                }
    }

    override fun onPermissionGranted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionDenied() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
