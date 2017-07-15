package com.orafaaraujo.androiddevconference2017.ui;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import dagger.Reusable;

/**
 * A simple {@link Fragment} subclass.
 */
@Reusable
public class PermissionFragment extends DialogFragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Inject
    public PermissionFragment() {
    }


}
