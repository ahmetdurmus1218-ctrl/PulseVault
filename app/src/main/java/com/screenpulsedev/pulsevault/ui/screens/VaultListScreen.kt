package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItem
import com.screenpulsedev.pulsevault.ui.components.CardStack
import com.screenpulsedev.pulsevault.ui.theme.rememberPressScale

private enum class VaultTab(val label: String) {
    ALL("Tümü"), REAL("Gerçek"), VIRTUAL("Sanal"), FAVORITES("Favoriler"),
    ACCOUNTS("Hesaplar"), PASSWORDS("Şifreler"), NOTES("Notlar")
}

private enum class SortMode(val label: String) {
    BANK_AZ("Banka (A-Z)"), LABEL_AZ("Etiket (A-Z)"), NEWEST("Yeni Eklenen"), OLDEST("Eski Eklenen")
}

@Composable
fun VaultListScreen(
    items: List<VaultItem>,
    onAddClick: () -> Unit,
    onItemClick: (VaultItem) -> Unit,
    onSettingsClick: () -> Unit
) {
    val (fabScale, fabInteraction) = rememberPressScale(pressedScale = 0.9f)
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(VaultTab.ALL) }
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val searched = remember(items, query) {
        if (query.isBlank()) items
        else items.filter {
            it.label.contains(query, ignoreCase = true) || it.bank.contains(query, ignoreCase = true)
        }
    }

    val tabbed = remember(searched, selectedTab) {
        when (selectedTab) {
            VaultTab.ALL -> searched
            VaultTab.REAL -> searched.filter {
                (it.category == VaultCategory.CREDIT_CARD || it.category == VaultCategory.BANK_ACCOUNT) && !it.isVirtual
            }
            VaultTab.VIRTUAL -> searched.filter { it.isVirtual }
            VaultTab.FAVORITES -> searched.filter { it.isFavorite }
            VaultTab.ACCOUNTS -> searched.filter { it.category == VaultCategory.BANK_ACCOUNT }
            VaultTab.PASSWORDS -> searched.filter { it.category == VaultCategory.PASSWORD }
            VaultTab.NOTES -> searched.filter { it.category == VaultCategory.NOTE }
        }
    }

    val filtered = remember(tabbed, sortMode) {
        when (sortMode) {
            SortMode.BANK_AZ -> tabbed.sortedBy { it.bank.ifBlank { "\uFFFF" } }
            SortMode.LABEL_AZ -> tabbed.sortedBy { it.label.lowercase() }
            SortMode.NEWEST -> tabbed.sortedByDescending { it.createdAt }
            SortMode.OLDEST -> tabbed.sortedBy { it.createdAt }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("PulseVault", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        Box {
                            IconButton(onClick = { sortMenuOpen = true }) {
                                Icon(Icons.Filled.SortByAlpha, contentDescription = "Sırala")
                            }
                            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                                SortMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.label) },
                                        onClick = { sortMode = mode; sortMenuOpen = false }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
                        }
                    }
                )
                if (items.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        edgePadding = 12.dp
                    ) {
                        VaultTab.entries.forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                interactionSource = fabInteraction,
                modifier = fabScale,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ekle")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (items.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Ara...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (items.isEmpty()) {
                EmptyState()
            } else if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sonuç bulunamadı", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val cardItems = filtered.filter {
                    it.category == VaultCategory.CREDIT_CARD || it.category == VaultCategory.BANK_ACCOUNT
                }
                val otherItems = filtered.filter {
                    it.category != VaultCategory.CREDIT_CARD && it.category != VaultCategory.BANK_ACCOUNT
                }

                LazyColumn {
                    if (cardItems.isNotEmpty()) {
                        item(key = "card_stack") {
                            CardStack(
                                items = cardItems,
                                onItemClick = onItemClick,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    itemsIndexed(otherItems, key = { _, item -> item.id }) { index, item ->
                        AnimatedCardEntry(delayMillis = index * 60) {
                            SimpleEntryRow(item = item, onClick = { onItemClick(item) })
                        }
                    }
                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Henüz kayıt yok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Sağ alttaki + ile ilk kaydını ekle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnimatedCardEntry(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 4 }
    ) {
        content()
    }
}

@Composable
private fun SimpleEntryRow(item: VaultItem, onClick: () -> Unit) {
    val (scale, interaction) = rememberPressScale()
    val (icon: ImageVector, tint: Color) = when (item.category) {
        VaultCategory.PASSWORD -> Icons.Filled.Key to MaterialTheme.colorScheme.tertiary
        VaultCategory.NOTE -> Icons.Filled.Description to MaterialTheme.colorScheme.secondary
        else -> Icons.Filled.AccountBalance to MaterialTheme.colorScheme.primary
    }
    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = scale.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = { Text(item.label, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(categoryLabel(item.category)) },
            leadingContent = {
                Box(
                    modifier = Modifier.size(40.dp).background(color = tint.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }
            },
            trailingContent = {
                if (item.isFavorite) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.padding(4.dp)
        )
    }
}

private fun categoryLabel(category: VaultCategory): String = when (category) {
    VaultCategory.CREDIT_CARD -> "Kredi Kartı"
    VaultCategory.BANK_ACCOUNT -> "Banka Hesabı"
    VaultCategory.PASSWORD -> "Şifre"
    VaultCategory.NOTE -> "Not"
}
