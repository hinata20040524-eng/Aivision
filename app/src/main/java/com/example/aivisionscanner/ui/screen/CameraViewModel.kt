package com.example.aivisionscanner.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aivisionscanner.camera.CameraStatus
import com.example.aivisionscanner.core.FpsMeter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val fpsMeter = FpsMeter()

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fpsMeter.fps.collect { fps ->
                _uiState.update { it.copy(fps = fps) }
            }
        }
    }

    fun onFrameAnalyzed() {
        fpsMeter.onFrame()
    }

    fun onCameraStatusChanged(status: CameraStatus, error: String?) {
        _uiState.update { current ->
            current.copy(
                cameraStatus = status,
                errorMessage = error,
                fps = if (status == CameraStatus.ACTIVE) current.fps else 0
            )
        }
    }
}
