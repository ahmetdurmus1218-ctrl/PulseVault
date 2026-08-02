package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.VaultItem
import com.screenpulsedev.pulsevault.ui.components.CreditCardView
import com.screenpulsedev.pulsevault.ui.theme.rememberPressScale

@Composable
fun VaultListScreen(
    items: List<VaultItem>,
    onAddClick: () -> Unit,
    onItemClick: (VaultItem) -> Unit
) {
    val (fabScale, fabInteraction) = rememberPressScale(pressedScale = 0.9f)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PulseVault", fontWeight = FontWeight.Bold) })
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
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Henüz kayıt yok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sağ alttaki + ile ilk kartını veya hesabını ekle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(top = 8.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    AnimatedCardEntry(delayMillis = index * 60) {
                        VaultItemCard(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

/** Fades + slides each card in on first composition, staggered by list position. */
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
private fun VaultItemCard(item: VaultItem, onClick: () -> Unit) {
    val (scale, interaction) = rememberPressScale()
    Box(
        modifier = scale
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CreditCardView(
            label = item.label,
            category = item.category,
            network = item.network,
            lastFourDigits = item.lastFourDigits,
            modifier = Modifier.clickable(
                interactionSource = interaction,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
        )
    }
}
