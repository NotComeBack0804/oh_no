package com.easyaccounting.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.easyaccounting.R

object ThemeUtils {
    fun applySelectedTheme(activity: AppCompatActivity) {
        activity.setTheme(resolveThemeResId(AppPreferences.getSelectedThemeName(activity)))
    }

    fun applyScreenBackground(target: View, activity: AppCompatActivity) {
        target.setBackgroundResource(getScreenBackgroundResId(AppPreferences.getSelectedThemeName(activity)))
    }

    fun getThemeRadioButtonId(themeName: String): Int = when (themeName) {
        "ocean" -> R.id.rb_theme_ocean
        "forest" -> R.id.rb_theme_forest
        "sunrise" -> R.id.rb_theme_sunrise
        "rose" -> R.id.rb_theme_rose
        else -> R.id.rb_theme_purple
    }

    fun getThemeNameForRadioButton(radioButtonId: Int): String? = when (radioButtonId) {
        R.id.rb_theme_purple -> "purple"
        R.id.rb_theme_ocean -> "ocean"
        R.id.rb_theme_forest -> "forest"
        R.id.rb_theme_sunrise -> "sunrise"
        R.id.rb_theme_rose -> "rose"
        else -> null
    }

    fun getScreenBackgroundResId(themeName: String): Int = when (themeName) {
        "ocean" -> R.drawable.bg_screen_ocean
        "forest" -> R.drawable.bg_screen_forest
        "sunrise" -> R.drawable.bg_screen_sunrise
        "rose" -> R.drawable.bg_screen_rose
        else -> R.drawable.bg_screen_purple
    }

    private fun resolveThemeResId(themeName: String): Int = when (themeName) {
        "ocean" -> R.style.Theme_EasyAccounting_Ocean
        "forest" -> R.style.Theme_EasyAccounting_Forest
        "sunrise" -> R.style.Theme_EasyAccounting_Sunrise
        "rose" -> R.style.Theme_EasyAccounting_Rose
        else -> R.style.Theme_EasyAccounting
    }
}
