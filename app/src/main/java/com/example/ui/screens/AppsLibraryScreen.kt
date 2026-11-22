package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.AppSortOption
import com.example.data.model.InstalledAppInfo
import com.example.ui.LauncherUiState
import com.example.ui.components.AppShortcutTile
import com.example.ui.components.GlassCockpitCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppsLibraryScreen(
    state: LauncherUiState,
    effectiveColumns: Int,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (AppCategory) -> Unit,
    onSelectSort: (AppSortOption) -> Unit,
    onLaunchApp: (String) -> Unit,
    onTogglePin: (String, String) -> Unit,
    onOpenAppDetails: (String) -> Unit,
    onReloadApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var inspectedApp by remember { mutableStateOf<InstalledAppInfo?>(null) }
    val filteredApps = state.filteredInstalledApps
    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale("ar")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderRow(
            title = stringResource(R.string.apps_title),
            subtitle = "${filteredApps.size} من أصل ${state.installedApps.size} تطبيق مثبت على الجهاز",
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box {
                        Surface(
                            onClick = { sortMenuExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianSurface.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("apps_sort_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sort,
                                    contentDescription = "ترتيب",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.sortOption.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            AppSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.labelAr) },
                                    onClick = {
                                        onSelectSort(option)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onReloadApps,
                        modifier = Modifier.testTag("apps_reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "تحديث التطبيقات",
                            tint = ElectricCyan
                        )
                    }
                }
            }
        )

        // Instant Search Field
        OutlinedTextField(
            value = state.appSearchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apps_search_input"),
            placeholder = {
                Text(
                    text = stringResource(R.string.apps_search_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = ElectricCyan
                )
            },
            trailingIcon = {
                if (state.appSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "مسح البحث",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ObsidianSurface.copy(alpha = 0.85f),
                unfocusedContainerColor = ObsidianSurface.copy(alpha = 0.72f),
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = ObsidianBorder
            )
        )

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(AppCategory.entries, key = { it.name }) { category ->
                val selected = state.selectedCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCategory(category) },
                    label = {
                        Text(
                            text = category.labelAr,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = ObsidianDeep,
                        containerColor = ObsidianSurface.copy(alpha = 0.78f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = ObsidianBorder,
                        selectedBorderColor = ElectricCyan
                    ),
                    modifier = Modifier.testTag("apps_category_${category.name.lowercase()}")
                )
            }
        }

        // Quick Action Inspector Bar (when user taps pin/info or inspects)
        inspectedApp?.let { app ->
            val isPinned = app.packageName in state.preferences.pinnedPackages
            GlassCockpitCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = ObsidianCard.copy(alpha = 0.92f),
                borderColor = ElectricCyan.copy(alpha = 0.55f),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${app.label} • v${app.versionName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${app.category.labelAr} • مثبت: ${
                                if (app.firstInstallTime > 0L) dateFormatter.format(Date(app.firstInstallTime))
                                else "النظام"
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            onTogglePin(app.packageName, app.label)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPinned) "إزالة التثبيت" else "تثبيت")
                        }
                        IconButton(onClick = { onOpenAppDetails(app.packageName) }) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = stringResource(R.string.apps_info),
                                tint = ElectricCyan
                            )
                        }
                        IconButton(onClick = { inspectedApp = null }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Apps Grid
        if (state.isLoadingApps) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GlassCockpitCard(containerColor = ObsidianSurface.copy(alpha = 0.78f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = stringResource(R.string.apps_empty_search),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(effectiveColumns.coerceIn(3, 8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isPinned = app.packageName in state.preferences.pinnedPackages
                    AppShortcutTile(
                        app = app,
                        iconScale = state.preferences.iconScale,
                        isPinned = isPinned,
                        showPinBadge = true,
                        onClick = { onLaunchApp(app.packageName) },
                        onSecondaryAction = {
                            inspectedApp = app
                            onTogglePin(app.packageName, app.label)
                        }
                    )
                }
            }
        }
    }
}
