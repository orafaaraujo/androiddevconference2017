package com.orafaaraujo.androiddevconference2017.ui

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.helper.PermissionHelper


class MainCurrentWayActivity : AppCompatActivity() {

    private val TAG = PermissionHelper::class.java.simpleName

    // Código para recuperar se status da permissão recebida é o mesmo da permissão requisitada.
    private val PERMISSION_REQUEST_CODE = 100

    // Código para abrir a camera e recupera o conteúdo.
    private val REQUEST_IMAGE_CAPTURE: Int = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        findViewById(R.id.activity_main_camera).setOnClickListener {

            // Recupera o status da permissão
            val permissionCheck = ContextCompat.checkSelfPermission(this, CAMERA)

            // Permissão está concedida?
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                takeAPic()
            } else {
                ActivityCompat
                        .requestPermissions(this, arrayOf(CAMERA), PERMISSION_REQUEST_CODE)
            }
        }
    }

    /**
     * Recupera resultado da dialog de permissão
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult")

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeAPic()
            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showRetryMessage()
                } else {
                    // Usuário negou a permissão mas não marcou "Não perguntar novamente"
                    showConfigurationMessage()
                }
            }
        }
    }

    fun showRetryMessage() {
        Log.d(TAG, "showRetryMessage")
        toast(R.string.permission_retry_message)
    }

    fun showConfigurationMessage() {
        Log.d(TAG, "showConfigurationMessage")
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