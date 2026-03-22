package com.advancedclock.app.widget

import android.graphics.Color

private val colorCache = HashMap<String, Int>(32)

// Helper functions for parsing Macro language styling
fun resolveSize(sizeStr: String?, defaultFontSize: Float): Float {
    if (sizeStr == null) return defaultFontSize

    return when (sizeStr.lowercase()) {
        "s", "small" -> defaultFontSize * 0.75f
        "m", "normal", "medium" -> defaultFontSize * 1.0f
        "l", "large" -> defaultFontSize * 1.5f
        "off" -> 0f
        else -> {
            val exactSize = sizeStr.toFloatOrNull()
            exactSize ?: defaultFontSize
        }
    }
}

fun resolveColor(colorStr: String?, defaultColor: Int): Int {
    if (colorStr == null) return defaultColor
    colorCache[colorStr]?.let { return it }

    val result = try {
        when (colorStr.lowercase()) {
            "red" -> Color.RED
            "green" -> Color.GREEN
            "blue" -> Color.BLUE
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "cyan" -> Color.CYAN
            "magenta" -> Color.MAGENTA
            "yellow" -> Color.YELLOW
            "orange" -> Color.rgb(255, 165, 0)
            "pink" -> Color.rgb(255, 105, 180)
            "purple" -> Color.rgb(128, 0, 128)
            "lime" -> Color.rgb(0, 255, 0)
            "brown" -> Color.rgb(165, 42, 42)
            "gray", "grey" -> Color.GRAY
            "transparent" -> Color.TRANSPARENT
            else -> {
                val formattedStr = if (!colorStr.startsWith("#")) "#$colorStr" else colorStr
                Color.parseColor(formattedStr)
            }
        }
    } catch (e: IllegalArgumentException) {
        defaultColor
    }
    colorCache[colorStr] = result
    return result
}
