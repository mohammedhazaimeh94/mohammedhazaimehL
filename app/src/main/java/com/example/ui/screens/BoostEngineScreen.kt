package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.LauncherUiState
import com.example.ui.components.GlassCockpitCard
import com.example.ui.components.MetricBadgePill
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldHealth
import com.example.ui.theme.LuminousTeal
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SpaceGroteskFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BoostEngineScreen(
    state: LauncherUiState,
    isWideScreen: Boolean,
    onManualBoost: () -> Unit,
    onToggleTurboMode: (Boolean) -> Unit,
    onOpenStorageSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = state.systemMetrics
    val boost = state.boostState
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeaderRow(
            title = stringResource(R.string.boost_title),
            subtitle = stringResource(R.string.boost_subtitle)
        )

        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                BoostGaugeHeroCard(
                    state = state,
                    onManualBoost = onManualBoost,
                    modifier = Modifier.weight(0.48f)
                )
                Column(
                    modifier = Modifier.weight(0.52f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HardwareDiagnosticsCard(state = state)
                    TurboAndStorageCard(
                        turboEnabled = state.preferences.turboMode,
                        onToggleTurbo = onToggleTurboMode,
                        onOpenStorageSettings = onOpenStorageSettings
                    )
                }
            }
        } else {
            BoostGaugeHeroCard(
                state = state,
                onManualBoost = onManualBoost,
                modifier = Modifier.fillMaxWidth()
            )
            HardwareDiagnosticsCard(state = state)
            TurboAndStorageCard(
                turboEnabled = state.preferences.turboMode,
                onToggleTurbo = onToggleTurboMode,
                onOpenStorageSettings = onOpenStorageSettings
            )
        }

        // Last Boost History Card
        if (boost.lastBoostTimestamp > 0L) {
            val formattedTime = SimpleDateFormat("HH:mm • d MMM", Locale("ar"))
                .format(Date(boost.lastBoostTimestamp))
            GlassCockpitCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = ObsidianSurface.copy(alpha = 0.78f),
                borderColor = EmeraldHealth.copy(alpha = 0.4f)
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
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldHealth,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "${stringResource(R.string.boost_last_result)} ($formattedTime)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ذاكرة محررة: ${boost.lastFreedRamMb} MB • مؤقتة: ${boost.lastClearedCacheKb} KB • إجمالي التسريعات: ${boost.totalBoostsCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BoostGaugeHeroCard(
    state: LauncherUiState,
    onManualBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = state.systemMetrics
    val boost = state.boostState
    val animatedScore by animateFloatAsState(
        targetValue = (metrics.responsivenessScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (state.preferences.turboMode) 120 else 550),
        label = "scoreArc"
    )
    val gaugeColor = when {
        metrics.responsivenessScore >= 80 -> EmeraldHealth
        metrics.responsivenessScore >= 60 -> SignalAmber
        else -> AlertCrimson
    }

    GlassCockpitCard(
        modifier = modifier,
        containerColor = ObsidianSurface.copy(alpha = 0.82f),
        borderColor = ElectricCyan.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(186.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 15.dp.toPx()
                    val inset = strokePx / 2f + 4.dp.toPx()
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    val topLeft = Offset(inset, inset)

                    // Background Track Arc
                    drawArc(
                        color = ObsidianCard,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Active Luminous Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ElectricCyan, LuminousTeal, gaugeColor, ElectricCyan)
                        ),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedScore,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${metrics.responsivenessScore}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = gaugeColor
                    )
                    Text(
                        text = stringResource(R.string.home_score_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (metrics.isLowMemory) "ذاكرة منخفضة" else "استجابة ممتازة",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )
                }
            }

            if (boost.isBoosting) {
                Text(
                    text = boost.stepMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricCyan
                )
                LinearProgressIndicator(
                    progress = { boost.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = ElectricCyan,
                    trackColor = ObsidianCard
                )
            }

            Button(
                onClick = onManualBoost,
                enabled = !boost.isBoosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("boost_engine_run_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianDeep
                )
            ) {
                if (boost.isBoosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = ObsidianDeep
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.boost_action_running),
                        style = MaterialTheme.typography.titleSmall
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.boost_action_now),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareDiagnosticsCard(state: LauncherUiState) {
    val m = state.systemMetrics
    GlassCockpitCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ObsidianSurface.copy(alpha = 0.80f)
    ) {
        Text(
            text = "تحليل الموارد المباشر",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${m.deviceModel.ifBlank { "جهاز أندرويد" }} • ${m.androidVersion}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // RAM Progress Bar
        ResourceBarRow(
            label = "الذاكرة العشوائية (RAM)",
            detail = "${m.usedRamMb} MB مستخدم / ${m.totalRamMb} MB (متاح ${m.availableRamMb} MB)",
            percent = m.ramUsagePercent,
            barColor = if (m.ramUsagePercent > 82) SignalAmber else ElectricCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Storage Progress Bar
        ResourceBarRow(
            label = "التخزين الداخلي",
            detail = String.format(
                Locale.US,
                "%.1f GB مستخدم / %.1f GB (متاح %.1f GB)",
                m.usedStorageGb,
                m.totalStorageGb,
                m.freeStorageGb
            ),
            percent = m.storageUsagePercent,
            barColor = if (m.storageUsagePercent > 85) AlertCrimson else EmeraldHealth
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadgePill(
                icon = Icons.Rounded.DeveloperBoard,
                label = "أنوية المعالج",
                value = "${m.cpuCores} Cores",
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            MetricBadgePill(
                icon = Icons.Rounded.Thermostat,
                label = "حرارة النظام",
                value = String.format(Locale.US, "%.1f°C", m.batteryTempC),
                accentColor = if (m.batteryTempC > 42f) SignalAmber else EmeraldHealth,
                modifier = Modifier.weight(1f)
            )
            MetricBadgePill(
                icon = Icons.Rounded.BatteryChargingFull,
                label = "الطاقة",
                value = "${m.batteryLevel}%",
                accentColor = LuminousTeal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResourceBarRow(
    label: String,
    detail: String,
    percent: Int,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = ObsidianCard
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TurboAndStorageCard(
    turboEnabled: Boolean,
    onToggleTurbo: (Boolean) -> Unit,
    onOpenStorageSettings: () -> Unit
) {
    GlassCockpitCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ObsidianSurface.copy(alpha = 0.80f),
        borderColor = if (turboEnabled) ElectricCyan.copy(alpha = 0.55f) else ObsidianBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.boost_turbo_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.boost_turbo_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = turboEnabled,
                onCheckedChange = onToggleTurbo,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDeep,
                    checkedTrackColor = ElectricCyan
                ),
                modifier = Modifier.testTag("boost_turbo_switch")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenStorageSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("boost_storage_settings_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ObsidianBorder)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.boost_storage_settings),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
