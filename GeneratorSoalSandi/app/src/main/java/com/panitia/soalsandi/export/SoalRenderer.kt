package com.panitia.soalsandi.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.panitia.soalsandi.model.SoalPackage

/**
 * Draws a single paket onto any [Canvas] (works for both a Bitmap canvas and a
 * PdfDocument page canvas — both are plain android.graphics.Canvas).
 *
 * Layout intentionally mirrors the reference sheet: no title, no "Kunci Jawaban"
 * label, no decoration — just the numbered soal boxes followed directly by the
 * answer list. Print-ready on A4 when used via [PdfExporter].
 */
object SoalRenderer {

    private const val COLS = 5
    private const val ROWS = 6 // 5 x 6 = 30 boxes

    fun draw(canvas: Canvas, pkg: SoalPackage, width: Float, height: Float) {
        val scale = width / 595f // 595pt = A4 width baseline used to size fonts/margins

        val margin = 28f * scale
        val gridLeft = margin
        val gridTop = margin
        val gridWidth = width - margin * 2
        val cellW = gridWidth / COLS
        val cellH = cellW * 0.8f

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f * scale
            color = Color.BLACK
            isAntiAlias = true
        }
        val numberPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f * scale
            isAntiAlias = true
        }
        val codePaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f * scale
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // ---- Soal grid: 30 boxes, number top-left, code centered ----
        pkg.items.forEach { item ->
            val row = (item.number - 1) / COLS
            val col = (item.number - 1) % COLS
            val left = gridLeft + col * cellW
            val top = gridTop + row * cellH
            val rect = RectF(left, top, left + cellW, top + cellH)
            canvas.drawRect(rect, boxPaint)
            canvas.drawText(item.number.toString(), left + 4f * scale, top + 12f * scale, numberPaint)
            canvas.drawText(
                item.code,
                left + cellW / 2f,
                top + cellH / 2f + 5f * scale,
                codePaint
            )
        }

        // ---- Answer list directly below the grid ----
        val answerTop = gridTop + ROWS * cellH + 24f * scale
        val answerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f * scale
            isAntiAlias = true
        }
        val answerCols = 5
        val answerRows = 6
        val answerColWidth = gridWidth / answerCols
        val lineHeight = 18f * scale

        pkg.items.forEach { item ->
            val row = (item.number - 1) / answerCols
            val col = (item.number - 1) % answerCols
            val x = gridLeft + col * answerColWidth
            val y = answerTop + row * lineHeight
            canvas.drawText("${item.number}. ${item.letter}", x, y, answerPaint)
        }
    }

    /** Standard A4 portrait size in points (used by PDF export at 72dpi). */
    const val A4_WIDTH_PT = 595
    const val A4_HEIGHT_PT = 842
}
