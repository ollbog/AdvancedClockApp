package com.advancedclock.app.widget

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.advancedclock.app.data.WidgetSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val nextAlarmMillis = alarmManager.nextAlarmClock?.triggerTime ?: 0L
                WidgetSettingsRepository(context).updateNextAlarm(nextAlarmMillis)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
