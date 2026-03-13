package com.vatoo.erick

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vatoo.erick.shared.ColorPaletteType
import com.vatoo.erick.shared.ColorPalettes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    layoutPreferences: LayoutPreferences,
    onClose: () -> Unit
) {
    val layoutType    by preferencesManager.layoutType.collectAsState(initial = PreferencesManager.LAYOUT_LOGICAL)
    val darkTheme     by preferencesManager.darkTheme.collectAsState(initial = false)
    val colorPalette  by preferencesManager.colorPalette.collectAsState(initial = ColorPaletteType.DEFAULT)
    val leftHandedMode by preferencesManager.leftHandedMode.collectAsState(initial = false)

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keyboard Settings") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Layout ────────────────────────────────────────────────────────
            Text(
                text = "Keyboard Layout",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LayoutRadioOption(
                title = "Logical (A–Z)",
                subtitle = null,
                selected = layoutType == PreferencesManager.LAYOUT_LOGICAL,
                enabled = true,
                onClick = { scope.launch { preferencesManager.setLayoutType(PreferencesManager.LAYOUT_LOGICAL) } }
            )

            LayoutRadioOption(
                title = "Efficiency",
                subtitle = "Optimized for English letter frequency",
                selected = layoutType == PreferencesManager.LAYOUT_EFFICIENCY,
                enabled = true,
                onClick = { scope.launch { preferencesManager.setLayoutType(PreferencesManager.LAYOUT_EFFICIENCY) } }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ── Appearance ────────────────────────────────────────────────────
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingToggle(
                title = "Dark Theme",
                checked = darkTheme,
                enabled = true,
                onCheckedChange = { scope.launch { preferencesManager.setDarkTheme(it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ── Accessibility ─────────────────────────────────────────────────
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Color Palette",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Palette options
            ColorPaletteType.entries.forEach { paletteType ->
                ColorPaletteOption(
                    paletteType = paletteType,
                    isSelected = colorPalette == paletteType,
                    onClick = { scope.launch { preferencesManager.setColorPalette(paletteType) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingToggle(
                title = "Left-Handed Mode",
                checked = leftHandedMode,
                enabled = true,
                onCheckedChange = { scope.launch { preferencesManager.setLeftHandedMode(it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ── Privacy & Security ────────────────────────────────────────────
            Text(
                text = "Privacy & Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Your privacy is our priority. ERICKeyboard:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "✓ Does NOT collect any text you type\n" +
                                "✓ Does NOT store passwords or personal data\n" +
                                "✓ Does NOT transmit any data from your device\n" +
                                "✓ Only stores your keyboard preferences locally\n" +
                                "✓ Has no internet permissions\n" +
                                "✓ Is 100% open source for full transparency",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ── Color palette row ─────────────────────────────────────────────────────────

@Composable
private fun ColorPaletteOption(
    paletteType: ColorPaletteType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = when (paletteType) {
        ColorPaletteType.DEFAULT      -> "Default"
        ColorPaletteType.OKABE_ITO    -> "Okabe-Ito (Universal)"
        ColorPaletteType.DEUTERANOPIA -> "Deuteranopia (Green-blind)"
        ColorPaletteType.PROTANOPIA   -> "Protanopia (Red-blind)"
        ColorPaletteType.TRITANOPIA   -> "Tritanopia (Blue-blind)"
    }

    val colors = ColorPalettes.getPalette(paletteType)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 8 color swatches
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                colors.forEach { entry ->
                    val hex = entry.hexColor
                    val color = Color(
                        red   = ((hex shr 16) and 0xFF).toFloat() / 255f,
                        green = ((hex shr 8)  and 0xFF).toFloat() / 255f,
                        blue  = (hex          and 0xFF).toFloat() / 255f,
                        alpha = 1f
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }
        }
    }
}

// ── Reusable components (unchanged) ──────────────────────────────────────────

@Composable
fun LayoutRadioOption(
    title: String,
    subtitle: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}