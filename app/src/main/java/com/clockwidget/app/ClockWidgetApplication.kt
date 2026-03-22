package com.advancedclock.app

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.advancedclock.app.data.WidgetSettingsRepository
import com.advancedclock.app.widget.SecondTickReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClockWidgetApplication : android.app.Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Dynamic receiver for alarm changes
        val dynamicReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                syncAlarmToDataStore()
            }
        }
        val filter = IntentFilter("android.app.action.NEXT_ALARM_CLOCK_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dynamicReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(dynamicReceiver, filter)
        }

        // ContentObserver as backup
        val alarmObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                syncAlarmToDataStore()
            }
        }
        @Suppress("DEPRECATION")
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.NEXT_ALARM_FORMATTED),
            false,
            alarmObserver
        )

        syncAlarmToDataStore()

        // Note: SecondTickReceiver was removed because TextClock updates seconds natively
    }

    fun syncAlarmToDataStore() {
        appScope.launch {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextAlarmMillis = alarmManager.nextAlarmClock?.triggerTime ?: 0L
            WidgetSettingsRepository.getInstance(this@ClockWidgetApplication).updateNextAlarm(nextAlarmMillis)
        }
    }
}
