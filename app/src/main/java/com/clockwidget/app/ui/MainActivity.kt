package com.advancedclock.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.advancedclock.app.ui.settings.SettingsViewModel
import com.advancedclock.app.ui.settings.SettingsScreen
import com.advancedclock.app.ui.theme.ClockWidgetTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClockWidgetTheme {
                SettingsScreen()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isDirty) viewModel.flush()
    }
}
