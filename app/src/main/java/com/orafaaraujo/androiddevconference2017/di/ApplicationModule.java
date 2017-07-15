package com.orafaaraujo.androiddevconference2017.di;

import android.app.Application;
import android.content.Context;

import com.orafaaraujo.androiddevconference2017.DevConferenceApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * {@link Module} exposing the {@link Application} main fields.
 */
@Module
class ApplicationModule {

    private final DevConferenceApplication mApplication;

    ApplicationModule(DevConferenceApplication app) {
        mApplication = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mApplication.getApplicationContext();
    }
}
