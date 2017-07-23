package com.orafaaraujo.androiddevconference2017.di

import android.app.Application
import android.content.Context
import com.orafaaraujo.androiddevconference2017.DevConferenceApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * [Module] exposing the [Application] main fields.
 */
@Module
class ApplicationModule(val mApplication: DevConferenceApplication) {

    @Provides
    @Singleton
    fun provideApplication() = mApplication

    @Provides
    @Singleton
    fun provideContext(): Context = mApplication.applicationContext
}
