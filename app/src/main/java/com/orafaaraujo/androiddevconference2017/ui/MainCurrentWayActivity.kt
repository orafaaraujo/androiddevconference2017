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
import android.widget.Toast
import com.orafaaraujo.androiddevconference2017.R


class MainCurrentWayActivity : AppCompatActivity() {

    // Código para recuperar se status da permissão recebida é o mesmo da permissão requisitada.
    private val PERMISSION_REQUEST_CODE = 100

    // Código para abrir a camera e recupera o conteúdo.
    private val REQUEST_IMAGE_CAPTURE: Int = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.activity_main_camera_title).
                setOnClickListener {

                    // Recupera o status da permissão
                    val permissionCheck = ContextCompat.checkSelfPermission(this, CAMERA)

                    // Permissão não está concedida?
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        // Requisita a permissão
                        ActivityCompat
                                .requestPermissions(this, arrayOf(CAMERA), PERMISSION_REQUEST_CODE)
                    } else {
                        takeAPic()
                    }
                }
    }

    /**
     * Recupera resultado da dialog de permissão
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {

            // Usuário negou a permissão e marcou "Nãro perguntar novamente"
            if (!shouldShowRequestPermissionRationale(permissions[0]) && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Avisar que usuário deve conceder permissão nas Configurações
                showConfigurationMessage()
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Avisar usuário para tentar novamente a aceitar
                showRetryMessage()
            } else {
                takeAPic()
            }
        }
    }

    fun showRetryMessage() {
        toast(R.string.activity_main_take_pic_retry)
    }

    fun showConfigurationMessage() {
        toast(R.string.activity_main_take_pic_configuration)
    }

    fun AppCompatActivity.takeAPic() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

    }

    fun AppCompatActivity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, getString(message), duration).show()
    }

}