package com.orafaaraujo.androiddevconference2017.di;

import com.orafaaraujo.androiddevconference2017.DevConferenceApplication;

import java.util.Objects;

/**
 * Class responsible to initialize the {@link ApplicationComponent}, setting all the applicable
 * {@link dagger.Module}.
 */
public class Injector {

    private static ApplicationComponent mApplicationComponent;

    /**
     * Private constructor to avoid instantiation.
     */
    private Injector() {
    }

    /**
     * Initialize the {@link ApplicationComponent}, setting all the related {@link dagger.Module}.
     *
     * @param app application class
     */
    public static void initializeApplicationComponent(DevConferenceApplication app) {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(app))
                .build();
    }

    public static ApplicationComponent getApplicationComponent() {
        Objects.requireNonNull(mApplicationComponent, "ApplicationComponent is null");
        return mApplicationComponent;
    }
}
