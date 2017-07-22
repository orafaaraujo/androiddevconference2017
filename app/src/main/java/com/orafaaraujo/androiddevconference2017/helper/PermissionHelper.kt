package com.orafaaraujo.androiddevconference2017.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import com.orafaaraujo.androiddevconference2017.R
import com.orafaaraujo.androiddevconference2017.helper.PermissionHelper.PermissionListener
import javax.inject.Inject

/**
 * Manages the request permission flow. [PermissionListener] can be used by clients who
 * wants to be notified about the permission status.
 */
class PermissionHelper @Inject constructor() : DialogFragment() {

    private val TAG = PermissionHelper::class.java.simpleName

    private val PERMISSION_REQUEST_CODE = 100

    private val SAVE_INSTANCE_ALREADY_ASKED_KEY = "SAVE_INSTANCE_ALREADY_ASKED_KEY"

    /**
     * Permission that should be validated.
     */
    private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * Indicates the user has denied at least one permission, but not checked "Don't ask again".
     */
    private var mShouldRetry: Boolean = false

    /**
     * Indicates the user has denied at least one permission and checked "Don't ask again".
     */
    private var mExternalRequestRequired: Boolean = false

    /**
     * Indicates `onRequestPermissionsResult()` was called and we need to process the
     * current status in the `onResume()`.
     */
    private var mCheckPermissionStatus: Boolean = false

    /**
     * Flag to avoid recreate permissions dialog in cause orientation changes.
     */
    private var mIsPermissionDialogShown: Boolean = false

    /**
     * Called when user reject once or didn't check "Don't ask again" when reject a permission.
     */
    private var mRetryDialog: AlertDialog? = null

    /**
     * Called when user reject at least one permission and check "Don't ask again", so the user
     * is advised to goes to configuration of the application to allow the permission manually.
     */
    private var mAppSettingsDialog: AlertDialog? = null

    private var mListener: PermissionListener? = null

    private var mRetryMessage: String? = null

    private var mConfigurationMessage: String? = null

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

        mCheckPermissionStatus = false
        mIsPermissionDialogShown = true

        // Check if must show permissions dialog if orientation changes and fragment is recreated.
        // If already showing, don't create again. If don't, create when user rotate the phone.
        if (savedInstanceState == null
                || savedInstanceState.containsKey(SAVE_INSTANCE_ALREADY_ASKED_KEY)
                && !savedInstanceState.getBoolean(SAVE_INSTANCE_ALREADY_ASKED_KEY)) {
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        // Save if permission dialog is already displayed.
        outState?.putBoolean(SAVE_INSTANCE_ALREADY_ASKED_KEY, mIsPermissionDialogShown)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - check permissions:" + mCheckPermissionStatus)

        if (mCheckPermissionStatus) {
            if (areAllPermissionsGranted()) {
                Log.d(TAG, "all permissions granted")
                onPermissionGranted()
            } else {
                Log.d(TAG, "not all permissions granted - retry:" + mShouldRetry
                        + "external request:" + mExternalRequestRequired)
                if (mExternalRequestRequired) {
                    showAppSettingDialog()
                } else if (mShouldRetry) {
                    showRetryDialog()
                }
            }
        }
    }

    private fun onPermissionGranted() {
        mListener?.onPermissionGranted()
        dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")

        mCheckPermissionStatus = true
        mIsPermissionDialogShown = false

        for (i in permissions.indices) {
            val permission = permissions[i]
            val grantResult = grantResults[i]

            if (!shouldShowRequestPermissionRationale(permission) && grantResult != PackageManager.PERMISSION_GRANTED) {
                mExternalRequestRequired = true
                return
            } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                mShouldRetry = true
                return
            }
        }
    }

    /**
     * Starts the permission flow. If this is the first time user is facing the permission, a
     * dialog to confirm and reject will be shown. If the user already rejected once and
     * checked to
     * never be asked, the configuration screen is shown, so the user can toggle the
     * permissions by
     * itself.

     * @param fragmentManager [FragmentManager] of the Activity that is calling.
     */

    fun requestPermissionIfNeeded(fragmentManager: FragmentManager,
                                  configurationMessage: String? = "aaa",
                                  retryMessage: String = "Bbb" ) {
        Log.d(TAG, "requestPermissionIfNeeded")
        val fragment = fragmentManager.findFragmentByTag(TAG)
        if (fragment == null && !isAdded) {
            Log.d(TAG, "requestPermissionIfNeeded - show fragment")
            show(fragmentManager, TAG)
            fragmentManager.executePendingTransactions()

            mConfigurationMessage = configurationMessage
            mRetryMessage = retryMessage
        }
    }

    /**
     * Check if all permissions already granted.

     * @return `TRUE` case ALL permission are granted, `FALSE` otherwise.
     */
    private fun areAllPermissionsGranted(): Boolean {
        Log.d(TAG, "areAllPermissionsGranted")
        for (permission in PERMISSIONS) {
            Log.d(TAG, "Checking permission: " + permission)
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    private fun showAppSettingDialog() {
        Log.d(TAG, "showAppSettingDialog")
        if (mAppSettingsDialog?.isShowing == true) {
            return
        }

        val builder = AlertDialog.Builder(context)
                .setMessage(mConfigurationMessage)
                .setPositiveButton(android.R.string.ok) { _, _ -> showAppSettings() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> onDialogPermissionCanceled() }

        mAppSettingsDialog = builder.create()
        mAppSettingsDialog?.show()
    }

    private fun showRetryDialog() {
        Log.d(TAG, "showRetryDialog")
        if (mRetryDialog?.isShowing == true) {
            return
        }

        val builder = AlertDialog.Builder(activity)
                .setMessage(mRetryMessage)
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> onDialogPermissionCanceled() }

        mRetryDialog = builder.create()
        mRetryDialog?.show()
    }

    private fun showAppSettings() {
        Log.d(TAG, "showAppSettings")
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
        dismiss()
    }

    private fun onDialogPermissionCanceled() {
        Log.d(TAG, "onDialogPermissionCanceled")
        dismiss()
        mListener?.onPermissionDenied()
    }

    /**
     * Callback interfaces for clients using this helper fragment.
     */
    internal interface PermissionListener {

        /**
         * Called when all (and only all) permission are accepted.
         */
        fun onPermissionGranted()

        /**
         * Called when at least one permission are denied.
         */
        fun onPermissionDenied()
    }

}
