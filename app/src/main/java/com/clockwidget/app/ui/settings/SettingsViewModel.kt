package com.advancedclock.app.ui.settings

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.advancedclock.app.data.AppDatabase
import com.advancedclock.app.data.Preset
import com.advancedclock.app.data.PresetRepository
import com.advancedclock.app.data.WidgetSettings
import com.advancedclock.app.data.WidgetSettingsRepository
import com.advancedclock.app.widget.ClockWidgetAdvancedReceiver
import com.advancedclock.app.widget.ClockWidgetReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = WidgetSettingsRepository.getInstance(application)
    private val presetRepo = PresetRepository(AppDatabase.getDatabase(application).presetDao())

    private val _settings = MutableStateFlow(WidgetSettings())
    val settings: StateFlow<WidgetSettings> = _settings

    val presets: StateFlow<List<Preset>> = presetRepo.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _settings.value = repo.settings.first()
        }
    }

    fun update(transform: WidgetSettings.() -> WidgetSettings) {
        _settings.value = _settings.value.transform()
        isDirty = true
        persist()
    }

    fun resetDefaults() {
        _settings.value = WidgetSettings(nextAlarmMillis = _settings.value.nextAlarmMillis)
        isDirty = true
        persist()
    }
    
    fun saveAsPreset(name: String) {
        viewModelScope.launch {
            val newPreset = Preset.fromWidgetSettings(name, _settings.value)
            val insertedId = presetRepo.savePreset(newPreset)
            _settings.value = _settings.value.copy(loadedPresetId = insertedId)
            persist()
        }
    }
    
    fun updateCurrentPreset() {
        val currentSettings = _settings.value
        val presetId = currentSettings.loadedPresetId
        if (presetId != -1L) {
            viewModelScope.launch {
                val existing = presetRepo.getPresetById(presetId)
                if (existing != null) {
                    val updatedPreset = Preset.fromWidgetSettings(existing.name, currentSettings).copy(id = existing.id, createdAt = existing.createdAt)
                    presetRepo.updatePreset(updatedPreset)
                }
            }
        }
    }
    
    fun loadPreset(preset: Preset) {
        _settings.value = preset.toWidgetSettings(_settings.value.nextAlarmMillis)
        isDirty = true
        persist()
    }

    fun duplicatePreset(preset: Preset, newName: String) {
        viewModelScope.launch {
            presetRepo.duplicatePreset(preset, newName)
        }
    }
    
    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            presetRepo.deletePreset(preset)
        }
    }

    @Volatile var isDirty = false
        private set

    private var persistJob: Job? = null

    fun flush() {
        persistJob?.cancel()
        persistJob = viewModelScope.launch { doPersist() }
    }

    private fun persist() {
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(300)
            doPersist()
        }
    }

    private suspend fun doPersist() {
        try {
            repo.updateSettings(_settings.value)
            val context = getApplication<Application>()
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(component)
            if (widgetIds.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    setComponent(component)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                context.sendBroadcast(intent)
            }
            val advancedComponent = ComponentName(context, ClockWidgetAdvancedReceiver::class.java)
            val advancedWidgetIds = appWidgetManager.getAppWidgetIds(advancedComponent)
            if (advancedWidgetIds.isNotEmpty()) {
                val advancedIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    setComponent(advancedComponent)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, advancedWidgetIds)
                }
                context.sendBroadcast(advancedIntent)
            }
        } catch (_: Exception) {
        }
        isDirty = false
    }
}
