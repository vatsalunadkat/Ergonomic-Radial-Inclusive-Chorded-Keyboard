package com.vatoo.erick

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class) // Essential fix for compilation
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val selected by vm.selectedLayout.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Keyboard Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Keyboard layout", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            // Logical (A–Z) - enabled and default
            OptionRow(
                title = "Logical (A–Z)",
                subtitle = "Default layout",
                selected = selected == KeyboardLayout.LOGICAL,
                enabled = true,
                onClick = { vm.selectLayout(KeyboardLayout.LOGICAL) }
            )

            Spacer(Modifier.height(8.dp))

            // Efficiency - visible but disabled
            OptionRow(
                title = "Efficiency",
                subtitle = "Coming in Sprint 3",
                selected = selected == KeyboardLayout.EFFICIENCY,
                enabled = false,
                onClick = { /* disabled */ }
            )
        }
    }
}

@Composable
private fun OptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = if (enabled) onClick else null,
            enabled = enabled
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}