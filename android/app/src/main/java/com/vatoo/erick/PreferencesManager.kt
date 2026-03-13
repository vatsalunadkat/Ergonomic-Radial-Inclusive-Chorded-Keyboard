package com.vatoo.erick

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vatoo.erick.shared.ColorPaletteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val LAYOUT_TYPE_KEY      = stringPreferencesKey("layout_type")
        private val DARK_THEME_KEY       = booleanPreferencesKey("dark_theme")
        private val COLOR_PALETTE_KEY    = stringPreferencesKey("color_palette")   // replaces old bool
        private val LEFT_HANDED_MODE_KEY = booleanPreferencesKey("left_handed_mode")

        const val LAYOUT_LOGICAL    = "logical"
        const val LAYOUT_EFFICIENCY = "efficiency"
    }

    val layoutType: Flow<String> = context.dataStore.data
        .map { it[LAYOUT_TYPE_KEY] ?: LAYOUT_LOGICAL }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_THEME_KEY] ?: false }

    /** Emits the active ColorPaletteType; defaults to DEFAULT. */
    val colorPalette: Flow<ColorPaletteType> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[COLOR_PALETTE_KEY] ?: ColorPaletteType.DEFAULT.name
            runCatching { ColorPaletteType.valueOf(raw) }.getOrDefault(ColorPaletteType.DEFAULT)
        }

    val leftHandedMode: Flow<Boolean> = context.dataStore.data
        .map { it[LEFT_HANDED_MODE_KEY] ?: false }

    suspend fun setLayoutType(layoutType: String) {
        context.dataStore.edit { it[LAYOUT_TYPE_KEY] = layoutType }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }

    suspend fun setColorPalette(type: ColorPaletteType) {
        context.dataStore.edit { it[COLOR_PALETTE_KEY] = type.name }
    }

    suspend fun setLeftHandedMode(enabled: Boolean) {
        context.dataStore.edit { it[LEFT_HANDED_MODE_KEY] = enabled }
    }
}