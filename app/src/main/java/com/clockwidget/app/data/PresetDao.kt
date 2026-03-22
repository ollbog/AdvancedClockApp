package com.advancedclock.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<Preset>>

    @Insert
    suspend fun insertPreset(preset: Preset): Long

    @Delete
    suspend fun deletePreset(preset: Preset)

    @Update
    suspend fun updatePreset(preset: Preset)

    @Query("SELECT * FROM presets WHERE id = :presetId LIMIT 1")
    suspend fun getPresetById(presetId: Long): Preset?
}
