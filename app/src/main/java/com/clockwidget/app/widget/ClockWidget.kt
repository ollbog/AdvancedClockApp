package com.advancedclock.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.advancedclock.app.data.WidgetSettings
import com.advancedclock.app.data.WidgetSettingsRepository
import com.advancedclock.app.ui.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.WeekFields
import java.util.Locale

private fun resolveColorProvider(colorStr: String?, defaultColorInt: Int) =
    ColorProvider(
        day = androidx.compose.ui.graphics.Color(resolveColor(colorStr, defaultColorInt)),
        night = androidx.compose.ui.graphics.Color(resolveColor(colorStr, defaultColorInt))
    )

class ClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = WidgetSettingsRepository.getInstance(context)

        provideContent {
            val settings = repo.settings.collectAsState(initial = WidgetSettings()).value
            GlanceTheme {
                ClockWidgetContent(context, settings, advancedMode = false)
            }
        }
    }
}

class ClockWidgetAdvanced : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = WidgetSettingsRepository.getInstance(context)

        provideContent {
            val settings = repo.settings.collectAsState(initial = WidgetSettings()).value
            GlanceTheme {
                ClockWidgetContent(context, settings, advancedMode = true)
            }
        }
    }
}

class OpenClockAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

class OpenTimeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            val component = alarmIntent.resolveActivity(context.packageManager)
            if (component != null) {
                // If it is specifically the Google DeskClock app, use its hidden deep link URI scheme
                if (component.packageName == "com.google.android.deskclock") {
                    val deepLinkIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("clock-app://com.google.android.deskclock/clock")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(deepLinkIntent)
                    return
                }

                // Fallback for Samsung or other OEM clocks
                val launchIntent = context.packageManager.getLaunchIntentForPackage(component.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    launchIntent.putExtra("deskclock.select.tab", 1)
                    launchIntent.putExtra("clock.select.tab", 1)
                    context.startActivity(launchIntent)
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fallback if we couldn't resolve the pure launch intent
        try {
            val fallbackIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class OpenCalendarAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
                .appendPath(System.currentTimeMillis().toString())
                .build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

@Composable
private fun ClockWidgetContent(context: Context, settings: WidgetSettings, advancedMode: Boolean) {
    val locale = Locale.getDefault()
    val timeZone = remember(settings.timeZoneId) {
        if (settings.timeZoneId.isNotEmpty()) {
            try { ZoneId.of(settings.timeZoneId) } catch (e: Exception) { ZoneId.systemDefault() }
        } else {
            ZoneId.systemDefault()
        }
    }
    val now = ZonedDateTime.now(timeZone)

    val is24Hour = when (settings.timeFormat) {
        "12h" -> false
        "24h" -> true
        else -> DateFormat.is24HourFormat(context)
    }
    val timePatternDefault = when {
        is24Hour && settings.showSeconds -> "HH:mm:ss"
        is24Hour -> "HH:mm"
        settings.showSeconds -> "h:mm:ss a"
        else -> "h:mm a"
    }

    val dateFormat = remember(settings.dateFormat) { DateTimeFormatter.ofPattern(settings.dateFormat, locale) }
    val dayFormat = remember { DateTimeFormatter.ofPattern("EEEE", locale) }
    val monthFormat = remember { DateTimeFormatter.ofPattern("MMMM", locale) }

    val bgOpacity = settings.backgroundOpacity.coerceIn(0f, 1f)
    val bgColor = androidx.compose.ui.graphics.Color(
        red = 0f,
        green = 0f,
        blue = 0f,
        alpha = bgOpacity
    )
    val secondaryTextColor = if (settings.accentColor != 0L) {
        val color = androidx.compose.ui.graphics.Color(settings.accentColor or (0xFF000000)).copy(alpha = 0.7f)
        ColorProvider(day = color, night = color)
    } else {
        ColorProvider(
            day = androidx.compose.ui.graphics.Color(0xFFB0B0B0),
            night = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
        )
    }

    val alarmTimeFormatter = DateTimeFormatter.ofPattern(if (is24Hour) "EEE HH:mm" else "EEE h:mm a", locale)

    val weekField = remember(settings.weekRule) {
        when (settings.weekRule) {
            "iso" -> WeekFields.ISO
            "sunday" -> WeekFields.SUNDAY_START
            else -> WeekFields.of(locale)
        }
    }
    val weekNumber = now.get(weekField.weekOfWeekBasedYear())

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val primaryColorInt = if (settings.accentColor != 0L) {
            (settings.accentColor or 0xFF000000).toInt()
        } else {
            android.graphics.Color.WHITE
        }
        val primaryColorProvider = ColorProvider(
            day = androidx.compose.ui.graphics.Color(primaryColorInt),
            night = androidx.compose.ui.graphics.Color(primaryColorInt)
        )

        if (advancedMode) {
            val parsedLines = remember(settings.advancedLayoutTemplate) {
                LayoutParser.parseTemplate(settings.advancedLayoutTemplate)
            }
            parsedLines.forEach { line ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    line.elements.forEach { element ->
                        when (element) {
                            is ParsedElement.TextNode -> {
                                Text(
                                    text = element.text,
                                    style = TextStyle(color = secondaryTextColor, fontSize = (settings.fontSize * 0.44f).sp)
                                )
                            }
                            is ParsedElement.StyledTextNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.44f)
                                val nodeTextColor = resolveColorProvider(element.colorStr, if (settings.accentColor != 0L) settings.accentColor.toInt() else android.graphics.Color.WHITE)
                                Text(
                                    text = element.text,
                                    style = TextStyle(color = nodeTextColor, fontSize = nodeFontSize.sp)
                                )
                            }
                            is ParsedElement.TimeNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.44f)
                                val nodeColor = resolveColor(element.colorStr, primaryColorInt)
                                val elementTz = element.zoneId ?: timeZone
                                val patternToUse = if (element.pattern.isEmpty()) timePatternDefault else element.pattern
                                val rv = remember(nodeColor, nodeFontSize, elementTz.id, patternToUse) {
                                    android.widget.RemoteViews(context.packageName, com.advancedclock.app.R.layout.widget_text_clock).also {
                                        it.setTextColor(com.advancedclock.app.R.id.text_clock, nodeColor)
                                        it.setTextViewTextSize(com.advancedclock.app.R.id.text_clock, android.util.TypedValue.COMPLEX_UNIT_SP, nodeFontSize)
                                        it.setString(com.advancedclock.app.R.id.text_clock, "setTimeZone", elementTz.id)
                                        it.setCharSequence(com.advancedclock.app.R.id.text_clock, "setFormat12Hour", patternToUse)
                                        it.setCharSequence(com.advancedclock.app.R.id.text_clock, "setFormat24Hour", patternToUse)
                                    }
                                }
                                androidx.glance.appwidget.AndroidRemoteViews(
                                    remoteViews = rv,
                                    modifier = GlanceModifier.clickable(actionRunCallback<OpenTimeAction>())
                                )
                            }
                            is ParsedElement.DateNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.44f)
                                val nodeTextColor = resolveColorProvider(element.colorStr, if (settings.accentColor != 0L) settings.accentColor.toInt() else android.graphics.Color.LTGRAY)

                                val elementTz = element.zoneId ?: timeZone
                                val elementLocale = element.locale ?: locale
                                val patternToUse = if (element.pattern.isEmpty()) settings.dateFormat else element.pattern
                                val text = try {
                                    val formatter = DateTimeFormatter.ofPattern(patternToUse, elementLocale).withZone(elementTz)
                                    formatter.format(now)
                                } catch (e: Exception) {
                                    "[$patternToUse?]"
                                }
                                Text(
                                    text = text,
                                    style = TextStyle(color = nodeTextColor, fontSize = nodeFontSize.sp),
                                    modifier = GlanceModifier.clickable(actionRunCallback<OpenCalendarAction>())
                                )
                            }
                            is ParsedElement.AlarmNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.375f)
                                val nodeTextColor = resolveColorProvider(element.colorStr, if (settings.accentColor != 0L) settings.accentColor.toInt() else android.graphics.Color.LTGRAY)

                                val alarmMillis = settings.nextAlarmMillis
                                if (alarmMillis > 0L) {
                                    val alarmTime = Instant.ofEpochMilli(alarmMillis).atZone(timeZone)
                                    Text(
                                        text = "⏰ ${alarmTimeFormatter.format(alarmTime)}",
                                        style = TextStyle(color = nodeTextColor, fontSize = nodeFontSize.sp),
                                        modifier = GlanceModifier.clickable(actionRunCallback<OpenClockAction>())
                                    )
                                }
                            }
                            is ParsedElement.WeekNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.375f)
                                val nodeTextColor = resolveColorProvider(element.colorStr, if (settings.accentColor != 0L) settings.accentColor.toInt() else android.graphics.Color.LTGRAY)
                                Text(
                                    text = "W$weekNumber",
                                    style = TextStyle(color = nodeTextColor, fontSize = nodeFontSize.sp)
                                )
                            }
                            is ParsedElement.TimezoneNode -> {
                                val nodeFontSize = resolveSize(element.sizeStr, settings.fontSize * 0.375f)
                                val nodeTextColor = resolveColorProvider(element.colorStr, if (settings.accentColor != 0L) settings.accentColor.toInt() else android.graphics.Color.LTGRAY)

                                Text(
                                    text = timeZone.getDisplayName(JavaTextStyle.SHORT, locale),
                                    style = TextStyle(color = nodeTextColor, fontSize = nodeFontSize.sp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            if (settings.timeSize > WidgetSettings.SIZE_OFF) {
                val rv = android.widget.RemoteViews(context.packageName, com.advancedclock.app.R.layout.widget_text_clock_bold)
                rv.setTextColor(com.advancedclock.app.R.id.text_clock, primaryColorInt)
                rv.setTextViewTextSize(com.advancedclock.app.R.id.text_clock, android.util.TypedValue.COMPLEX_UNIT_SP, settings.fontSize * settings.timeSize)
                
                val timeZoneStr = if (settings.timeZoneId.isNotEmpty()) settings.timeZoneId else ZoneId.systemDefault().id
                rv.setString(com.advancedclock.app.R.id.text_clock, "setTimeZone", timeZoneStr)

                rv.setCharSequence(com.advancedclock.app.R.id.text_clock, "setFormat12Hour", timePatternDefault)
                rv.setCharSequence(com.advancedclock.app.R.id.text_clock, "setFormat24Hour", timePatternDefault)

                androidx.glance.appwidget.AndroidRemoteViews(
                    remoteViews = rv,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionRunCallback<OpenTimeAction>())
                )
            }

            if (settings.dayOfWeekSize > WidgetSettings.SIZE_OFF) {
                Text(
                    text = dayFormat.format(now),
                    style = TextStyle(
                        color = primaryColorProvider,
                        fontSize = (settings.fontSize * settings.dayOfWeekSize).sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionRunCallback<OpenCalendarAction>())
                )
            }

            if (settings.monthNameSize > WidgetSettings.SIZE_OFF) {
                Text(
                    text = monthFormat.format(now),
                    style = TextStyle(
                        color = primaryColorProvider,
                        fontSize = (settings.fontSize * settings.monthNameSize).sp
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionRunCallback<OpenCalendarAction>())
                )
            }

            if (settings.dateSize > WidgetSettings.SIZE_OFF) {
                Text(
                    text = dateFormat.format(now),
                    style = TextStyle(
                        color = primaryColorProvider,
                        fontSize = (settings.fontSize * settings.dateSize).sp
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionRunCallback<OpenCalendarAction>())
                )
            }

            if (settings.weekNumberSize > WidgetSettings.SIZE_OFF) {
                Text(
                    text = "W$weekNumber",
                    style = TextStyle(
                        color = primaryColorProvider,
                        fontSize = (settings.fontSize * settings.weekNumberSize).sp
                    )
                )
            }

            if (settings.timeZoneSize > WidgetSettings.SIZE_OFF) {
                Text(
                    text = timeZone.getDisplayName(JavaTextStyle.SHORT, locale),
                    style = TextStyle(
                        color = primaryColorProvider,
                        fontSize = (settings.fontSize * settings.timeZoneSize).sp
                    )
                )
            }

            if (settings.nextAlarmSize > WidgetSettings.SIZE_OFF) {
                val alarmMillis = settings.nextAlarmMillis
                if (alarmMillis > 0L) {
                    val alarmTime = Instant.ofEpochMilli(alarmMillis).atZone(timeZone)
                    Text(
                        text = "⏰ ${alarmTimeFormatter.format(alarmTime)}",
                        style = TextStyle(
                            color = primaryColorProvider,
                            fontSize = (settings.fontSize * settings.nextAlarmSize).sp
                        ),
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .clickable(actionRunCallback<OpenClockAction>())
                    )
                }
            }
        }
    }
}
