package com.gymtracker.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gymtracker.domain.model.Language

object LocaleHelper {

    fun setLocale(language: Language) {
        val localeTag = when (language) {
            Language.ENGLISH -> "en"
            Language.SPANISH -> "es"
        }
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getCurrentLanguage(): Language {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            return Language.ENGLISH
        }
        val locale = currentLocales[0]
        return when (locale?.language) {
            "es" -> Language.SPANISH
            else -> Language.ENGLISH
        }
    }
}
