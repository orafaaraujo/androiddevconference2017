package com.orafaaraujo.androiddevconference2017;

import android.app.Application;

import com.orafaaraujo.androiddevconference2017.di.Injector;

/**
 * Created by Rafael Araujo on 15/07/17.
 */
public class DevConferenceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.initializeApplicationComponent(this);
        Injector.getApplicationComponent().inject(this);
    }
}
