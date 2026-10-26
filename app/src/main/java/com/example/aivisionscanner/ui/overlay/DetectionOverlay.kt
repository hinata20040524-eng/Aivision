package com.example.aivisionscanner.ui.overlay

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import com.example.aivisionscanner.detection.CocoLabels
import com.example.aivisionscanner.detection.Detection
import com.example.aivisionscanner.ui.theme.NeonCyan
import kotlin.math.max

/**
 * Menggambar kotak deteksi di atas preview.
 * Koordinat frame disesuaikan dengan PreviewView yang memakai FILL_CENTER (crop di tengah).
 */
@Composable
fun DetectionOverlay(
    detections: List<Detection>,
    frameWidth: Int,
    frameHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (frameWidth <= 0 || frameHeight <= 0) return@Canvas

        val scale = max(size.width / frameWidth, size.height / frameHeight)
        val dx = (size.width - frameWidth * scale) / 2f
        val dy = (size.height - frameHeight * scale) / 2f
        val textSizePx = 12.sp.toPx()
        val labelHeight = textSizePx + 10f

        detections.forEach { d ->
            val l = d.left * scale + dx
            val t = d.top * scale + dy
            val r = d.right * scale + dx
            val b = d.bottom * scale + dy

            drawRect(
                color = NeonCyan,
                topLeft = Offset(l, t),
                size = Size(r - l, b - t),
                style = Stroke(width = 3f)
            )

            val text = "${CocoLabels.displayName(d.classId)} ${(d.score * 100).toInt()}%"
            val paint = Paint().apply {
                textSize = textSizePx
                isAntiAlias = true
                color = android.graphics.Color.BLACK
            }
            val labelWidth = paint.measureText(text) + 12f

            drawRect(
                color = NeonCyan,
                topLeft = Offset(l, t - labelHeight),
                size = Size(labelWidth, labelHeight)
            )
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(text, l + 6f, t - 6f, paint)
            }
        }
    }
}
