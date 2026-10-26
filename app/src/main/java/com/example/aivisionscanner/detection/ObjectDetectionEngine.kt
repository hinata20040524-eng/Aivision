package com.example.aivisionscanner.detection

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ObjectDetectionEngine"

/**
 * Menjalankan model YOLOv8. Mendukung input NHWC [1, H, W, 3] dan NCHW [1, 3, H, W].
 * Output format [1, 84, N] atau [1, N, 84]. Koordinat bisa piksel atau ternormalisasi (0..1).
 * Dipanggil dari thread analisis kamera, bukan dari UI thread.
 */
class ObjectDetectionEngine(private val interpreter: Interpreter) {

    private val inputH: Int
    private val inputW: Int
    private val inputChannelsFirst: Boolean
    private val channelsFirst: Boolean
    private val numChannels: Int
    private val numAnchors: Int
    private val numClasses: Int

    private val inputBuffer: ByteBuffer
    private val outputBuffer: ByteBuffer
    private val pixels: IntArray

    init {
        val inTensor = interpreter.getInputTensor(0)
        require(inTensor.dataType() == DataType.FLOAT32) { "Model harus berformat float32" }
        val inShape = inTensor.shape()
        inputChannelsFirst = inShape[1] == 3
        if (inputChannelsFirst) {
            inputH = inShape[2]
            inputW = inShape[3]
        } else {
            inputH = inShape[1]
            inputW = inShape[2]
        }

        val outShape = interpreter.getOutputTensor(0).shape()
        channelsFirst = outShape[1] < outShape[2]
        numChannels = if (channelsFirst) outShape[1] else outShape[2]
        numAnchors = if (channelsFirst) outShape[2] else outShape[1]
        numClasses = numChannels - 4
        require(numClasses > 0) { "Format output model tidak dikenali: ${outShape.joinToString()}" }

        Log.i(TAG, "input=${inShape.joinToString()} output=${outShape.joinToString()}")

        inputBuffer = ByteBuffer.allocateDirect(4 * inputW * inputH * 3).order(ByteOrder.nativeOrder())
        outputBuffer = ByteBuffer.allocateDirect(4 * numChannels * numAnchors).order(ByteOrder.nativeOrder())
        pixels = IntArray(inputW * inputH)
    }

    fun detect(frame: Bitmap): DetectionResult {
        val start = SystemClock.elapsedRealtime()

        val scaled = Bitmap.createScaledBitmap(frame, inputW, inputH, true)
        scaled.getPixels(pixels, 0, inputW, 0, 0, inputW, inputH)
        if (scaled !== frame) scaled.recycle()

        inputBuffer.rewind()
        if (inputChannelsFirst) {
            for (shift in CHANNEL_SHIFTS) {
                for (p in pixels) {
                    inputBuffer.putFloat(((p shr shift) and 0xFF) / 255f)
                }
            }
        } else {
            for (p in pixels) {
                inputBuffer.putFloat(((p shr 16) and 0xFF) / 255f)
                inputBuffer.putFloat(((p shr 8) and 0xFF) / 255f)
                inputBuffer.putFloat((p and 0xFF) / 255f)
            }
        }
        inputBuffer.rewind()

        outputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        val output = outputBuffer.asFloatBuffer()

        // Deteksi apakah koordinat ternormalisasi (0..1) atau dalam piksel model
        var maxCoord = 0f
        for (i in 0 until numAnchors) {
            maxCoord = max(maxCoord, output.get(index(0, i)))
            maxCoord = max(maxCoord, output.get(index(1, i)))
            maxCoord = max(maxCoord, output.get(index(2, i)))
            maxCoord = max(maxCoord, output.get(index(3, i)))
        }
        val normalized = maxCoord <= 1.5f

        // Faktor konversi koordinat model ke piksel frame
        val mx = if (normalized) frame.width.toFloat() else frame.width.toFloat() / inputW
        val my = if (normalized) frame.height.toFloat() else frame.height.toFloat() / inputH
        val maxW = frame.width.toFloat()
        val maxH = frame.height.toFloat()
        val candidates = ArrayList<Detection>()

        for (i in 0 until numAnchors) {
            var bestScore = 0f
            var bestClass = -1
            for (c in 0 until numClasses) {
                val s = output.get(index(4 + c, i))
                if (s > bestScore) {
                    bestScore = s
                    bestClass = c
                }
            }
            if (bestClass < 0 || bestScore < CONF_THRESHOLD) continue

            val cx = output.get(index(0, i))
            val cy = output.get(index(1, i))
            val w = output.get(index(2, i))
            val h = output.get(index(3, i))

            candidates.add(
                Detection(
                    classId = bestClass,
                    label = CocoLabels.nameOf(bestClass),
                    score = bestScore,
                    left = ((cx - w / 2f) * mx).coerceIn(0f, maxW),
                    top = ((cy - h / 2f) * my).coerceIn(0f, maxH),
                    right = ((cx + w / 2f) * mx).coerceIn(0f, maxW),
                    bottom = ((cy + h / 2f) * my).coerceIn(0f, maxH)
                )
            )
        }

        val kept = ArrayList<Detection>()
        for (d in candidates.sortedByDescending { it.score }) {
            if (kept.size >= MAX_DETECTIONS) break
            val overlaps = kept.any { it.classId == d.classId && iou(it, d) > IOU_THRESHOLD }
            if (!overlaps) kept.add(d)
        }

        return DetectionResult(
            detections = kept,
            frameWidth = frame.width,
            frameHeight = frame.height,
            inferenceMs = SystemClock.elapsedRealtime() - start
        )
    }

    fun close() {
        interpreter.close()
    }

    private fun index(channel: Int, anchor: Int): Int =
        if (channelsFirst) channel * numAnchors + anchor else anchor * numChannels + channel

    private fun iou(a: Detection, b: Detection): Float {
        val interW = max(0f, min(a.right, b.right) - max(a.left, b.left))
        val interH = max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))
        val inter = interW * interH
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - inter
        return if (union <= 0f) 0f else inter / union
    }

    companion object {
        const val CONF_THRESHOLD = 0.45f
        const val IOU_THRESHOLD = 0.45f
        const val MAX_DETECTIONS = 50
        private val CHANNEL_SHIFTS = intArrayOf(16, 8, 0)
    }
}
