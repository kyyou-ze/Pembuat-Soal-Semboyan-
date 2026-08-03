package com.panitia.soalsandi.export

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

    /** Bundles a list of already-exported files into a single .zip. */
    fun zipFiles(files: List<File>, outFile: File): File {
        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            files.forEach { file ->
                zos.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return outFile
    }
}
