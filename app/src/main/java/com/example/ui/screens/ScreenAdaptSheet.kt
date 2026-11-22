package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.LauncherPreferences
import com.example.data.model.ScreenProfile
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenAdaptSheet(
    preferences: LauncherPreferences,
    onSelectProfile: (ScreenProfile) -> Unit,
    onGridColumnsChange: (Int) -> Unit,
    onIconScaleChange: (Float) -> Unit,
    onTurboModeChange: (Boolean) -> Unit,
    onOpenDefaultLauncher: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.screen_settings_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            // Set as Default Home Launcher Button
            Button(
                onClick = onOpenDefaultLauncher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("screen_set_default_launcher_button"),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianDeep
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.screen_set_default_launcher),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Screen Profile Selector
            Text(
                text = stringResource(R.string.screen_profile_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScreenProfile.entries.forEach { profile ->
                    val selected = preferences.screenProfile == profile
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProfile(profile) }
                            .testTag("screen_profile_${profile.name.lowercase()}"),
                        shape = RoundedCornerShape(15.dp),
                        color = if (selected) ElectricCyan.copy(alpha = 0.16f) else ObsidianCard,
                        border = BorderStroke(
                            1.dp,
                            if (selected) ElectricCyan else ObsidianBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.labelAr,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = profile.descAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "محدد",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Grid Columns Density
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.screen_columns_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = if (preferences.gridColumns == 0) "تلقائي" else "${preferences.gridColumns} أعمدة",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
                Slider(
                    value = preferences.gridColumns.toFloat(),
                    onValueChange = { value ->
                        val v = value.roundToInt()
                        onGridColumnsChange(if (v <= 2) 0 else v)
                    },
                    valueRange = 2f..8f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )
            }

            // Icon Scale
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.screen_icon_scale_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${(preferences.iconScale * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
                Slider(
                    value = preferences.iconScale,
                    onValueChange = onIconScaleChange,
                    valueRange = 0.8f..1.3f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )
            }

            // Turbo Mode Shortcut
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.boost_turbo_title),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "استجابة فائقة للأجهزة ذات الذاكرة المحدودة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = preferences.turboMode,
                        onCheckedChange = onTurboModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianDeep,
                            checkedTrackColor = ElectricCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
