package com.advancedclock.app.widget

import java.util.Locale
import java.time.ZoneId

sealed class ParsedElement {
    data class TextNode(val text: String) : ParsedElement()
    data class StyledTextNode(val text: String, val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
    data class TimeNode(val pattern: String, val locale: Locale?, val zoneId: ZoneId?, val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
    data class DateNode(val pattern: String, val locale: Locale?, val zoneId: ZoneId?, val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
    data class AlarmNode(val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
    data class WeekNode(val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
    data class TimezoneNode(val sizeStr: String? = null, val colorStr: String? = null) : ParsedElement()
}

data class ParsedLine(val elements: List<ParsedElement>)

object LayoutParser {
    private val MACRO_PATTERN = Regex("""\$\{([^}]+)\}""")
    private var cachedTemplate: String? = null
    private var cachedResult: List<ParsedLine>? = null

    fun parseTemplate(template: String): List<ParsedLine> {
        if (template == cachedTemplate) return cachedResult!!
        val lines = template.split("\n")
        val result = lines.map { line ->
            var lastIndex = 0
            val elements = mutableListOf<ParsedElement>()
            val matches = MACRO_PATTERN.findAll(line)
            for (match in matches) {
                val preText = line.substring(lastIndex, match.range.first)
                if (preText.isNotEmpty()) {
                    elements.add(ParsedElement.TextNode(preText))
                }

                val rawContent = match.groupValues[1]

                // Split all parts by semicolon
                val parts = rawContent.split(";").map { it.trim() }
                val cmdPart = parts[0]

                // Parse all named properties (key=value) from the remaining parts
                var sizeStr: String? = null
                var colorStr: String? = null
                var localeStr: String? = null
                var tzStr: String? = null

                for (i in 1 until parts.size) {
                    val keyVal = parts[i].split("=", limit = 2)
                    if (keyVal.size == 2) {
                        val key = keyVal[0].trim().lowercase()
                        val value = keyVal[1].trim()
                        when (key) {
                            "size", "si"            -> sizeStr = value
                            "color", "co"           -> colorStr = value
                            "locale", "lc"          -> localeStr = value
                            "timezone", "tz"        -> tzStr = value
                        }
                    }
                }

                val locale = localeStr?.let { Locale.forLanguageTag(it) }
                val zoneId = tzStr?.let {
                    try { ZoneId.of(it) } catch (e: Exception) { null }
                }

                when {
                    cmdPart.startsWith("ti:") || cmdPart.startsWith("time:") -> {
                        val pattern = cmdPart.substringAfter(":").trim()
                        elements.add(ParsedElement.TimeNode(pattern, locale, zoneId, sizeStr, colorStr))
                    }
                    cmdPart == "ti" || cmdPart == "time" -> {
                        elements.add(ParsedElement.TimeNode("", locale, zoneId, sizeStr, colorStr))
                    }
                    cmdPart.startsWith("da:") || cmdPart.startsWith("date:") -> {
                        val pattern = cmdPart.substringAfter(":").trim()
                        elements.add(ParsedElement.DateNode(pattern, locale, zoneId, sizeStr, colorStr))
                    }
                    cmdPart == "da" || cmdPart == "date" -> {
                        elements.add(ParsedElement.DateNode("", locale, zoneId, sizeStr, colorStr))
                    }
                    cmdPart.startsWith("tx:") || cmdPart.startsWith("text:") -> {
                        val text = cmdPart.substringAfter(":").trim()
                        elements.add(ParsedElement.StyledTextNode(text, sizeStr, colorStr))
                    }
                    cmdPart == "alarm"    -> elements.add(ParsedElement.AlarmNode(sizeStr, colorStr))
                    cmdPart == "week"     -> elements.add(ParsedElement.WeekNode(sizeStr, colorStr))
                    cmdPart == "timezone" -> elements.add(ParsedElement.TimezoneNode(sizeStr, colorStr))
                    else -> elements.add(ParsedElement.TextNode(match.value)) // unknown token, passthrough
                }
                lastIndex = match.range.last + 1
            }
            val postText = line.substring(lastIndex)
            if (postText.isNotEmpty()) {
                elements.add(ParsedElement.TextNode(postText))
            }
            ParsedLine(elements)
        }
        cachedTemplate = template
        cachedResult = result
        return result
    }
}
