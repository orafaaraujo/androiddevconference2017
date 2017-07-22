package com.orafaaraujo.androiddevconference2017.helper;

import android.Manifest;
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

import java.util.Arrays;


/**
 * Manages the request permission flow. {@link PermissionListener} can be used by clients who
 * wants to be notified about the permission status.
 */

public class PermissionJavaHelper extends DialogFragment {

    private static final String TAG = PermissionJavaHelper.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String SAVE_INSTANCE_ALREADY_ASKED_KEY = "SAVE_INSTANCE_ALREADY_ASKED_KEY";

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

    private Context mContext;

    private String[] mRequestedPermissions;

    private String mRetryTitle;

    private String mRetryMessage;

    private String mConfigurationTitle;

    private String mConfigurationMessage;

    private PermissionListener mListener;

    public PermissionJavaHelper() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        mContext = context;
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
        setCancelable(false);

        mCheckPermissionStatus = false;
        mIsPermissionDialogShown = true;

        // Check if must show permissions dialog if orientation changes and fragment is recreated.
        // If already showing, don't create again. If don't, create when user rotate the phone.
        if (savedInstanceState == null ||
                savedInstanceState.containsKey(SAVE_INSTANCE_ALREADY_ASKED_KEY) &&
                        !savedInstanceState.getBoolean(SAVE_INSTANCE_ALREADY_ASKED_KEY)) {


            if (mRequestedPermissions != null && mRequestedPermissions.length != 0) {
                requestPermissions(mRequestedPermissions, PERMISSION_REQUEST_CODE);
            }
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
            if (areAllPermissionsGranted(mRequestedPermissions)) {
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

    private void onPermissionGranted() {
        if (mListener != null) {
            mListener.onPermissionGranted();
        }
        dismiss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    /**
     * Requests a single permission. If this is the first time user is facing the permission, a
     * dialog to confirm and reject will be shown. If the user already rejected once and
     * checked to
     * never be asked, the configuration screen is shown, so the user can toggle the
     * permissions by
     * itself.
     *
     * @param fragmentManager {@link FragmentManager} of the Activity that is calling.
     * @param permissions     the permissions being requested.
     */
    public void requestPermissions(@NonNull FragmentManager fragmentManager,
            @NonNull String[] permissions, String retryTitle, String retryMessage,
            String configurationTitle, String configurationMessage) {

        Log.d(TAG, "requestPermissions");
        if (permissions.length == 0) {
            throw new IllegalArgumentException("Permissions array cannot be empty.");
        }

        mRequestedPermissions = Arrays.copyOf(permissions, permissions.length);
        mRetryTitle = retryTitle;
        mRetryMessage = retryMessage;
        mConfigurationTitle = configurationTitle;
        mConfigurationMessage = configurationMessage;

        final Fragment fragment = fragmentManager.findFragmentByTag(TAG);
        if (fragment == null && !isAdded()) {
            Log.d(TAG, "requestPermissions - show fragment");
            show(fragmentManager, TAG);
            fragmentManager.executePendingTransactions();
        }
    }

    /**
     * Check if given permissions granted.
     *
     * @param permissions the {@link Manifest.permission}s to be checked.
     * @return <code>true</code> case ALL permission are granted, <code>false</code> otherwise.
     */
    private boolean areAllPermissionsGranted(@NonNull String[] permissions) {
        Log.d(TAG, "areAllPermissionsGranted");
        if (permissions.length == 0) {
            throw new IllegalArgumentException("Permissions array cannot be empty.");
        }

        for (String permission : permissions) {
            Log.d(TAG, "Checking permission: " + permission);
            if (ContextCompat.checkSelfPermission(mContext, permission)
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
                .setTitle(mConfigurationTitle)
                .setMessage(mConfigurationMessage)
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
                .setTitle(mRetryTitle)
                .setMessage(mRetryMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(mRequestedPermissions, PERMISSION_REQUEST_CODE);
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

        if (mListener != null) {
            mListener.onPermissionDenied();
        }
        dismiss();
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
