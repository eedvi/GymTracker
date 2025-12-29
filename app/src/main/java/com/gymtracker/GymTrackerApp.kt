package com.gymtracker

import android.app.Application
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GymTrackerApp : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applyStoredLocale()
    }

    private fun applyStoredLocale() {
        applicationScope.launch {
            val settings = settingsRepository.getSettings().first()
            LocaleHelper.setLocale(settings.language)
        }
    }
}
