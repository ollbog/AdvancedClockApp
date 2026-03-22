package com.advancedclock.app.data

data class WidgetSettings(
    val timeSize: Float = SIZE_LARGE,
    val dateSize: Float = SIZE_NORMAL,
    val dayOfWeekSize: Float = SIZE_OFF,
    val timeZoneSize: Float = SIZE_OFF,
    val weekNumberSize: Float = SIZE_OFF,
    val monthNameSize: Float = SIZE_OFF,
    val nextAlarmSize: Float = SIZE_SMALL,
    val showSeconds: Boolean = false,
    val nextAlarmMillis: Long = 0L,  // 0 = no alarm; set by Application when alarm changes
    val weekRule: String = "locale",  // "locale", "iso", "sunday"
    val dateFormat: String = "EEE, MMM d, yyyy",
    val timeFormat: String = "system",  // "system", "12h", "24h"
    val timeZoneId: String = "",  // empty = system default
    val fontFamily: String = "default",
    val fontSize: Float = 32f,  // base size in sp; large elements render at 1.0×, others scale down
    val accentColor: Long = 0L,  // 0 = use Material You dynamic color
    val backgroundOpacity: Float = 0.5f,
    val advancedLayoutTemplate: String = "\${time:HH:mm:ss;si=L;co=red} - \${date:EEEE, MMM d;si=S;co=Green}\n" +
        "FR \${da:MMMM yyyy, EEEE d;size=15;lc=fr-FR;tz=Europe/Paris;color=yellow}\n" +
        "ES \${da:MMMM yyyy, EEEE d;si=15;locale=es-ES;timezone=Europe/Madrid;co=yellow}\n" +
        "CN \${da:MMMM yyyy, EEEE d;si=15;lc=zh-CN;tz=Asia/Shanghai;co=yellow}\n" +
        "IN \${da:MMMM yyyy, EEEE d;si=15;lc=hi-IN;tz=Asia/Kolkata;co=yellow}\n" +
        "BR \${da:MMMM yyyy, EEEE d;si=15;lc=pt-BR;tz=America/Sao_Paulo;co=yellow}\n" +
        "China:\${ti:HH:mm;tz=Asia/Shanghai}, India:\${ti:HH:mm;tz=Asia/Kolkata}\n" +
        "Bras\u00edl:\${ti:HH:mm;tz=America/Sao_Paulo}, US EST:\${ti:HH:mm;tz=America/New_York}\n" +
        "Week: \${week;co=gray}, Timezone:\${timezone;co=magenta}\n" +
        "\${tx:Next: ;co=orange}\${alarm;si=L;co=#bc75bd}",
    val loadedPresetId: Long = -1L // -1 means no preset or custom changes
) {
    companion object {
        const val SIZE_OFF = 0f
        const val SIZE_SMALL = 0.375f
        const val SIZE_NORMAL = 0.44f
        const val SIZE_LARGE = 1.0f

        /** Convert a legacy string size value to its Float equivalent. */
        fun sizeFromString(value: String): Float = when (value.lowercase()) {
            "small"  -> SIZE_SMALL
            "normal" -> SIZE_NORMAL
            "large"  -> SIZE_LARGE
            else     -> SIZE_OFF
        }
        val DATE_FORMATS = listOf(
            "EEE, MMM d, yyyy" to "Thu, Mar 20, 2026",
            "MMM d, yyyy" to "Mar 20, 2026",
            "d MMM yyyy" to "20 Mar 2026",
            "dd/MM/yyyy" to "20/03/2026",
            "MM/dd/yyyy" to "03/20/2026",
            "yyyy-MM-dd" to "2026-03-20"
        )

        val TIME_FORMATS = listOf(
            "system" to "System Default",
            "12h" to "12-Hour",
            "24h" to "24-Hour"
        )

        val WEEK_RULES = listOf(
            "locale" to "Locale Default",
            "iso" to "ISO 8601 (Mon, Jan 4)",
            "sunday" to "US (Sun, Jan 1)"
        )

        val FONT_FAMILIES = listOf(
            "default" to "System Default",
            "sans-serif" to "Sans Serif",
            "sans-serif-light" to "Sans Serif Light",
            "sans-serif-medium" to "Sans Serif Medium",
            "monospace" to "Monospace",
            "serif" to "Serif"
        )

        val ACCENT_COLORS = listOf(
            0L to "White (Default)",
            0xFFFF0000 to "Red",
            0xFFFF5722 to "Deep Orange",
            0xFFFF9800 to "Orange",
            0xFFFFC107 to "Amber",
            0xFFFFEB3B to "Yellow",
            0xFF8BC34A to "Light Green",
            0xFF4CAF50 to "Green",
            0xFF009688 to "Teal",
            0xFF00BCD4 to "Cyan",
            0xFF03A9F4 to "Light Blue",
            0xFF2196F3 to "Blue",
            0xFF3F51B5 to "Indigo",
            0xFF673AB7 to "Deep Purple",
            0xFF9C27B0 to "Purple",
            0xFFE91E63 to "Pink",
            0xFFB0B0B0 to "Gray"
        )
    }
}
