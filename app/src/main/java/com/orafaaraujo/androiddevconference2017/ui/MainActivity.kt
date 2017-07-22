package com.orafaaraujo.androiddevconference2017.ui

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.di.Injector

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Injector.getApplicationComponent().inject(this)

        findViewById(R.id.activity_main_camera_title).
                setOnClickListener {

                    // Recupera o status da permissão
                    val permissionCheck = ContextCompat.checkSelfPermission(this, CAMERA)

                    // Permissão não está concedida?
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

                        // Usuário já recebeu a mensagem alguma vez e precisa de uma explicação melhor?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                            showRetryMessage()
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), PERMISSION_REQUEST_CODE)
                        }
                    } else {
                        takeAPic()
                    }
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.contains(CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeAPic()
                } else {

                }
            }
        }
    }

    fun showRetryMessage() {
        toast(R.string.activity_main_take_pic_retry)
    }

    fun showConfigurationMessage() {
        toast(R.string.activity_main_take_pic_configuration)
    }

    fun takeAPic() {
        toast(R.string.activity_main_take_pic_success)
    }

    fun Activity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, getString(message), duration).show()
    }

}