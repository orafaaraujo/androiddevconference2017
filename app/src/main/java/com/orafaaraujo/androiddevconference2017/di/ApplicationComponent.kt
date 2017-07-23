package com.orafaaraujo.androiddevconference2017.di

import com.orafaaraujo.androiddevconference2017.DevConferenceApplication
import com.orafaaraujo.androiddevconference2017.ui.MainNewWayDaggerActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Main application [Component].
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(devConferenceApplication: DevConferenceApplication)

    fun inject(mainNewWayDaggerActivity: MainNewWayDaggerActivity)
}
