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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedLayoutScreen(
    template: String,
    onTemplateChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenDocs: () -> Unit
) {
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Layout") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Used by the Advanced Widget. Each line becomes a row in the widget.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = template,
                onValueChange = onTemplateChange,
                label = { Text("Layout Template") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Text(
                "Use \${ti:HH:mm} for time, \${da:EEEE} for date, \${alarm}, \${week}, \${timezone}, \${tx:label} for styled text.\nProperties: si= size, co= color, lc= locale, tz= timezone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val linkColor = MaterialTheme.colorScheme.primary
            val annotatedLink = androidx.compose.ui.text.buildAnnotatedString {
                append("See ")
                pushLink(androidx.compose.ui.text.LinkAnnotation.Url("https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"))
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        color = linkColor,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                ) {
                    append("DateTimeFormatter formatting rules")
                }
                pop()
                append(" for full pattern syntax.")
            }
            Text(text = annotatedLink, style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = onOpenDocs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Macro Documentation")
            }
        }
    }
}
