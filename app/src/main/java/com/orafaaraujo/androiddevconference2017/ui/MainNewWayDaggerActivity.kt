package com.orafaaraujo.androiddevconference2017.ui

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.di.Injector
import com.orafaaraujo.androiddevconference2017.helper.PermissionHelper
import javax.inject.Inject

class MainNewWayDaggerActivity : AppCompatActivity(), PermissionHelper.PermissionListener {

    private val TAG = PermissionHelper::class.java.simpleName

    // Código para abrir a camera e recupera o conteúdo.
    private val REQUEST_IMAGE_CAPTURE: Int = 1234

    @Inject
    lateinit var mPermissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Injector.getApplicationComponent().inject(this)

        findViewById(R.id.activity_main_camera_title).setOnClickListener {
            mPermissionHelper.requestPermissionIfNeeded(supportFragmentManager)
        }
    }

    override fun onPermissionGranted() {
        Log.d(TAG, "onPermissionGranted")
        takeAPic()
    }

    override fun onPermissionDenied() {
        Log.d(TAG, "onPermissionDenied")
        toast(R.string.permission_setting_message)
    }

    fun takeAPic() {
        Log.d(TAG, "takeAPic")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    fun toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, getString(message), duration).show()
    }

}
