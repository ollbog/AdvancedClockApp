package com.advancedclock.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.advancedclock.app.data.WidgetSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecondTickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val widget = ClockWidget()
                manager.getGlanceIds(ClockWidget::class.java).forEach { glanceId ->
                    widget.update(context, glanceId)
                }
                val widgetAdvanced = ClockWidgetAdvanced()
                manager.getGlanceIds(ClockWidgetAdvanced::class.java).forEach { glanceId ->
                    widgetAdvanced.update(context, glanceId)
                }
                // Only reschedule if showSeconds is still on
                val settings = WidgetSettingsRepository(context).settings.first()
                if (settings.showSeconds) {
                    scheduleNextTick(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION = "com.advancedclock.app.SECOND_TICK"

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SecondTickReceiver::class.java).apply {
                action = ACTION
            }
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextSecond = (System.currentTimeMillis() / 1000 + 1) * 1000
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Permission not granted — use a ~200ms window as fallback
                alarmManager.setWindow(AlarmManager.RTC, nextSecond, 200L, getPendingIntent(context))
            } else {
                alarmManager.setExact(AlarmManager.RTC, nextSecond, getPendingIntent(context))
            }
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntent(context))
        }
    }
}
