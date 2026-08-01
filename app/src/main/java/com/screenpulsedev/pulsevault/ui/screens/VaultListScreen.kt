package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.VaultItem

@Composable
fun VaultListScreen(
    items: List<VaultItem>,
    onAddClick: () -> Unit,
    onItemClick: (VaultItem) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PulseVault") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
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
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Henüz kayıt yok. Sağ alttaki + ile kart veya hesap ekle.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(items) { item ->
                    Card(
                        onClick = { onItemClick(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(item.label) },
                            supportingContent = { Text(item.category.name) },
                            leadingContent = { Icon(Icons.Filled.CreditCard, contentDescription = null) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
