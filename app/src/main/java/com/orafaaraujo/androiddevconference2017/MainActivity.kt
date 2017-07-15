package com.orafaaraujo.androiddevconference2017

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.activity_main_camera_title)
                .setOnClickListener {

                    // Recupera o status da permissão
                    val permissionCheck = ContextCompat.checkSelfPermission(this, CAMERA)

                    // Permissão não está concedida?
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

                        // Usuário já recebeu a mensagem alguma coisa?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {

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
                if (grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                    takeAPic()
                } else {

                }
            }
        }
    }

    fun takeAPic() {
        Toast.makeText(this, "Nova foto!", Toast.LENGTH_SHORT).show()
    }


}
