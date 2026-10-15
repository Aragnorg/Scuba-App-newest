package com.example.ui.i18n

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val shortName: String,
    val flag: String
) {
    EN("en", "English", "EN", "🇬🇧"),
    DE("de", "Deutsch", "DE", "🇩🇪");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DE
        }
    }
}
