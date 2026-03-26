package com.easyaccounting.util

import android.content.Context

object AppPreferences {
    const val PREFS_NAME = "easy_accounting_prefs"
    const val KEY_AUTO_ACCOUNTING = "auto_accounting_enabled"
    const val KEY_THEME = "app_theme"
    const val DEFAULT_THEME = "purple"

    fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedThemeName(context: Context): String {
        return prefs(context).getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }
}
