package com.advancedclock.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "widget_settings")

class WidgetSettingsRepository(private val context: Context) {

    companion object {
        @Volatile
        private var instance: WidgetSettingsRepository? = null

        fun getInstance(context: Context): WidgetSettingsRepository =
            instance ?: synchronized(this) {
                instance ?: WidgetSettingsRepository(context.applicationContext).also { instance = it }
            }
    }

    private object Keys {
        // Float keys (new — v2)
        val TIME_SIZE = floatPreferencesKey("time_size_f")
        val DATE_SIZE = floatPreferencesKey("date_size_f")
        val DAY_OF_WEEK_SIZE = floatPreferencesKey("day_of_week_size_f")
        val TIME_ZONE_SIZE = floatPreferencesKey("timezone_size_f")
        val WEEK_NUMBER_SIZE = floatPreferencesKey("week_number_size_f")
        val MONTH_NAME_SIZE = floatPreferencesKey("month_name_size_f")
        val NEXT_ALARM_SIZE = floatPreferencesKey("next_alarm_size_f")
        // Legacy string keys — read-once for migration
        val TIME_SIZE_LEGACY = stringPreferencesKey("time_size")
        val DATE_SIZE_LEGACY = stringPreferencesKey("date_size")
        val DAY_OF_WEEK_SIZE_LEGACY = stringPreferencesKey("day_of_week_size")
        val TIME_ZONE_SIZE_LEGACY = stringPreferencesKey("timezone_size")
        val WEEK_NUMBER_SIZE_LEGACY = stringPreferencesKey("week_number_size")
        val MONTH_NAME_SIZE_LEGACY = stringPreferencesKey("month_name_size")
        val NEXT_ALARM_SIZE_LEGACY = stringPreferencesKey("next_alarm_size")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val NEXT_ALARM_MILLIS = longPreferencesKey("next_alarm_millis")
        val WEEK_RULE = stringPreferencesKey("week_rule")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val TIME_ZONE_ID = stringPreferencesKey("time_zone_id")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val ADVANCED_LAYOUT_TEMPLATE = stringPreferencesKey("advanced_layout_template")
        val LOADED_PRESET_ID = longPreferencesKey("loaded_preset_id")
    }

    val settings: Flow<WidgetSettings> = context.dataStore.data.map { prefs ->
        WidgetSettings(
            timeSize = prefs[Keys.TIME_SIZE] ?: prefs[Keys.TIME_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_LARGE,
            dateSize = prefs[Keys.DATE_SIZE] ?: prefs[Keys.DATE_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_NORMAL,
            dayOfWeekSize = prefs[Keys.DAY_OF_WEEK_SIZE] ?: prefs[Keys.DAY_OF_WEEK_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_OFF,
            timeZoneSize = prefs[Keys.TIME_ZONE_SIZE] ?: prefs[Keys.TIME_ZONE_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_OFF,
            weekNumberSize = prefs[Keys.WEEK_NUMBER_SIZE] ?: prefs[Keys.WEEK_NUMBER_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_OFF,
            monthNameSize = prefs[Keys.MONTH_NAME_SIZE] ?: prefs[Keys.MONTH_NAME_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_OFF,
            nextAlarmSize = prefs[Keys.NEXT_ALARM_SIZE] ?: prefs[Keys.NEXT_ALARM_SIZE_LEGACY]?.let { WidgetSettings.sizeFromString(it) } ?: WidgetSettings.SIZE_SMALL,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: false,
            nextAlarmMillis = prefs[Keys.NEXT_ALARM_MILLIS] ?: 0L,
            weekRule = prefs[Keys.WEEK_RULE] ?: "locale",
            dateFormat = prefs[Keys.DATE_FORMAT] ?: "EEE, MMM d, yyyy",
            timeFormat = prefs[Keys.TIME_FORMAT] ?: "system",
            timeZoneId = prefs[Keys.TIME_ZONE_ID] ?: "",
            fontFamily = prefs[Keys.FONT_FAMILY] ?: "default",
            fontSize = prefs[Keys.FONT_SIZE] ?: 32f,
            accentColor = prefs[Keys.ACCENT_COLOR] ?: 0L,
            backgroundOpacity = prefs[Keys.BACKGROUND_OPACITY] ?: 0.5f,
            advancedLayoutTemplate = prefs[Keys.ADVANCED_LAYOUT_TEMPLATE] ?: WidgetSettings().advancedLayoutTemplate,
            loadedPresetId = prefs[Keys.LOADED_PRESET_ID] ?: -1L
        )
    }

    suspend fun updateSettings(settings: WidgetSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TIME_SIZE] = settings.timeSize
            prefs[Keys.DATE_SIZE] = settings.dateSize
            prefs[Keys.DAY_OF_WEEK_SIZE] = settings.dayOfWeekSize
            prefs[Keys.TIME_ZONE_SIZE] = settings.timeZoneSize
            prefs[Keys.WEEK_NUMBER_SIZE] = settings.weekNumberSize
            prefs[Keys.MONTH_NAME_SIZE] = settings.monthNameSize
            prefs[Keys.NEXT_ALARM_SIZE] = settings.nextAlarmSize
            // Drop legacy string keys once floats are written
            prefs.remove(Keys.TIME_SIZE_LEGACY)
            prefs.remove(Keys.DATE_SIZE_LEGACY)
            prefs.remove(Keys.DAY_OF_WEEK_SIZE_LEGACY)
            prefs.remove(Keys.TIME_ZONE_SIZE_LEGACY)
            prefs.remove(Keys.WEEK_NUMBER_SIZE_LEGACY)
            prefs.remove(Keys.MONTH_NAME_SIZE_LEGACY)
            prefs.remove(Keys.NEXT_ALARM_SIZE_LEGACY)
            prefs[Keys.SHOW_SECONDS] = settings.showSeconds
            prefs[Keys.NEXT_ALARM_MILLIS] = settings.nextAlarmMillis
            prefs[Keys.WEEK_RULE] = settings.weekRule
            prefs[Keys.DATE_FORMAT] = settings.dateFormat
            prefs[Keys.TIME_FORMAT] = settings.timeFormat
            prefs[Keys.TIME_ZONE_ID] = settings.timeZoneId
            prefs[Keys.FONT_FAMILY] = settings.fontFamily
            prefs[Keys.FONT_SIZE] = settings.fontSize
            prefs[Keys.ACCENT_COLOR] = settings.accentColor
            prefs[Keys.BACKGROUND_OPACITY] = settings.backgroundOpacity
            prefs[Keys.ADVANCED_LAYOUT_TEMPLATE] = settings.advancedLayoutTemplate
            prefs[Keys.LOADED_PRESET_ID] = settings.loadedPresetId
        }
    }

    suspend fun updateNextAlarm(millis: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NEXT_ALARM_MILLIS] = millis
        }
    }

    suspend fun resetDefaults() {
        updateSettings(WidgetSettings())
    }
}

