package com.advancedclock.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.advancedclock.app.R
import com.advancedclock.app.data.WidgetSettings
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val presets by viewModel.presets.collectAsState()
    var showMacroDocs by remember { mutableStateOf(false) }
    var showAdvancedLayout by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(AlarmManager::class.java) }
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* user returns from system settings; permission may now be granted */ }
    fun requestExactAlarmIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            exactAlarmLauncher.launch(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            )
        }
    }

    if (showMacroDocs) {
        MacroDocsScreen(onBack = { showMacroDocs = false })
        return
    }

    if (showAdvancedLayout) {
        AdvancedLayoutScreen(
            template = settings.advancedLayoutTemplate,
            onTemplateChange = { viewModel.update { copy(advancedLayoutTemplate = it) } },
            onBack = { showAdvancedLayout = false },
            onOpenDocs = { showMacroDocs = true }
        )
        return
    }

    if (showPresetsDialog) {
        PresetsDialog(
            presets = presets,
            onDismissRequest = { showPresetsDialog = false },
            onLoadPreset = { viewModel.loadPreset(it) },
            onSaveNewPreset = { viewModel.saveAsPreset(it) },
            onDuplicatePreset = { preset, name -> viewModel.duplicatePreset(preset, name) },
            onDeletePreset = { viewModel.deletePreset(it) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.settings_title))
                        val currentPreset = presets.find { it.id == settings.loadedPresetId }
                        if (currentPreset != null) {
                            Text(
                                "Loaded: " + currentPreset.name + "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (settings.loadedPresetId != -1L) {
                        IconButton(onClick = { viewModel.updateCurrentPreset() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save to Preset")
                        }
                    }
                    IconButton(onClick = { showPresetsDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Presets")
                    }
                    IconButton(onClick = { viewModel.resetDefaults() }) {
                        Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.reset_defaults))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionHeader("Advanced Layout Mode")
            Text(
                "Used by the Advanced Widget. Edit the template to customise its layout.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    requestExactAlarmIfNeeded()
                    showAdvancedLayout = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Advanced Layout Template →")
            }
            
            Spacer(Modifier.height(8.dp))

            // --- Visibility Toggles ---
                SectionHeader("Display Sizes")
                SizeToggleRow(stringResource(R.string.show_time), settings.timeSize) {
                    viewModel.update { copy(timeSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_date), settings.dateSize) {
                    viewModel.update { copy(dateSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_day_of_week), settings.dayOfWeekSize) {
                    viewModel.update { copy(dayOfWeekSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_timezone), settings.timeZoneSize) {
                    viewModel.update { copy(timeZoneSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_week_number), settings.weekNumberSize) {
                    viewModel.update { copy(weekNumberSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_month_name), settings.monthNameSize) {
                    viewModel.update { copy(monthNameSize = it) }
                }
                SizeToggleRow(stringResource(R.string.show_next_alarm), settings.nextAlarmSize) {
                    viewModel.update { copy(nextAlarmSize = it) }
                }
                SettingToggle(stringResource(R.string.show_seconds), settings.showSeconds) { enabled ->
                    viewModel.update { copy(showSeconds = enabled) }
                    if (enabled) requestExactAlarmIfNeeded()
                }
                Text(
                    "Requires the \"Alarms & Reminders\" permission to tick precisely every second.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
                )

            Spacer(Modifier.height(12.dp))

            // --- Week Rule ---
            SectionHeader(stringResource(R.string.week_rule))
            DropdownSetting(
                label = stringResource(R.string.week_rule),
                selectedKey = settings.weekRule,
                options = WidgetSettings.WEEK_RULES,
                onSelect = { viewModel.update { copy(weekRule = it) } }
            )

            Spacer(Modifier.height(12.dp))

            // --- Time Format ---
            SectionHeader(stringResource(R.string.time_format))
            DropdownSetting(
                label = stringResource(R.string.time_format),
                selectedKey = settings.timeFormat,
                options = WidgetSettings.TIME_FORMATS,
                onSelect = { viewModel.update { copy(timeFormat = it) } }
            )

            Spacer(Modifier.height(12.dp))

            // --- Date Format ---
            SectionHeader(stringResource(R.string.date_format))
            DropdownSetting(
                label = stringResource(R.string.date_format),
                selectedKey = settings.dateFormat,
                options = WidgetSettings.DATE_FORMATS,
                onSelect = { viewModel.update { copy(dateFormat = it) } }
            )

            Spacer(Modifier.height(12.dp))

            // --- Font ---
            SectionHeader(stringResource(R.string.font))
            DropdownSetting(
                label = stringResource(R.string.font),
                selectedKey = settings.fontFamily,
                options = WidgetSettings.FONT_FAMILIES,
                onSelect = { viewModel.update { copy(fontFamily = it) } }
            )

            Spacer(Modifier.height(12.dp))

            // --- Font Size ---
            SectionHeader(stringResource(R.string.font_size))
            Text(
                text = "${settings.fontSize.toInt()} sp",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.fontSize,
                onValueChange = { viewModel.update { copy(fontSize = it) } },
                valueRange = 16f..64f,
                steps = 23
            )

            Spacer(Modifier.height(12.dp))

            // --- Time Zone ---
            SectionHeader(stringResource(R.string.time_zone))
            TimeZonePicker(
                selectedZoneId = settings.timeZoneId,
                onSelect = { viewModel.update { copy(timeZoneId = it) } }
            )

            Spacer(Modifier.height(12.dp))

                // --- Accent Color ---
                SectionHeader(stringResource(R.string.accent_color))
                ColorPickerGrid(
                    selectedColor = settings.accentColor,
                    colors = WidgetSettings.ACCENT_COLORS,
                    onSelect = { viewModel.update { copy(accentColor = it) } }
                )
            Spacer(Modifier.height(12.dp))

            // --- Background Opacity ---
            SectionHeader(stringResource(R.string.background_opacity))
            Text(
                text = "${(settings.backgroundOpacity * 100).toInt()}%",                        style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.backgroundOpacity,
                onValueChange = { viewModel.update { copy(backgroundOpacity = it) } },
                valueRange = 0f..1f,
                steps = 9
            )

            Spacer(Modifier.height(24.dp))

            // --- Build info ---
            Text(
                text = "v${com.advancedclock.app.BuildConfig.VERSION_NAME} (${com.advancedclock.app.BuildConfig.GIT_HASH})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeToggleRow(label: String, selectedSize: Float, onSizeSelected: (Float) -> Unit) {
    val options = listOf(WidgetSettings.SIZE_OFF, WidgetSettings.SIZE_SMALL, WidgetSettings.SIZE_NORMAL, WidgetSettings.SIZE_LARGE)
    val displayNames = listOf("Off", "S", "M", "L")
    val selectedIndex = options.indexOf(selectedSize).takeIf { it >= 0 } ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(1f)
        ) {
            options.forEachIndexed { index, _ ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onSizeSelected(options[index]) },
                    selected = index == selectedIndex
                ) {
                    Text(displayNames[index])
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSetting(
    label: String,
    selectedKey: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.firstOrNull { it.first == selectedKey }?.second ?: selectedKey

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeZonePicker(selectedZoneId: String, onSelect: (String) -> Unit) {
    val zones = remember {
        TimeZone.getAvailableIDs()
            .sorted()
            .map { it to it.replace("_", " ") }
    }
    val displayName = if (selectedZoneId.isEmpty()) "System Default" else selectedZoneId.replace("_", " ")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.time_zone)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("System Default") },
                onClick = {
                    onSelect("")
                    expanded = false
                }
            )
            zones.forEach { (id, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorPickerGrid(
    selectedColor: Long,
    colors: List<Pair<Long, String>>,
    onSelect: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { (colorValue, label) ->
            val displayColor = if (colorValue == 0L) Color.White else Color(colorValue or 0xFF000000)
            val isSelected = selectedColor == colorValue
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(displayColor)
                    .border(3.dp, borderColor, CircleShape)
                    .clickable { onSelect(colorValue) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = label,
                        tint = if (colorValue == 0L || colorValue == 0xFFFFEB3B || colorValue == 0xFFFFC107)
                            Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

