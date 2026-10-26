package com.example.aivisionscanner.detection

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

/**
 * Mengelola file model yang disimpan di app/src/main/assets.
 * Model tidak disertakan di proyek; jika file belum ada, status tetap MODEL REQUIRED.
 */
class ModelManager(private val context: Context) {

    fun isModelPresent(): Boolean =
        context.assets.list("")?.contains(MODEL_FILE) == true

    fun loadInterpreter(): Interpreter {
        val buffer = context.assets.openFd(MODEL_FILE).use { fd ->
            FileInputStream(fd.fileDescriptor).use { stream ->
                stream.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
        val options = Interpreter.Options().setNumThreads(4)
        return Interpreter(buffer, options)
    }

    companion object {
        const val MODEL_FILE = "yolov8n.tflite"
    }
}
