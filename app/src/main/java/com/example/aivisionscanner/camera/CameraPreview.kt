package com.example.aivisionscanner.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

private const val TAG = "CameraPreview"

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onStatusChanged: (CameraStatus, String?) -> Unit,
    onFrameAnalyzed: () -> Unit,
    onBitmap: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val currentOnStatus by rememberUpdatedState(onStatusChanged)
    val currentOnFrame by rememberUpdatedState(onFrameAnalyzed)
    val currentOnBitmap by rememberUpdatedState(onBitmap)

    DisposableEffect(lifecycleOwner) {
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(analysisExecutor) { image ->
                            currentOnFrame()
                            try {
                                val raw = image.toBitmap()
                                val degrees = image.imageInfo.rotationDegrees
                                val bitmap = if (degrees == 0) {
                                    raw
                                } else {
                                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                                    Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                                }
                                currentOnBitmap(bitmap)
                            } catch (e: Exception) {
                                Log.e(TAG, "Gagal memproses frame", e)
                            } finally {
                                image.close()
                            }
                        }
                    }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                currentOnStatus(CameraStatus.ACTIVE, null)
            } catch (e: Exception) {
                currentOnStatus(CameraStatus.ERROR, e.message ?: "Gagal membuka kamera")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
