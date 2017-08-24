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
import android.util.Log
import com.orafaaraujo.androiddevconference2017.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton

class PermissionHelper : DialogFragment() {

    private val TAG = PermissionHelper::class.java.simpleName

    private val PERMISSION_TAG = "PERMISSION_TAG"

    private val PERMISSIONS = arrayOf(permission.CAMERA, permission.RECORD_AUDIO)
    private val PERMISSION_RC = 100

    private var mAcceptedAll: Boolean = false
    private var mShouldRetry: Boolean = false
    private var mExternalRequestRequired: Boolean = false

    private var mCheckPermissionStatus: Boolean = false

    private lateinit var mListener: PermissionListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
        if (context is PermissionListener) {
            mListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // Styling to make the background a little darker, like a dialog background.
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PermissionsDialogFragmentStyle)
        isCancelable = false

        requestPermissions(PERMISSIONS, PERMISSION_RC)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        if (mCheckPermissionStatus && mAcceptedAll) {
            onPermissionGranted()
        } else {
            if (mExternalRequestRequired) {
                showAppSettingDialog()
            } else if (mShouldRetry) {
                showRetryDialog()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")

        resetFlags()

        permissions.indices
                .filter { grantResults[it] == PackageManager.PERMISSION_DENIED }
                .forEach {
                    if (!shouldShowRequestPermissionRationale(permissions[it])) {
                        mExternalRequestRequired = true
                        return
                    } else {
                        mShouldRetry = true
                    }
                }
        if (!mShouldRetry) {
            mAcceptedAll = true
        }
    }

    private fun resetFlags() {
        mCheckPermissionStatus = true

        mAcceptedAll = false
        mShouldRetry = false
        mExternalRequestRequired = false
    }

    fun requestPermissionIfNeeded(fragmentManager: FragmentManager) {
        Log.d(TAG, "requestPermissionIfNeeded")

        val fragment = fragmentManager.findFragmentByTag(PERMISSION_TAG)
        if (fragment == null && !isAdded) {
            show(fragmentManager, PERMISSION_TAG)
            fragmentManager.executePendingTransactions()
        }
    }

    private fun showAppSettingDialog() {
        Log.d(TAG, "showAppSettingDialog")

        val pwAlert = activity.alert(Appcompat) {
            titleResource = R.string.permission_setting_title
            messageResource = R.string.permission_setting_message
            okButton { showAppSettings() }
            cancelButton { onPermissionDenied() }
        }.build()
        pwAlert.setCancelable(false)
        pwAlert.setCanceledOnTouchOutside(false)
        pwAlert.show()
    }

    private fun showRetryDialog() {
        Log.d(TAG, "showRetryDialog")

        val pwAlert = activity.alert(Appcompat) {
            titleResource = R.string.permission_retry_title
            messageResource = R.string.permission_retry_message
            okButton { requestPermissions(PERMISSIONS, PERMISSION_RC) }
            cancelButton { onPermissionDenied() }
        }.build()
        pwAlert.setCancelable(false)
        pwAlert.setCanceledOnTouchOutside(false)
        pwAlert.show()

    }

    private fun onPermissionGranted() {
        Log.d(TAG, "onPermissionGranted")

        dismiss()
        mListener.onPermissionGranted()
    }

    private fun onPermissionDenied() {
        Log.d(TAG, "onPermissionDenied")

        dismiss()
        mListener.onPermissionDenied()
    }

    private fun showAppSettings() {
        Log.d(TAG, "showAppSettings")

        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
        dismiss()
    }

}
