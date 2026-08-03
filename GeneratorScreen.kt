package com.panitia.soalsandi.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.panitia.soalsandi.export.ImageFormat
import com.panitia.soalsandi.model.SoalPackage
import com.panitia.soalsandi.ui.components.AnswerList
import com.panitia.soalsandi.ui.components.SoalGrid

/** What the user picked in the download dialog. */
sealed class DownloadChoice {
    data class SinglePackage(val format: SingleFormat) : DownloadChoice()
    data class AllAsOnePdf(val dummy: Unit = Unit) : DownloadChoice()
    data class PerPackageZip(val format: ImageFormat?, val pdf: Boolean) : DownloadChoice()
}
enum class SingleFormat { PDF, PNG, JPG }

private val PACKAGE_COUNT_OPTIONS = listOf(2, 3, 5, 10, 20, 30, 50, 100)

@Composable
fun GeneratorScreen(
    packages: List<SoalPackage>,
    selectedIndex: Int,
    onSelectPackage: (Int) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onGenerateMultiple: (Int) -> Unit,
    onDownload: (DownloadChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMultiDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // ---- Action buttons ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onGenerate) {
                Icon(Icons.Filled.LibraryAdd, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Generate")
            }
            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Refresh")
            }
            OutlinedButton(onClick = { showMultiDialog = true }) {
                Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Paket Banyak")
            }
            Button(onClick = { showDownloadDialog = true }) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Download")
            }
        }

        HorizontalDivider()

        // ---- Paket switcher (only shown when there's more than one) ----
        if (packages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                packages.forEachIndexed { index, pkg ->
                    FilterChip(
                        selected = index == selectedIndex,
                        onClick = { onSelectPackage(index) },
                        label = { Text(pkg.name) }
                    )
                }
            }
        }

        // ---- Soal grid + answer list ----
        val current = packages.getOrNull(selectedIndex)
        if (current != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                SoalGrid(pkg = current, modifier = Modifier.wrapContentHeight())
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                AnswerList(pkg = current, modifier = Modifier.wrapContentHeight())
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 24.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.LibraryAdd,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Belum ada soal",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    "Tekan \"Generate\" untuk membuat paket soal pertama.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (showMultiDialog) {
        MultiPackageDialog(
            onDismiss = { showMultiDialog = false },
            onConfirm = { count ->
                showMultiDialog = false
                onGenerateMultiple(count)
            }
        )
    }

    if (showDownloadDialog) {
        DownloadDialog(
            isMultiplePackages = packages.size > 1,
            onDismiss = { showDownloadDialog = false },
            onConfirm = { choice ->
                showDownloadDialog = false
                onDownload(choice)
            }
        )
    }
}

@Composable
private fun MultiPackageDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var selected by remember { mutableStateOf(PACKAGE_COUNT_OPTIONS.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Banyak Paket") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PACKAGE_COUNT_OPTIONS.forEach { count ->
                        FilterChip(
                            selected = selected == count,
                            onClick = { selected = count },
                            label = { Text(count.toString()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Generate") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DownloadDialog(
    isMultiplePackages: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (DownloadChoice) -> Unit
) {
    // Options differ depending on whether one or several packages are active.
    var mode by remember { mutableStateOf(if (isMultiplePackages) "combined_pdf" else "single") }
    var singleFormat by remember { mutableStateOf(SingleFormat.PDF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download") },
        text = {
            Column {
                if (!isMultiplePackages) {
                    Text("Pilih format:", style = MaterialTheme.typography.bodyMedium)
                    SingleFormat.values().forEach { fmt ->
                        Row {
                            RadioButton(selected = singleFormat == fmt, onClick = { singleFormat = fmt })
                            Text(fmt.name, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                } else {
                    Text("Beberapa paket aktif — pilih mode unduh:", style = MaterialTheme.typography.bodyMedium)
                    val options = listOf(
                        "combined_pdf" to "Semua paket jadi satu PDF",
                        "zip_pdf" to "Satu paket satu PDF (ZIP)",
                        "zip_png" to "Setiap paket sebagai PNG (ZIP)",
                        "zip_jpg" to "Setiap paket sebagai JPG (ZIP)"
                    )
                    options.forEach { (key, label) ->
                        Row {
                            RadioButton(selected = mode == key, onClick = { mode = key })
                            Text(label, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val choice = if (!isMultiplePackages) {
                    DownloadChoice.SinglePackage(singleFormat)
                } else {
                    when (mode) {
                        "combined_pdf" -> DownloadChoice.AllAsOnePdf()
                        "zip_pdf" -> DownloadChoice.PerPackageZip(format = null, pdf = true)
                        "zip_png" -> DownloadChoice.PerPackageZip(format = ImageFormat.PNG, pdf = false)
                        else -> DownloadChoice.PerPackageZip(format = ImageFormat.JPG, pdf = false)
                    }
                }
                onConfirm(choice)
            }) { Text("Unduh") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
