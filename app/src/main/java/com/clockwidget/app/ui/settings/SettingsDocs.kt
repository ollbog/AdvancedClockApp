package com.advancedclock.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroDocsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Macro Language Guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Advanced Layout Mode lets you create fully custom widgets by blending standard text with dynamic macros.", style = MaterialTheme.typography.bodyLarge)

            DocSection("Basic Data Tags")
            DocExample("\${ti:HH:mm}  or  \${time:HH:mm}", "Shows the current time. Format follows Java DateTimeFormatter.")
            DocExample("\${da:EEEE, MMM d}  or  \${date:EEEE, MMM d}", "Shows the date.")
            DocExample("\${tx:Next: }  or  \${text:Next: }", "Static text with optional size and color styling.")
            DocExample("\${alarm}", "Shows the next scheduled alarm.")
            DocExample("\${week}", "Shows the current week number (e.g. W42).")
            DocExample("\${timezone}", "Shows the short Time Zone ID.")

            DocSection("Properties (Size, Color, Locale, Timezone)")
            Text("Add any property after the command using semicolons. All properties are optional and order-independent.", style = MaterialTheme.typography.bodyMedium)

            DocExample("\${ti:HH:mm; size=L; color=red}", "Large red time.")
            DocExample("\${ti:HH:mm; si=L; co=red}", "Same — 2-char aliases work too.")
            DocExample("\${da:EEEE; size=18; color=#00FF00}", "Date at exactly 18sp, hex green.")
            DocExample("\${alarm; co=yellow}", "Alarm in yellow.")
            DocExample("\${tx:Next: ; si=S; co=#bc75bd}\${alarm; si=L; co=#bc75bd}", "Styled label + alarm in matching color.")

            Text("Size: size= or si=\nValues: S, M, L, off, or any number (e.g. 24)", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("Color: color= or co=\nNamed: red, green, blue, white, black, cyan, magenta, yellow, gray, transparent\nHex: #FF0000, #AARRGGBB", style = MaterialTheme.typography.bodySmall)

            DocSection("Locale & Timezone")
            Text("Override the locale or timezone for any time or date macro.", style = MaterialTheme.typography.bodyMedium)

            DocExample("\${ti:HH:mm; tz=America/New_York}", "New York time.")
            DocExample("\${ti:HH:mm; timezone=America/New_York}", "Same — long alias.")
            DocExample("\${da:EEEE; locale=fr-FR; tz=Europe/Paris}", "French date in Paris time.")
            DocExample("\${time:h:mm a; lc=ja-JP; tz=Asia/Tokyo; si=S; co=cyan}", "Tokyo time, Japanese locale, small cyan.")

            Text("Timezone: tz= or timezone=  (IANA IDs, e.g. Europe/London, UTC)", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("Locale: locale= or lc=  (BCP 47 tags, e.g. fr-FR, de-DE, ja-JP)", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DocSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun DocExample(code: String, description: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(code, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}