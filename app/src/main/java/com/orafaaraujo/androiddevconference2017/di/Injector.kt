package com.orafaaraujo.androiddevconference2017.di

import com.orafaaraujo.androiddevconference2017.DevConferenceApplication

/**
 * Created by rafael on 7/23/17.
 */
object Injector {

    private lateinit var mApplicationComponent: ApplicationComponent

    fun initializeApplicationComponent(app: DevConferenceApplication) {
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(app))
                .build()
    }

    fun getApplicationComponent() = mApplicationComponent
}