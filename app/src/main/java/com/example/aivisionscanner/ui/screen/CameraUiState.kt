package com.example.aivisionscanner.ui.screen

import com.example.aivisionscanner.camera.CameraStatus

enum class ModelStatus {
    REQUIRED,
    READY,
    ERROR
}

data class CameraUiState(
    val cameraStatus: CameraStatus = CameraStatus.IDLE,
    val modelStatus: ModelStatus = ModelStatus.REQUIRED,
    val fps: Int = 0,
    val errorMessage: String? = null
)
