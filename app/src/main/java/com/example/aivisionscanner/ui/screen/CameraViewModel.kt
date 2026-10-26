package com.example.aivisionscanner.ui.screen

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aivisionscanner.camera.CameraStatus
import com.example.aivisionscanner.core.FpsMeter
import com.example.aivisionscanner.detection.ModelManager
import com.example.aivisionscanner.detection.ObjectDetectionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CameraViewModel"

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val fpsMeter = FpsMeter()

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var engine: ObjectDetectionEngine? = null

    init {
        viewModelScope.launch {
            fpsMeter.fps.collect { fps ->
                _uiState.update { it.copy(fps = fps) }
            }
        }
        loadModel(application)
    }

    private fun loadModel(context: Application) {
        val manager = ModelManager(context)
        if (!manager.isModelPresent()) {
            _uiState.update { it.copy(modelStatus = ModelStatus.REQUIRED) }
            return
        }
        try {
            engine = ObjectDetectionEngine(manager.loadInterpreter())
            _uiState.update { it.copy(modelStatus = ModelStatus.READY) }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat model", e)
            _uiState.update {
                it.copy(
                    modelStatus = ModelStatus.ERROR,
                    errorMessage = "Model gagal dimuat: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    fun onFrameAnalyzed() {
        fpsMeter.onFrame()
    }

    fun onBitmapAnalyzed(bitmap: Bitmap) {
        val currentEngine = engine ?: return
        val result = try {
            currentEngine.detect(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menjalankan deteksi", e)
            _uiState.update {
                it.copy(
                    detections = emptyList(),
                    errorMessage = "Deteksi gagal: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                detections = result.detections,
                frameWidth = result.frameWidth,
                frameHeight = result.frameHeight,
                inferenceMs = result.inferenceMs,
                errorMessage = null
            )
        }
    }

    fun onCameraStatusChanged(status: CameraStatus, error: String?) {
        _uiState.update { current ->
            current.copy(
                cameraStatus = status,
                errorMessage = error,
                fps = if (status == CameraStatus.ACTIVE) current.fps else 0,
                detections = if (status == CameraStatus.ACTIVE) current.detections else emptyList()
            )
        }
    }

    override fun onCleared() {
        engine?.close()
        engine = null
        super.onCleared()
    }
}
