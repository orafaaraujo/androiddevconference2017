package com.orafaaraujo.androiddevconference2017.helper;

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