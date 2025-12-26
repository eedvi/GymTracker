package com.gymtracker.domain.model

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español")
}

enum class WeightUnit(val symbol: String, val displayName: String) {
    KG("kg", "Kilograms"),
    LBS("lb", "Libras")
}

data class AppSettings(
    val language: Language = Language.ENGLISH,
    val weightUnit: WeightUnit = WeightUnit.KG
)
