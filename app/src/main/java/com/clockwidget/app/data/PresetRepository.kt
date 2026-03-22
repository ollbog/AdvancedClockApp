package com.advancedclock.app.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    
    val allPresets: Flow<List<Preset>> = presetDao.getAllPresets()

    suspend fun savePreset(preset: Preset): Long {
        return presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: Preset) {
        presetDao.deletePreset(preset)
    }

    suspend fun updatePreset(preset: Preset) {
        presetDao.updatePreset(preset)
    }

    suspend fun getPresetById(presetId: Long): Preset? {
        return presetDao.getPresetById(presetId)
    }

    suspend fun duplicatePreset(presetToCopy: Preset, newName: String) {
        val newPreset = presetToCopy.copy(
            id = 0, // 0 lets Room auto-generate a new ID
            name = newName,
            createdAt = System.currentTimeMillis()
        )
        presetDao.insertPreset(newPreset)
    }
}
