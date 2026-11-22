package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.LauncherTab
import com.example.ui.LauncherUiState
import com.example.ui.components.AppShortcutTile
import com.example.ui.components.GlassCockpitCard
import com.example.ui.components.MetricBadgePill
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldHealth
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun HomeDashboardScreen(
    state: LauncherUiState,
    isWideScreen: Boolean,
    effectiveColumns: Int,
    onLaunchApp: (String) -> Unit,
    onManualBoost: () -> Unit,
    onOpenTab: (LauncherTab) -> Unit,
    onNextWallpaper: () -> Unit,
    onOpenScreenSettings: () -> Unit,
    onRequestDefaultLauncher: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        // Landscape / Chinese Car Head Unit / Tablet 2-Pane Cockpit Layout
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!state.isDefaultLauncher) {
                    DefaultLauncherPromptBanner(onClick = onRequestDefaultLauncher)
                }
                CockpitClockAndBoostCard(
                    state = state,
                    onManualBoost = onManualBoost,
                    onOpenBoostTab = { onOpenTab(LauncherTab.BOOST) },
                    onOpenScreenSettings = onOpenScreenSettings
                )
                QuickStatusActionRow(
                    state = state,
                    onNextWallpaper = onNextWallpaper,
                    onOpenWallpapers = { onOpenTab(LauncherTab.WALLPAPERS) },
                    onOpenUpdates = { onOpenTab(LauncherTab.UPDATES) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PinnedAppsHeader(
                    pinnedCount = state.pinnedApps.size,
                    onOpenAllApps = { onOpenTab(LauncherTab.APPS) }
                )
                PinnedAppsGridPanel(
                    state = state,
                    columns = effectiveColumns.coerceIn(3, 5),
                    onLaunchApp = onLaunchApp,
                    onOpenAllApps = { onOpenTab(LauncherTab.APPS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        // Portrait / Compact Screen Vertical Layout
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!state.isDefaultLauncher) {
                DefaultLauncherPromptBanner(onClick = onRequestDefaultLauncher)
            }

            CockpitClockAndBoostCard(
                state = state,
                onManualBoost = onManualBoost,
                onOpenBoostTab = { onOpenTab(LauncherTab.BOOST) },
                onOpenScreenSettings = onOpenScreenSettings
            )

            QuickStatusActionRow(
                state = state,
                onNextWallpaper = onNextWallpaper,
                onOpenWallpapers = { onOpenTab(LauncherTab.WALLPAPERS) },
                onOpenUpdates = { onOpenTab(LauncherTab.UPDATES) }
            )

            PinnedAppsHeader(
                pinnedCount = state.pinnedApps.size,
                onOpenAllApps = { onOpenTab(LauncherTab.APPS) }
            )

            PinnedAppsGridPanel(
                state = state,
                columns = effectiveColumns.coerceIn(3, 4),
                onLaunchApp = onLaunchApp,
                onOpenAllApps = { onOpenTab(LauncherTab.APPS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DefaultLauncherPromptBanner(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_default_launcher_banner"),
        shape = RoundedCornerShape(16.dp),
        color = SignalAmber.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = SignalAmber,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "تعيين مدار لانشر كمشغّل افتراضي",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "اضغط لتفعيل اللانشر تلقائياً عند تشغيل الشاشة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SignalAmber
            ) {
                Text(
                    text = "تفعيل",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ObsidianDeep,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CockpitClockAndBoostCard(
    state: LauncherUiState,
    onManualBoost: () -> Unit,
    onOpenBoostTab: () -> Unit,
    onOpenScreenSettings: () -> Unit
) {
    val metrics = state.systemMetrics
    val boost = state.boostState
    val healthColor = when {
        metrics.responsivenessScore >= 80 -> EmeraldHealth
        metrics.responsivenessScore >= 60 -> SignalAmber
        else -> MaterialTheme.colorScheme.error
    }

    GlassCockpitCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ObsidianSurface.copy(alpha = 0.80f),
        borderColor = ElectricCyan.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = state.currentTimeText.ifBlank { "--:--" },
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.currentSecondsText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = ElectricCyan,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Text(
                    text = state.currentDateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenScreenSettings)
                    .testTag("home_screen_settings_button"),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianCard.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.home_edit_screen),
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Telemetry Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadgePill(
                icon = Icons.Rounded.Speed,
                label = stringResource(R.string.home_score_label),
                value = "${metrics.responsivenessScore}%",
                accentColor = healthColor,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenBoostTab)
            )
            MetricBadgePill(
                icon = Icons.Rounded.Memory,
                label = stringResource(R.string.home_ram_label),
                value = "${metrics.ramUsagePercent}%",
                accentColor = if (metrics.ramUsagePercent > 80) SignalAmber else ElectricCyan,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenBoostTab)
            )
            MetricBadgePill(
                icon = Icons.Rounded.SdStorage,
                label = stringResource(R.string.home_storage_label),
                value = "${metrics.storageUsagePercent}%",
                accentColor = EmeraldHealth,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenBoostTab)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual One-Tap Acceleration CTA
        Button(
            onClick = onManualBoost,
            enabled = !boost.isBoosting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("home_quick_boost_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = ObsidianDeep,
                disabledContainerColor = ElectricCyan.copy(alpha = 0.5f),
                disabledContentColor = ObsidianDeep
            )
        ) {
            if (boost.isBoosting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.4.dp,
                    color = ObsidianDeep
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = boost.stepMessage.ifBlank { stringResource(R.string.home_boosting) },
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.preferences.turboMode) "تسريع فوري • وضع الأداء الفائق نشط"
                    else stringResource(R.string.boost_action_now),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (boost.isBoosting) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { boost.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = ElectricCyan,
                trackColor = ObsidianCard
            )
        }
    }
}

@Composable
private fun QuickStatusActionRow(
    state: LauncherUiState,
    onNextWallpaper: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenUpdates: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Wallpaper Live Status & Instant Next Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenWallpapers)
                .testTag("home_wallpaper_card"),
            shape = RoundedCornerShape(18.dp),
            color = ObsidianSurface.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricCyan.copy(alpha = 0.25f), EmeraldHealth.copy(alpha = 0.25f))
                            )
                        )
                        .clickable(onClick = onNextWallpaper)
                        .testTag("home_next_wallpaper_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wallpaper,
                        contentDescription = stringResource(R.string.wallpapers_next_now),
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.preferences.wallpaperInterval.shortLabelAr,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.activeWallpaper?.title ?: "خلفيات مفتوحة المصدر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Open-Source Updates Live Status Card
        val updatesCount = state.availableUpdatesCount
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenUpdates)
                .testTag("home_updates_card"),
            shape = RoundedCornerShape(18.dp),
            color = ObsidianSurface.copy(alpha = 0.78f),
            border = BorderStroke(
                1.dp,
                if (updatesCount > 0) SignalAmber.copy(alpha = 0.65f)
                else ObsidianBorder.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (updatesCount > 0) SignalAmber.copy(alpha = 0.22f)
                            else EmeraldHealth.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = stringResource(R.string.nav_updates),
                        tint = if (updatesCount > 0) SignalAmber else EmeraldHealth,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (updatesCount > 0) "$updatesCount تحديث مفتوح" else "F-Droid بدون حساب",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.isCheckingUpdates) "جاري فحص التحديثات…"
                        else "مكتبة وتحديثات موثوقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedAppsHeader(
    pinnedCount: Int,
    onOpenAllApps: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${stringResource(R.string.home_pinned_title)} ($pinnedCount)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        OutlinedButton(
            onClick = onOpenAllApps,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
            modifier = Modifier.testTag("home_open_apps_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ElectricCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "كل التطبيقات",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun PinnedAppsGridPanel(
    state: LauncherUiState,
    columns: Int,
    onLaunchApp: (String) -> Unit,
    onOpenAllApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinned = state.pinnedApps
    if (pinned.isEmpty()) {
        GlassCockpitCard(
            modifier = modifier.fillMaxWidth(),
            containerColor = ObsidianSurface.copy(alpha = 0.72f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(38.dp)
                )
                Text(
                    text = stringResource(R.string.home_no_pinned),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onOpenAllApps,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianDeep
                    )
                ) {
                    Text(text = stringResource(R.string.home_open_apps))
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(pinned, key = { it.packageName }) { app ->
                AppShortcutTile(
                    app = app,
                    iconScale = state.preferences.iconScale,
                    isPinned = true,
                    showPinBadge = false,
                    onClick = { onLaunchApp(app.packageName) }
                )
            }
        }
    }
}
