package com.panitia.soalsandi.export

import android.graphics.pdf.PdfDocument
import com.panitia.soalsandi.model.SoalPackage
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    /** One PDF, one page per paket (used for "semua paket jadi satu PDF"). */
    fun exportCombined(packages: List<SoalPackage>, outFile: File): File {
        val document = PdfDocument()
        packages.forEachIndexed { index, pkg ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                SoalRenderer.A4_WIDTH_PT, SoalRenderer.A4_HEIGHT_PT, index + 1
            ).create()
            val page = document.startPage(pageInfo)
            SoalRenderer.draw(
                page.canvas, pkg,
                SoalRenderer.A4_WIDTH_PT.toFloat(),
                SoalRenderer.A4_HEIGHT_PT.toFloat()
            )
            document.finishPage(page)
        }
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()
        return outFile
    }

    /** One PDF for a single paket (used for "satu paket satu PDF"). */
    fun exportSingle(pkg: SoalPackage, outFile: File): File = exportCombined(listOf(pkg), outFile)
}
