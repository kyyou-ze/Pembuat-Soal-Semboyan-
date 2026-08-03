package com.panitia.soalsandi.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object FileSaver {

    /** Directory inside the app's cache used to build files before they're saved/shared. */
    fun exportsDir(context: Context): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun mimeTypeFor(fileName: String): String = when {
        fileName.endsWith(".pdf") -> "application/pdf"
        fileName.endsWith(".png") -> "image/png"
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
        fileName.endsWith(".zip") -> "application/zip"
        else -> "application/octet-stream"
    }

    /**
     * Copies [file] into the public Downloads collection so the user can find it
     * in their normal Files/Downloads app, fully offline (no permission dialog
     * needed on Android 10+, since MediaStore handles scoped storage).
     */
    fun saveToDownloads(context: Context, file: File): Uri? {
        val mime = mimeTypeFor(file.name)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                resolver.openOutputStream(it)?.use { out -> file.inputStream().copyTo(out) }
            }
            uri
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            val destFile = File(downloadsDir, file.name)
            file.copyTo(destFile, overwrite = true)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destFile)
        }
    }

    /** Opens a "Share/Open with" chooser for the given file (e.g. right after saving). */
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeTypeFor(file.name)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan / buka file").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
