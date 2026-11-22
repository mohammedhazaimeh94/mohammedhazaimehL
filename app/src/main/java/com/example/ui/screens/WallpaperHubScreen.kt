package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.WallpaperInterval
import com.example.data.model.WallpaperItem
import com.example.ui.LauncherUiState
import com.example.ui.components.GlassCockpitCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldHealth
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import kotlin.math.roundToInt

@Composable
fun WallpaperHubScreen(
    state: LauncherUiState,
    isWideScreen: Boolean,
    onSelectInterval: (WallpaperInterval) -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onNextWallpaperNow: () -> Unit,
    onRefreshOnline: () -> Unit,
    onPickCustomUri: (Uri) -> Unit,
    onApplySystemWallpaper: () -> Unit,
    onDimChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onPickCustomUri(uri)
            }
        }
    )

    val columns = if (isWideScreen) 3 else 2

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item(span = { GridItemSpan(columns) }) {
            SectionHeaderRow(
                title = stringResource(R.string.wallpapers_title),
                subtitle = stringResource(R.string.wallpapers_subtitle),
                trailing = {
                    OutlinedButton(
                        onClick = onRefreshOnline,
                        enabled = !state.isLoadingWallpapers,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("wallpaper_refresh_online_button")
                    ) {
                        if (state.isLoadingWallpapers) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ElectricCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.wallpapers_refresh_online),
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }
                }
            )
        }

        // Control Card: Auto-Rotation Schedule vs Fixed Static + System Apply + Photo Picker
        item(span = { GridItemSpan(columns) }) {
            GlassCockpitCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = ObsidianSurface.copy(alpha = 0.82f),
                borderColor = ElectricCyan.copy(alpha = 0.35f)
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
                            imageVector = Icons.Rounded.AutoMode,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.wallpapers_mode_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = state.nextRotationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldHealth,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WallpaperInterval.entries, key = { it.name }) { interval ->
                        val selected = state.preferences.wallpaperInterval == interval
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectInterval(interval) },
                            label = {
                                Text(
                                    text = interval.labelAr,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = ObsidianDeep,
                                containerColor = ObsidianCard,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = ObsidianBorder,
                                selectedBorderColor = ElectricCyan
                            ),
                            modifier = Modifier.testTag("wallpaper_interval_${interval.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNextWallpaperNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("wallpaper_next_now_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianDeep
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.wallpapers_next_now),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("wallpaper_pick_device_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ObsidianBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.wallpapers_pick_device),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onApplySystemWallpaper,
                        enabled = !state.isApplyingSystemWallpaper,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wallpaper_apply_system_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, EmeraldHealth.copy(alpha = 0.55f))
                    ) {
                        if (state.isApplyingSystemWallpaper) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = EmeraldHealth
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Wallpaper,
                                contentDescription = null,
                                tint = EmeraldHealth,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.wallpapers_set_system),
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldHealth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Backdrop Dim Slider for Daytime/Nighttime Screen Clarity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wallpapers_dim_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(state.preferences.backdropDimAlpha * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan
                    )
                }
                Slider(
                    value = state.preferences.backdropDimAlpha,
                    onValueChange = onDimChange,
                    valueRange = 0.15f..0.78f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = ObsidianCard
                    )
                )
            }
        }

        // Gallery Header
        item(span = { GridItemSpan(columns) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "الخلفيات المفتوحة المصدر المتاحة (${state.wallpapers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Wallpaper Gallery Cards
        items(state.wallpapers, key = { it.id }) { wallpaper ->
            val isSelected = state.activeWallpaper?.id == wallpaper.id
            WallpaperThumbnailCard(
                wallpaper = wallpaper,
                isSelected = isSelected,
                onClick = { onSelectWallpaper(wallpaper) }
            )
        }
    }
}

@Composable
private fun WallpaperThumbnailCard(
    wallpaper: WallpaperItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("wallpaper_card_${wallpaper.id}"),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianCard,
        border = BorderStroke(
            width = if (isSelected) 2.2.dp else 1.dp,
            color = if (isSelected) ElectricCyan else ObsidianBorder.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                wallpaper.drawableRes != null -> {
                    Image(
                        painter = painterResource(id = wallpaper.drawableRes),
                        contentDescription = wallpaper.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                !wallpaper.imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = wallpaper.imageUrl,
                        contentDescription = wallpaper.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom readability scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ObsidianDeep.copy(alpha = 0.25f),
                                ObsidianDeep.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Top badge: source or selected checkmark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianDeep.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = wallpaper.categoryAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "محددة حالياً",
                            tint = ObsidianDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Title & Open-Source License Attribution
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wallpaper.sourceName} • ${wallpaper.license}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
