package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppLanguage
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.moneyHubDataStore by preferencesDataStore(name = "money_hub_prefs")

class PreferencesRepository(private val context: Context) {

    private val languageKey = stringPreferencesKey("app_language")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val languageFlow: Flow<AppLanguage> = context.moneyHubDataStore.data.map { prefs ->
        val saved = prefs[languageKey]
        if (saved != null) {
            AppLanguage.fromCode(saved)
        } else {
            val deviceLang = Locale.getDefault().language
            if (deviceLang.startsWith("en", ignoreCase = true)) AppLanguage.EN else AppLanguage.PT
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.moneyHubDataStore.data.map { prefs ->
        ThemeMode.fromCode(prefs[themeModeKey] ?: ThemeMode.DARK.code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.moneyHubDataStore.edit { prefs ->
            prefs[languageKey] = language.code
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.moneyHubDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.code
        }
    }
}
