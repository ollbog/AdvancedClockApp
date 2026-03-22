package com.advancedclock.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.advancedclock.app.data.Preset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsDialog(
    presets: List<Preset>,
    onDismissRequest: () -> Unit,
    onLoadPreset: (Preset) -> Unit,
    onSaveNewPreset: (String) -> Unit,
    onDuplicatePreset: (Preset, String) -> Unit,
    onDeletePreset: (Preset) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetToDuplicate by remember { mutableStateOf<Preset?>(null) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }
    var newPresetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Saved Presets") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (presets.isEmpty()) {
                    Text(
                        "No presets saved yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(presets, key = { it.id }) { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        onLoadPreset(preset)
                                        onDismissRequest()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(preset.name, maxLines = 1)
                                }
                                
                                IconButton(onClick = {
                                    presetToDuplicate = preset
                                    newPresetName = "${preset.name} (Copy)"
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                
                                IconButton(onClick = { presetToDelete = preset }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                showSaveDialog = true
                newPresetName = ""
            }) {
                Icon(Icons.Default.Add, contentDescription = "Save New")
                Spacer(Modifier.width(8.dp))
                Text("Save Current")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset") },
            text = { Text("Delete \"${preset.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePreset(preset)
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showSaveDialog || presetToDuplicate != null) {
        val title = if (showSaveDialog) "Save Current as Preset" else "Duplicate Preset"
        
        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
                presetToDuplicate = null
            },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            if (showSaveDialog) {
                                onSaveNewPreset(newPresetName)
                            } else {
                                presetToDuplicate?.let { onDuplicatePreset(it, newPresetName) }
                            }
                            showSaveDialog = false
                            presetToDuplicate = null
                        }
                    },
                    enabled = newPresetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        presetToDuplicate = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
