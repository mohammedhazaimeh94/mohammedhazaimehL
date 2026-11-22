package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LauncherTab
import com.example.data.model.ScreenProfile
import com.example.data.model.WallpaperItem
import com.example.ui.screens.AppsLibraryScreen
import com.example.ui.screens.BoostEngineScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OpenSourceUpdatesScreen
import com.example.ui.screens.ScreenAdaptSheet
import com.example.ui.screens.WallpaperHubScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SignalAmber

private data class NavDestination(
    val tab: LauncherTab,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
)

private val launcherDestinations = listOf(
    NavDestination(LauncherTab.HOME, R.string.nav_home, Icons.Rounded.Home, "nav_tab_home"),
    NavDestination(LauncherTab.APPS, R.string.nav_apps, Icons.Rounded.Apps, "nav_tab_apps"),
    NavDestination(LauncherTab.BOOST, R.string.nav_boost, Icons.Rounded.Bolt, "nav_tab_boost"),
    NavDestination(LauncherTab.WALLPAPERS, R.string.nav_wallpapers, Icons.Rounded.Wallpaper, "nav_tab_wallpapers"),
    NavDestination(LauncherTab.UPDATES, R.string.nav_updates, Icons.Rounded.Storefront, "nav_tab_updates")
)

@Composable
fun LauncherShell(
    viewModel: LauncherViewModel,
    onRequestDefaultLauncher: () -> Unit = { viewModel.openDefaultLauncherSettings() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbar()
        }
    }

    // Back button returns cleanly from any sub-tab to Home Dashboard
    BackHandler(enabled = state.activeTab != LauncherTab.HOME) {
        viewModel.selectTab(LauncherTab.HOME)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-Bleed Dynamic Open-Source Wallpaper Backdrop
        DynamicWallpaperBackdrop(
            wallpaper = state.activeWallpaper,
            dimAlpha = state.preferences.backdropDimAlpha,
            turboMode = state.preferences.turboMode
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { _ ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val widthDp = maxWidth
                val heightDp = maxHeight
                val autoWide = widthDp >= 600.dp || widthDp > heightDp
                val isWideLayout = when (state.preferences.screenProfile) {
                    ScreenProfile.AUTO -> autoWide
                    ScreenProfile.CAR_WIDE, ScreenProfile.TV_TABLET -> true
                    ScreenProfile.COMPACT -> false
                }

                val effectiveColumns = if (state.preferences.gridColumns > 0) {
                    state.preferences.gridColumns
                } else {
                    when (state.preferences.screenProfile) {
                        ScreenProfile.CAR_WIDE -> if (widthDp >= 900.dp) 5 else 4
                        ScreenProfile.TV_TABLET -> if (widthDp >= 960.dp) 6 else 5
                        ScreenProfile.COMPACT -> 4
                        ScreenProfile.AUTO -> when {
                            widthDp >= 1000.dp -> 6
                            widthDp >= 720.dp -> 5
                            widthDp >= 520.dp -> 4
                            else -> 3
                        }
                    }
                }

                if (isWideLayout) {
                    // Wide / Landscape / Chinese Car Head Unit / TV Rail Shell
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        SideCockpitRail(
                            activeTab = state.activeTab,
                            updatesBadgeCount = state.availableUpdatesCount,
                            onSelectTab = viewModel::selectTab,
                            onOpenScreenSettings = { viewModel.toggleScreenSettingsSheet(true) }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            ActiveScreenContent(
                                state = state,
                                viewModel = viewModel,
                                isWideScreen = true,
                                effectiveColumns = effectiveColumns,
                                onRequestDefaultLauncher = onRequestDefaultLauncher
                            )
                        }
                    }
                } else {
                    // Compact Portrait Shell with Floating Glass Bottom Bar
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            ActiveScreenContent(
                                state = state,
                                viewModel = viewModel,
                                isWideScreen = false,
                                effectiveColumns = effectiveColumns,
                                onRequestDefaultLauncher = onRequestDefaultLauncher
                            )
                        }

                        BottomGlassNavBar(
                            activeTab = state.activeTab,
                            updatesBadgeCount = state.availableUpdatesCount,
                            onSelectTab = viewModel::selectTab
                        )
                    }
                }
            }
        }

        if (state.showScreenSettingsSheet) {
            ScreenAdaptSheet(
                preferences = state.preferences,
                onSelectProfile = viewModel::setScreenProfile,
                onGridColumnsChange = viewModel::setGridColumns,
                onIconScaleChange = viewModel::setIconScale,
                onTurboModeChange = viewModel::setTurboMode,
                onOpenDefaultLauncher = onRequestDefaultLauncher,
                onDismiss = { viewModel.toggleScreenSettingsSheet(false) }
            )
        }
    }
}

@Composable
private fun ActiveScreenContent(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    isWideScreen: Boolean,
    effectiveColumns: Int,
    onRequestDefaultLauncher: () -> Unit
) {
    val duration = if (state.preferences.turboMode) 0 else 220
    Crossfade(
        targetState = state.activeTab,
        animationSpec = tween(durationMillis = duration),
        label = "launcherTabTransition"
    ) { tab ->
        when (tab) {
            LauncherTab.HOME -> HomeDashboardScreen(
                state = state,
                isWideScreen = isWideScreen,
                effectiveColumns = effectiveColumns,
                onLaunchApp = viewModel::launchApp,
                onManualBoost = viewModel::triggerManualBoost,
                onOpenTab = viewModel::selectTab,
                onNextWallpaper = { viewModel.rotateToNextWallpaper(silent = false) },
                onOpenScreenSettings = { viewModel.toggleScreenSettingsSheet(true) },
                onRequestDefaultLauncher = onRequestDefaultLauncher
            )

            LauncherTab.APPS -> AppsLibraryScreen(
                state = state,
                effectiveColumns = effectiveColumns,
                onSearchChange = viewModel::setAppSearchQuery,
                onSelectCategory = viewModel::selectAppCategory,
                onSelectSort = viewModel::selectAppSort,
                onLaunchApp = viewModel::launchApp,
                onTogglePin = viewModel::togglePinApp,
                onOpenAppDetails = viewModel::openAppDetails,
                onReloadApps = viewModel::reloadInstalledApps
            )

            LauncherTab.BOOST -> BoostEngineScreen(
                state = state,
                isWideScreen = isWideScreen,
                onManualBoost = viewModel::triggerManualBoost,
                onToggleTurboMode = viewModel::setTurboMode,
                onOpenStorageSettings = viewModel::openStorageSettings
            )

            LauncherTab.WALLPAPERS -> WallpaperHubScreen(
                state = state,
                isWideScreen = isWideScreen,
                onSelectInterval = viewModel::setWallpaperInterval,
                onSelectWallpaper = viewModel::selectWallpaper,
                onNextWallpaperNow = { viewModel.rotateToNextWallpaper(silent = false) },
                onRefreshOnline = { viewModel.refreshOnlineWallpapers(silent = false) },
                onPickCustomUri = viewModel::setCustomLocalWallpaper,
                onApplySystemWallpaper = viewModel::applyCurrentWallpaperToSystem,
                onDimChange = viewModel::setBackdropDimAlpha
            )

            LauncherTab.UPDATES -> OpenSourceUpdatesScreen(
                state = state,
                onCheckUpdates = { viewModel.checkOpenSourceUpdates(silent = false) },
                onSearchQueryChange = viewModel::setOpenSourceSearchQuery,
                onExecuteSearch = viewModel::executeOpenSourceSearch,
                onOpenTrustedPage = viewModel::openTrustedOpenSourcePage
            )
        }
    }
}

@Composable
private fun DynamicWallpaperBackdrop(
    wallpaper: WallpaperItem?,
    dimAlpha: Float,
    turboMode: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
    ) {
        Crossfade(
            targetState = wallpaper,
            animationSpec = tween(durationMillis = if (turboMode) 0 else 450),
            label = "wallpaperCrossfade"
        ) { current ->
            when {
                current?.drawableRes != null -> {
                    Image(
                        painter = painterResource(id = current.drawableRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                !current?.localUri.isNullOrBlank() -> {
                    AsyncImage(
                        model = current?.localUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                !current?.imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = current?.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Atmospheric obsidian cockpit vignette for razor-sharp text & widget readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianDeep.copy(alpha = (dimAlpha + 0.12f).coerceIn(0.2f, 0.85f)),
                            ObsidianDeep.copy(alpha = dimAlpha.coerceIn(0.15f, 0.78f)),
                            ObsidianDeep.copy(alpha = (dimAlpha + 0.24f).coerceIn(0.32f, 0.90f))
                        )
                    )
                )
        )
    }
}

@Composable
private fun SideCockpitRail(
    activeTab: LauncherTab,
    updatesBadgeCount: Int,
    onSelectTab: (LauncherTab) -> Unit,
    onOpenScreenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(86.dp)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSurface.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                launcherDestinations.forEach { dest ->
                    val selected = activeTab == dest.tab
                    RailNavItem(
                        destination = dest,
                        selected = selected,
                        badgeCount = if (dest.tab == LauncherTab.UPDATES) updatesBadgeCount else 0,
                        onClick = { onSelectTab(dest.tab) }
                    )
                }
            }

            Surface(
                onClick = onOpenScreenSettings,
                shape = CircleShape,
                color = ObsidianCard,
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("rail_screen_settings_button")
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
    }
}

@Composable
private fun RailNavItem(
    destination: NavDestination,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val label = stringResource(destination.labelRes)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(destination.testTag),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.65f)) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = SignalAmber, contentColor = ObsidianDeep) {
                            Text("$badgeCount")
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = label,
                    tint = if (selected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(23.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BottomGlassNavBar(
    activeTab: LauncherTab,
    updatesBadgeCount: Int,
    onSelectTab: (LauncherTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSurface.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.75f)),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            launcherDestinations.forEach { dest ->
                val selected = activeTab == dest.tab
                val label = stringResource(dest.labelRes)
                val badgeCount = if (dest.tab == LauncherTab.UPDATES) updatesBadgeCount else 0

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectTab(dest.tab) }
                        .testTag(dest.testTag),
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) ElectricCyan.copy(alpha = 0.20f) else Color.Transparent,
                    border = if (selected) BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.55f)) else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge(containerColor = SignalAmber, contentColor = ObsidianDeep) {
                                        Text("$badgeCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = label,
                                tint = if (selected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
