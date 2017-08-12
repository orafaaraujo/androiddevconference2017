package com.orafaaraujo.androiddevconference2017.helper

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import com.orafaaraujo.androiddevconference2017.R
import org.jetbrains.anko.support.v4.alert

class PermissionHelper : DialogFragment() {

    private val PERMISSION_TAG = "PERMISSION_TAG"
    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS = arrayOf(permission.CAMERA, permission.RECORD_AUDIO)

    private var mShouldRetry: Boolean = false
    private var mExternalRequestRequired: Boolean = false
    private var mCheckPermissionStatus: Boolean = false

    private lateinit var mListener: PermissionListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is PermissionListener) {
            mListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Styling to make the background a little darker, like a dialog background.
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PermissionsDialogFragmentStyle)
        isCancelable = true

        mCheckPermissionStatus = false
        requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    override fun onResume() {
        super.onResume()

        if (mCheckPermissionStatus) {
            if (areAllPermissionsGranted()) {
                onPermissionGranted()
            } else {
                if (mShouldRetry) {
                    showRetryDialog()
                } else if (mExternalRequestRequired) {
                    showAppSettingDialog()
                }
            }
        }
    }

    private fun onPermissionGranted() {
        mListener.onPermissionGranted()
        dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        mCheckPermissionStatus = true

        for (i in permissions.indices) {
            val permission = permissions[i]
            val grantResult = grantResults[i]

            if (grantResult != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(permission)) {
                    mShouldRetry = true
                    return
                } else {
                    mExternalRequestRequired = true
                    return
                }
            }
        }
    }

    fun requestPermissionIfNeeded(fragmentManager: FragmentManager) {
        val fragment = fragmentManager.findFragmentByTag(PERMISSION_TAG)
        if (fragment == null && !isAdded) {
            show(fragmentManager, PERMISSION_TAG)
            fragmentManager.executePendingTransactions()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return PERMISSIONS.none {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }
    }

    private fun showAppSettingDialog() {
        alert(R.string.permission_setting_message) {
            titleResource = R.string.permission_setting_title
            positiveButton(android.R.string.ok) { showAppSettings() }
            negativeButton(android.R.string.cancel) { onDialogPermissionCanceled() }
        }.show()
    }

    private fun showRetryDialog() {
        alert(R.string.permission_retry_message) {
            titleResource = R.string.permission_retry_title
            positiveButton(android.R.string.ok) { requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE) }
            negativeButton(android.R.string.cancel) { onDialogPermissionCanceled() }
        }.show()
    }

    private fun onDialogPermissionCanceled() {
        dismiss()
        mListener.onPermissionDenied()
    }

    private fun showAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
        dismiss()
    }

}
