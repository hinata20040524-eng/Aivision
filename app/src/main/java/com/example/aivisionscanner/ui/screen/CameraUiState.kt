package com.example.aivisionscanner.ui.screen

import com.example.aivisionscanner.camera.CameraStatus
import com.example.aivisionscanner.detection.Detection

enum class ModelStatus {
    REQUIRED,
    READY,
    ERROR
}

data class CameraUiState(
    val cameraStatus: CameraStatus = CameraStatus.IDLE,
    val modelStatus: ModelStatus = ModelStatus.REQUIRED,
    val fps: Int = 0,
    val errorMessage: String? = null,
    val detections: List<Detection> = emptyList(),
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val inferenceMs: Long = 0L
)
