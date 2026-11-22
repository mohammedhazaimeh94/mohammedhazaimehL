package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.OpenSourceAppItem
import com.example.ui.LauncherUiState
import com.example.ui.components.GlassCockpitCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldHealth
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun OpenSourceUpdatesScreen(
    state: LauncherUiState,
    onCheckUpdates: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onOpenTrustedPage: (OpenSourceAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Library, 1 = Updates

    val updateCandidates = remember(state.openSourceCatalog) {
        state.openSourceCatalog.filter { it.isInstalled || it.hasUpdate }
    }
    val libraryItems = remember(state.openSourceCatalog, state.openSourceSearchResults, state.openSourceSearchQuery) {
        if (state.openSourceSearchQuery.isNotBlank() && state.openSourceSearchResults.isNotEmpty()) {
            state.openSourceSearchResults
        } else {
            state.openSourceCatalog
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderRow(
            title = stringResource(R.string.updates_title),
            subtitle = stringResource(R.string.updates_subtitle),
            trailing = {
                Button(
                    onClick = onCheckUpdates,
                    enabled = !state.isCheckingUpdates,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianDeep
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("updates_scan_button")
                ) {
                    if (state.isCheckingUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ObsidianDeep
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.updates_scan_now),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )

        // Trust & Zero-Login Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = EmeraldHealth.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, EmeraldHealth.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = EmeraldHealth,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.updates_no_login_badge) + " • F-Droid Official API",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldHealth,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Sub-Tab Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubTabChip(
                label = stringResource(R.string.updates_tab_store),
                count = libraryItems.size,
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("updates_subtab_store")
            )
            SubTabChip(
                label = stringResource(R.string.updates_tab_check),
                count = state.availableUpdatesCount,
                highlightCount = state.availableUpdatesCount > 0,
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("updates_subtab_check")
            )
        }

        if (selectedSubTab == 0) {
            // Live F-Droid Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.openSourceSearchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fdroid_search_input"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.updates_search_hint),
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
                        if (state.openSourceSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "مسح"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onExecuteSearch() }),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianSurface.copy(alpha = 0.85f),
                        unfocusedContainerColor = ObsidianSurface.copy(alpha = 0.72f),
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = ObsidianBorder
                    )
                )

                Button(
                    onClick = onExecuteSearch,
                    enabled = !state.isSearchingStore,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("fdroid_search_submit_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianDeep
                    )
                ) {
                    if (state.isSearchingStore) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ObsidianDeep
                        )
                    } else {
                        Text("بحث")
                    }
                }
            }
        }

        val activeList = if (selectedSubTab == 0) libraryItems else updateCandidates

        if (activeList.isEmpty()) {
            GlassCockpitCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = ObsidianSurface.copy(alpha = 0.78f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldHealth,
                        modifier = Modifier.size(38.dp)
                    )
                    Text(
                        text = if (selectedSubTab == 1)
                            "لا توجد تطبيقات مفتوحة المصدر مثبتة تحتاج لتحديث حالياً"
                        else
                            "لا توجد عناصر حالياً — جرب البحث في F-Droid",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedSubTab == 1) {
                        OutlinedButton(
                            onClick = { selectedSubTab = 0 },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Text("تصفح مكتبة التطبيقات المفتوحة", color = ElectricCyan)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                items(activeList, key = { it.packageName }) { item ->
                    OpenSourceAppRowCard(
                        item = item,
                        onOpenTrustedPage = { onOpenTrustedPage(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubTabChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightCount: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) ElectricCyan else ObsidianSurface.copy(alpha = 0.78f),
        border = BorderStroke(
            1.dp,
            if (selected) ElectricCyan else ObsidianBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) ObsidianDeep else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    highlightCount && !selected -> SignalAmber
                    selected -> ObsidianDeep.copy(alpha = 0.18f)
                    else -> ObsidianCard
                }
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                    color = when {
                        highlightCount && !selected -> ObsidianDeep
                        selected -> ObsidianDeep
                        else -> ElectricCyan
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun OpenSourceAppRowCard(
    item: OpenSourceAppItem,
    onOpenTrustedPage: () -> Unit
) {
    val borderColor = when {
        item.hasUpdate -> SignalAmber.copy(alpha = 0.7f)
        item.isInstalled -> EmeraldHealth.copy(alpha = 0.45f)
        else -> ObsidianBorder.copy(alpha = 0.6f)
    }

    GlassCockpitCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("oss_app_card_${item.packageName}"),
        containerColor = ObsidianSurface.copy(alpha = 0.82f),
        borderColor = borderColor,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianCard),
                contentAlignment = Alignment.Center
            ) {
                if (!item.iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.iconUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = item.name,
                        tint = ElectricCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianCard
                    ) {
                        Text(
                            text = item.categoryAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = item.arabicSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val versionLine = buildString {
                    if (item.installedVersionName != null) {
                        append("المثبت: v${item.installedVersionName}")
                    }
                    if (item.latestVersionName != null) {
                        if (isNotEmpty()) append(" • ")
                        append("الأحدث في F-Droid: v${item.latestVersionName}")
                    }
                    if (isEmpty()) {
                        append(item.packageName)
                    }
                }
                Text(
                    text = versionLine,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                    color = if (item.hasUpdate) SignalAmber else EmeraldHealth,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onOpenTrustedPage,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        item.hasUpdate -> SignalAmber
                        item.isInstalled -> EmeraldHealth
                        else -> ElectricCyan
                    },
                    contentColor = ObsidianDeep
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("oss_action_${item.packageName}")
            ) {
                Icon(
                    imageVector = when {
                        item.hasUpdate -> Icons.Rounded.SystemUpdate
                        item.isInstalled -> Icons.Rounded.OpenInNew
                        else -> Icons.Rounded.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = when {
                        item.hasUpdate -> "تحديث"
                        item.isInstalled -> "محدث"
                        else -> "تحميل"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
