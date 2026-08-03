package com.panitia.soalsandi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.panitia.soalsandi.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    onOpen: (HistoryEntry) -> Unit,
    onDownload: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onRename: (HistoryEntry, String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmClearAll by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<HistoryEntry?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Riwayat", style = MaterialTheme.typography.titleLarge)
            if (entries.isNotEmpty()) {
                TextButton(onClick = { confirmClearAll = true }) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Hapus Semua")
                }
            }
        }
        HorizontalDivider()

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onOpen = onOpen,
                        onDownload = onDownload,
                        onDelete = onDelete,
                        onRenameRequested = { renameTarget = entry }
                    )
                }
            }
        }
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("Hapus semua riwayat?") },
            text = { Text("Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClearAll = false
                    onClearAll()
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text("Batal") }
            }
        )
    }

    val target = renameTarget
    if (target != null) {
        RenameDialog(
            initialName = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                renameTarget = null
                onRename(target, newName)
            }
        )
    }
}

@Composable
private fun RenameDialog(initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Beri nama riwayat") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("misal: Lomba Semboyan Jepara 2026") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    onOpen: (HistoryEntry) -> Unit,
    onDownload: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onRenameRequested: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val firstPkg = entry.packages.firstOrNull()
    val dateText = dateFormat.format(Date(entry.timestampMillis))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name.ifBlank { dateText },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (entry.name.isNotBlank()) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onRenameRequested) {
                    Icon(Icons.Filled.Edit, contentDescription = "Ubah nama")
                }
                Text(
                    "${entry.jumlahPaket} paket",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (firstPkg != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                Text(
                    "Soal: " + firstPkg.items.take(6).joinToString("  ") { it.code },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Jawaban: " + firstPkg.items.take(6).joinToString(" ") { "${it.number}.${it.letter}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onDelete(entry) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                }
                IconButton(onClick = { onDownload(entry) }) {
                    Icon(Icons.Filled.Download, contentDescription = "Unduh ulang")
                }
                OutlinedButton(onClick = { onOpen(entry) }) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Buka")
                }
            }
        }
    }
}
