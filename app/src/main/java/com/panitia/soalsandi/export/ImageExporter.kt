package com.panitia.soalsandi.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.panitia.soalsandi.model.SoalPackage
import java.io.File
import java.io.FileOutputStream

enum class ImageFormat { PNG, JPG }

object ImageExporter {

    // Roughly 150dpi for an A4 sheet -> crisp enough to read/print.
    private const val WIDTH_PX = 1240
    private const val HEIGHT_PX = 1754

    fun exportPackage(pkg: SoalPackage, format: ImageFormat, outFile: File): File {
        val bitmap = Bitmap.createBitmap(WIDTH_PX, HEIGHT_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        SoalRenderer.draw(canvas, pkg, WIDTH_PX.toFloat(), HEIGHT_PX.toFloat())

        val compressFormat = if (format == ImageFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        FileOutputStream(outFile).use { out ->
            bitmap.compress(compressFormat, 95, out)
        }
        bitmap.recycle()
        return outFile
    }
}
