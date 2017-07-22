package com.orafaaraujo.androiddevconference2017.helper;

import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.orafaaraujo.androiddevconference2017.R;

import javax.inject.Inject;

/**
 * Manages the request permission flow. {@link PermissionListener} can be used by clients who
 * wants to be notified about the permission status.
 *
 * @see
 * <a href="https://android.jlelse.eu/keeping-android-runtime-permissions-from-cluttering-your-app-headless-dialog-fragments-6d675bf080c0">Keeping
 * Android runtime permissions from cluttering your app</a>
 * @see
 * <a href="https://github.com/tylerjroach/RuntimePermissionsExample">RuntimePermissionsExample</a>
 * @see
 * <a href="https://medium.com/@ali.muzaffar/use-headless-fragment-for-android-m-run-time-permissions-and-to-check-network-connectivity-b48615f6272d">Use
 * headless Fragment for Android M run-time permissions and to check network connectivity</a>
 */

public class PermissionJavaHelper extends DialogFragment {

    private static final String TAG = PermissionJavaHelper.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String SAVE_INSTANCE_ALREADY_ASKED_KEY = "SAVE_INSTANCE_ALREADY_ASKED_KEY";

    /**
     * Permission that should be validated.
     */
    private static final String[] PERMISSIONS = new String[]{
            permission.RECORD_AUDIO,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.ACCESS_FINE_LOCATION};

    /**
     * Indicates the user has denied at least one permission, but not checked "Don't ask again".
     */
    private boolean mShouldRetry;

    /**
     * Indicates the user has denied at least one permission and checked "Don't ask again".
     */
    private boolean mExternalRequestRequired;

    /**
     * Indicates <code>onRequestPermissionsResult()</code> was called and we need to process the
     * current status in the <code>onResume()</code>.
     */
    private boolean mCheckPermissionStatus;

    /**
     * Flag to avoid recreate permissions dialog in cause orientation changes.
     */
    private boolean mIsPermissionDialogShown;

    /**
     * Called when user reject once or didn't check "Don't ask again" when reject a permission.
     */
    private AlertDialog mRetryDialog;

    /**
     * Called when user reject at least one permission and check "Don't ask again", so the user
     * is advised to goes to configuration of the application to allow the permission manually.
     */
    private AlertDialog mAppSettingsDialog;

    private PermissionListener mListener;

    @Inject
    public PermissionJavaHelper() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (context instanceof PermissionListener) {
            mListener = (PermissionListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Styling to make the background a little darker, like a dialog background.
        setStyle(STYLE_NO_TITLE, R.style.PermissionsDialogFragmentStyle);

        mCheckPermissionStatus = false;
        mIsPermissionDialogShown = true;

        // Check if must show permissions dialog if orientation changes and fragment is recreated.
        // If already showing, don't create again. If don't, create when user rotate the phone.
        if (savedInstanceState == null ||
                savedInstanceState.containsKey(SAVE_INSTANCE_ALREADY_ASKED_KEY) &&
                        !savedInstanceState.getBoolean(SAVE_INSTANCE_ALREADY_ASKED_KEY)) {
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        // Save if permission dialog is already displayed.
        outState.putBoolean(SAVE_INSTANCE_ALREADY_ASKED_KEY, mIsPermissionDialogShown);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - check permissions:" + mCheckPermissionStatus);

        if (mCheckPermissionStatus) {
            if (areAllPermissionsGranted()) {
                Log.d(TAG, "all permissions granted");
                onPermissionGranted();
            } else {
                {
                    Log.d(TAG,
                            "not all permissions granted - retry:" + mShouldRetry
                                    + "external request:"
                                    + mExternalRequestRequired);
                }
                if (mExternalRequestRequired) {
                    showAppSettingDialog();
                } else if (mShouldRetry) {
                    showRetryDialog();
                }
            }
        }
    }

    private void onPermissionGranted() {
        if (mListener != null) {
            mListener.onPermissionGranted();
        }
        dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");

        mCheckPermissionStatus = true;
        mIsPermissionDialogShown = false;

        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            final int grantResult = grantResults[i];

            if (!shouldShowRequestPermissionRationale(permission)
                    && grantResult != PackageManager.PERMISSION_GRANTED) {
                mExternalRequestRequired = true;
                return;
            } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                mShouldRetry = true;
                return;
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
     *
     * @param fragmentManager {@link FragmentManager} of the Activity that is calling.
     */

    public void requestPermissionIfNeeded(@NonNull FragmentManager fragmentManager) {
        Log.d(TAG, "requestPermissionIfNeeded");
        final Fragment fragment = fragmentManager.findFragmentByTag(TAG);
        if (fragment == null && !isAdded()) {
            Log.d(TAG, "requestPermissionIfNeeded - show fragment");
            show(fragmentManager, TAG);
            fragmentManager.executePendingTransactions();
        }
    }

    /**
     * Check if all permissions already granted.
     *
     * @return <code>TRUE</code> case ALL permission are granted, <code>FALSE</code> otherwise.
     */
    private boolean areAllPermissionsGranted() {
        Log.d(TAG, "areAllPermissionsGranted");
        for (String permission : PERMISSIONS) {
            Log.d(TAG, "Checking permission: " + permission);
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showAppSettingDialog() {
        Log.d(TAG, "showAppSettingDialog");
        if (mAppSettingsDialog != null && mAppSettingsDialog.isShowing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog
                .Builder(getContext())
                .setTitle(R.string.permission_setting_title)
                .setMessage(R.string.permission_setting_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAppSettings();
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onDialogPermissionCanceled();
                            }
                        })
                .setCancelable(false);

        mAppSettingsDialog = builder.create();
        mAppSettingsDialog.show();
    }

    private void showRetryDialog() {
        Log.d(TAG, "showRetryDialog");
        if (mRetryDialog != null && mRetryDialog.isShowing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog
                .Builder(getActivity())
                .setTitle(R.string.permission_retry_title)
                .setMessage(R.string.permission_retry_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(PERMISSIONS,
                                PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onDialogPermissionCanceled();
                            }
                        })
                .setCancelable(false);

        mRetryDialog = builder.create();
        mRetryDialog.show();
    }

    private void onDialogPermissionCanceled() {
        Log.d(TAG, "onDialogPermissionCanceled");
        dismiss();
        mListener.onPermissionDenied();
    }

    private void showAppSettings() {
        Log.d(TAG, "showAppSettings");
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        final Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        getContext().startActivity(intent);
        dismiss();
    }

    /**
     * Callback interfaces for clients using this helper fragment.
     */
    public interface PermissionListener {

        /**
         * Called when all (and only all) permission are accepted.
         */
        void onPermissionGranted();

        /**
         * Called when at least one permission are denied.
         */
        void onPermissionDenied();
    }
}
