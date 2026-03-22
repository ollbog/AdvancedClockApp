package com.advancedclock.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "presets", indices = [Index(value = ["createdAt"])])
data class Preset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    
    // Core appearance
    val timeSize: Float,
    val dateSize: Float,
    val dayOfWeekSize: Float,
    val timeZoneSize: Float,
    val weekNumberSize: Float,
    val monthNameSize: Float,
    val nextAlarmSize: Float,
    val showSeconds: Boolean,
    
    // Formatting & Localization
    val weekRule: String,
    val dateFormat: String,
    val timeFormat: String,
    val timeZoneId: String,
    
    // Style
    val fontFamily: String,
    val fontSize: Float,
    val accentColor: Long,
    val backgroundOpacity: Float,
    
    // Advanced Customization
    val advancedLayoutTemplate: String,
    
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toWidgetSettings(currentAlarmMillis: Long): WidgetSettings {
        return WidgetSettings(
            timeSize = timeSize,
            dateSize = dateSize,
            dayOfWeekSize = dayOfWeekSize,
            timeZoneSize = timeZoneSize,
            weekNumberSize = weekNumberSize,
            monthNameSize = monthNameSize,
            nextAlarmSize = nextAlarmSize,
            showSeconds = showSeconds,
            nextAlarmMillis = currentAlarmMillis, // Retain current alarm state
            weekRule = weekRule,
            dateFormat = dateFormat,
            timeFormat = timeFormat,
            timeZoneId = timeZoneId,
            fontFamily = fontFamily,
            fontSize = fontSize,
            accentColor = accentColor,
            backgroundOpacity = backgroundOpacity,
            advancedLayoutTemplate = advancedLayoutTemplate,
            loadedPresetId = id
        )
    }

    companion object {
        fun fromWidgetSettings(name: String, settings: WidgetSettings): Preset {
            return Preset(
                name = name,
                timeSize = settings.timeSize,
                dateSize = settings.dateSize,
                dayOfWeekSize = settings.dayOfWeekSize,
                timeZoneSize = settings.timeZoneSize,
                weekNumberSize = settings.weekNumberSize,
                monthNameSize = settings.monthNameSize,
                nextAlarmSize = settings.nextAlarmSize,
                showSeconds = settings.showSeconds,
                weekRule = settings.weekRule,
                dateFormat = settings.dateFormat,
                timeFormat = settings.timeFormat,
                timeZoneId = settings.timeZoneId,
                fontFamily = settings.fontFamily,
                fontSize = settings.fontSize,
                accentColor = settings.accentColor,
                backgroundOpacity = settings.backgroundOpacity,
                advancedLayoutTemplate = settings.advancedLayoutTemplate
            )
        }
    }
}
