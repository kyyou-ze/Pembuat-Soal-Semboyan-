package com.panitia.soalsandi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.panitia.soalsandi.data.HistoryRepository
import com.panitia.soalsandi.export.FileSaver
import com.panitia.soalsandi.export.ImageExporter
import com.panitia.soalsandi.export.PdfExporter
import com.panitia.soalsandi.export.ZipExporter
import com.panitia.soalsandi.generator.SoalGenerator
import com.panitia.soalsandi.model.HistoryEntry
import com.panitia.soalsandi.model.SoalPackage
import com.panitia.soalsandi.ui.DownloadChoice
import com.panitia.soalsandi.ui.GeneratorScreen
import com.panitia.soalsandi.ui.HistoryScreen
import com.panitia.soalsandi.ui.SingleFormat
import com.panitia.soalsandi.ui.theme.GeneratorSoalSandiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var repository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = HistoryRepository(applicationContext)

        setContent {
            GeneratorSoalSandiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @androidx.compose.runtime.Composable
    private fun AppRoot() {
        var tab by remember { mutableStateOf(0) } // 0 = Generator, 1 = History
        var packages by remember { mutableStateOf<List<SoalPackage>>(emptyList()) }
        var selectedIndex by remember { mutableStateOf(0) }
        var history by remember { mutableStateOf(repository.loadHistory()) }
        val snackbarHostState = remember { SnackbarHostState() }

        fun refreshHistory() {
            history = repository.loadHistory()
        }

        fun doGenerate(count: Int) {
            val used = repository.loadUsedCombos()
            val newPackages = SoalGenerator.generatePackages(count, used)
            repository.saveUsedCombos(used)
            packages = newPackages
            selectedIndex = 0

            val entry = HistoryEntry(
                id = UUID.randomUUID().toString(),
                timestampMillis = System.currentTimeMillis(),
                jumlahPaket = count,
                packages = newPackages
            )
            repository.addHistoryEntry(entry)
            refreshHistory()
        }

        /**
         * Saves the file, then shows a Snackbar (NOT an automatic share-sheet —
         * that used to fire on every download and could crash on some devices/
         * OEM skins). Opening the file is now an explicit, optional action the
         * user taps, and it's wrapped in try/catch so a failure there is just a
         * toast, never a force-close.
         */
        suspend fun saveAndOfferToOpen(packagesToExport: List<SoalPackage>, choice: DownloadChoice) {
            val savedFile = try {
                withContext(Dispatchers.IO) { exportAndSave(packagesToExport, choice) }
            } catch (e: Exception) {
                null
            }

            if (savedFile == null) {
                Toast.makeText(this@MainActivity, "Gagal membuat file unduhan", Toast.LENGTH_SHORT).show()
                return
            }

            val result = snackbarHostState.showSnackbar(
                message = "Tersimpan di Downloads: ${savedFile.name}",
                actionLabel = "Buka",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                try {
                    FileSaver.shareFile(this@MainActivity, savedFile)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tidak bisa membuka otomatis — cek folder Downloads.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        fun handleDownload(choice: DownloadChoice) {
            lifecycleScope.launch { saveAndOfferToOpen(packages, choice) }
        }

        fun handleHistoryDownload(entry: HistoryEntry) {
            val choice = if (entry.packages.size > 1) DownloadChoice.AllAsOnePdf()
            else DownloadChoice.SinglePackage(SingleFormat.PDF)
            lifecycleScope.launch { saveAndOfferToOpen(entry.packages, choice) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (tab == 0) "Generator Soal Sandi" else "Riwayat") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                        label = { Text("Generator") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.History, contentDescription = null) },
                        label = { Text("Riwayat") }
                    )
                }
            }
        ) { padding ->
            if (tab == 0) {
                GeneratorScreen(
                    packages = packages,
                    selectedIndex = selectedIndex,
                    onSelectPackage = { selectedIndex = it },
                    onGenerate = { doGenerate(1) },
                    onRefresh = { doGenerate(1) },
                    onGenerateMultiple = { count -> doGenerate(count) },
                    onDownload = { choice -> handleDownload(choice) },
                    modifier = Modifier.padding(padding)
                )
            } else {
                HistoryScreen(
                    entries = history,
                    onOpen = { entry ->
                        packages = entry.packages
                        selectedIndex = 0
                        tab = 0
                    },
                    onDownload = { entry -> handleHistoryDownload(entry) },
                    onDelete = { entry ->
                        repository.deleteHistoryEntry(entry.id)
                        refreshHistory()
                    },
                    onRename = { entry, newName ->
                        repository.renameHistoryEntry(entry.id, newName)
                        refreshHistory()
                    },
                    onClearAll = {
                        repository.clearHistory()
                        refreshHistory()
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    /** Builds the requested export in [FileSaver.exportsDir], saves it to Downloads, returns the saved File. */
    private fun exportAndSave(packages: List<SoalPackage>, choice: DownloadChoice): File? {
        if (packages.isEmpty()) return null
        val dir = FileSaver.exportsDir(this)
        val stamp = System.currentTimeMillis()

        val builtFile: File = when (choice) {
            is DownloadChoice.SinglePackage -> {
                val pkg = packages.first()
                when (choice.format) {
                    SingleFormat.PDF -> PdfExporter.exportSingle(pkg, File(dir, "${safe(pkg.name)}_$stamp.pdf"))
                    SingleFormat.PNG -> ImageExporter.exportPackage(
                        pkg, com.panitia.soalsandi.export.ImageFormat.PNG, File(dir, "${safe(pkg.name)}_$stamp.png")
                    )
                    SingleFormat.JPG -> ImageExporter.exportPackage(
                        pkg, com.panitia.soalsandi.export.ImageFormat.JPG, File(dir, "${safe(pkg.name)}_$stamp.jpg")
                    )
                }
            }

            is DownloadChoice.AllAsOnePdf -> {
                PdfExporter.exportCombined(packages, File(dir, "Semua_Paket_$stamp.pdf"))
            }

            is DownloadChoice.PerPackageZip -> {
                val perPackageFiles = packages.map { pkg ->
                    when {
                        choice.pdf -> PdfExporter.exportSingle(pkg, File(dir, "${safe(pkg.name)}.pdf"))
                        choice.format != null -> ImageExporter.exportPackage(
                            pkg, choice.format, File(dir, "${safe(pkg.name)}.${choice.format.name.lowercase()}")
                        )
                        else -> PdfExporter.exportSingle(pkg, File(dir, "${safe(pkg.name)}.pdf"))
                    }
                }
                ZipExporter.zipFiles(perPackageFiles, File(dir, "Semua_Paket_$stamp.zip"))
            }
        }

        val uri = FileSaver.saveToDownloads(this, builtFile)
        return if (uri != null) builtFile else null
    }

    private fun safe(name: String): String = name.replace(" ", "_")
}
