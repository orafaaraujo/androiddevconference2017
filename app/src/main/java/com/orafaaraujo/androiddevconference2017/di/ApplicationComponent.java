package com.orafaaraujo.androiddevconference2017.di;

import android.support.annotation.NonNull;

import com.orafaaraujo.androiddevconference2017.DevConferenceApplication;
import com.orafaaraujo.androiddevconference2017.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Main application {@link Component}.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(@NonNull DevConferenceApplication devConferenceApplication);

    void inject(@NonNull MainActivity mainActivity);
}
