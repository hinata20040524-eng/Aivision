package com.example.aivisionscanner.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aivisionscanner.camera.CameraPreview
import com.example.aivisionscanner.camera.CameraStatus
import com.example.aivisionscanner.ui.header.SystemHeader
import com.example.aivisionscanner.ui.overlay.DetectionOverlay
import com.example.aivisionscanner.ui.theme.HudBackground
import com.example.aivisionscanner.ui.theme.HudText

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            viewModel.onCameraStatusChanged(
                CameraStatus.PERMISSION_REQUIRED,
                "Izin kamera ditolak"
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HudBackground)
    ) {
        if (hasPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onStatusChanged = viewModel::onCameraStatusChanged,
                onFrameAnalyzed = viewModel::onFrameAnalyzed,
                onBitmap = viewModel::onBitmapAnalyzed
            )
            DetectionOverlay(
                detections = state.detections,
                frameWidth = state.frameWidth,
                frameHeight = state.frameHeight,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PermissionRequiredMessage(
                onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        SystemHeader(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun PermissionRequiredMessage(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aplikasi membutuhkan akses kamera untuk memindai objek.",
            color = HudText
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Izinkan Kamera")
        }
        Text(
            text = "Jika izin ditolak permanen, buka Pengaturan aplikasi dan aktifkan kamera secara manual.",
            color = HudText.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
