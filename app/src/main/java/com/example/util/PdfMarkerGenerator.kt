package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.ARTrackedImageManager
import com.example.model.MarkerType
import java.io.File
import java.io.FileOutputStream

object PdfMarkerGenerator {

    fun generateAndSharePdf(context: Context): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }

        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val fillPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            isFakeBoldText = true
        }

        // Title Header
        canvas.drawText("ClayStudio AR - Hoja de Marcadores de Anclaje (A4)", 36f, 40f, titlePaint)
        canvas.drawText("Imprime esta página y coloca los marcadores alrededor de tu mesa de modelado o plato giratorio.", 36f, 56f, subtitlePaint)

        val manager = ARTrackedImageManager()
        val slots = manager.slots

        val columns = 3
        val cardWidth = 160f
        val cardHeight = 220f
        val startX = 36f
        val startY = 80f
        val marginX = 18f
        val marginY = 18f

        slots.forEachIndexed { index, slot ->
            val col = index % columns
            val row = index / columns

            val left = startX + col * (cardWidth + marginX)
            val top = startY + row * (cardHeight + marginY)
            val right = left + cardWidth
            val bottom = top + cardHeight

            // Draw Card Frame
            canvas.drawRect(left, top, right, bottom, borderPaint)

            // Header label inside card
            canvas.drawText("SLOT ${slot.slotId}: ${slot.title}", left + 8f, top + 18f, textPaint)
            canvas.drawText("Eje: ${slot.markerType.displayName}", left + 8f, top + 32f, subtitlePaint)

            // Draw Marker Symbol inside square
            val boxLeft = left + 20f
            val boxTop = top + 42f
            val boxSize = 120f
            val cx = boxLeft + boxSize / 2f
            val cy = boxTop + boxSize / 2f

            canvas.drawRect(boxLeft, boxTop, boxLeft + boxSize, boxTop + boxSize, borderPaint)

            when (slot.markerType) {
                MarkerType.CIRCLE_X -> {
                    borderPaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, 38f, borderPaint)
                    canvas.drawCircle(cx, cy, 12f, fillPaint)
                    borderPaint.strokeWidth = 2f
                }
                MarkerType.TRIANGLE_Z -> {
                    val p = Path().apply {
                        moveTo(cx, cy - 36f)
                        lineTo(cx - 36f, cy + 36f)
                        lineTo(cx + 36f, cy + 36f)
                        close()
                    }
                    borderPaint.strokeWidth = 4f
                    canvas.drawPath(p, borderPaint)
                    borderPaint.strokeWidth = 2f
                }
                MarkerType.CROSS_Y -> {
                    fillPaint.strokeWidth = 8f
                    canvas.drawLine(cx - 36f, cy, cx + 36f, cy, fillPaint)
                    canvas.drawLine(cx, cy - 36f, cx, cy + 36f, fillPaint)
                    fillPaint.strokeWidth = 1f
                }
                MarkerType.QR_SLOT -> {
                    // QR pattern simulation with alignment squares
                    val qSize = 28f
                    canvas.drawRect(boxLeft + 10f, boxTop + 10f, boxLeft + 10f + qSize, boxTop + 10f + qSize, fillPaint)
                    canvas.drawRect(boxLeft + boxSize - 10f - qSize, boxTop + 10f, boxLeft + boxSize - 10f, boxTop + 10f + qSize, fillPaint)
                    canvas.drawRect(boxLeft + 10f, boxTop + boxSize - 10f - qSize, boxLeft + 10f + qSize, boxTop + boxSize - 10f, fillPaint)
                    canvas.drawCircle(cx, cy, 14f, fillPaint)
                }
            }

            // Bottom QR Identifier code string
            canvas.drawText("ID: ${slot.qrCodeId}", left + 8f, bottom - 12f, subtitlePaint)
        }

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "marcadores_claystudio_ar.pdf")
        val outputStream = FileOutputStream(outputFile)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        val authority = "${context.packageName}.fileprovider"
        return try {
            FileProvider.getUriForFile(context, authority, outputFile)
        } catch (e: Exception) {
            Uri.fromFile(outputFile)
        }
    }

    fun sharePdfFile(context: Context) {
        val fileUri = generateAndSharePdf(context) ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir Hoja PDF de Marcadores AR"))
    }
}
