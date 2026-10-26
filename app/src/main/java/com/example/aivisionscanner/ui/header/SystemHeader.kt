package com.example.aivisionscanner.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aivisionscanner.camera.CameraStatus
import com.example.aivisionscanner.ui.screen.CameraUiState
import com.example.aivisionscanner.ui.screen.ModelStatus
import com.example.aivisionscanner.ui.theme.HudAmber
import com.example.aivisionscanner.ui.theme.HudGreen
import com.example.aivisionscanner.ui.theme.HudPanel
import com.example.aivisionscanner.ui.theme.HudRed
import com.example.aivisionscanner.ui.theme.HudText
import com.example.aivisionscanner.ui.theme.NeonCyan

@Composable
fun SystemHeader(state: CameraUiState, modifier: Modifier = Modifier) {
    val (cameraLabel, cameraColor) = when (state.cameraStatus) {
        CameraStatus.ACTIVE -> "AKTIF" to HudGreen
        CameraStatus.IDLE -> "STANDBY" to HudAmber
        CameraStatus.PERMISSION_REQUIRED -> "IZIN DIPERLUKAN" to HudAmber
        CameraStatus.ERROR -> "ERROR" to HudRed
    }

    val (aiLabel, aiColor) = when {
        state.cameraStatus == CameraStatus.ERROR -> "ERROR" to HudRed
        state.modelStatus != ModelStatus.READY -> "STANDBY" to HudAmber
        else -> "READY" to HudGreen
    }

    val (modelLabel, modelColor) = when (state.modelStatus) {
        ModelStatus.REQUIRED -> "MODEL REQUIRED" to HudAmber
        ModelStatus.READY -> "READY" to HudGreen
        ModelStatus.ERROR -> "ERROR" to HudRed
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HudPanel)
            .drawBehind {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.6f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI VISION SCANNER",
                    color = NeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MULTI-OBJECT DETECTION & TRACKING",
                    color = HudText.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "FPS", color = HudText.copy(alpha = 0.6f), fontSize = 9.sp)
                Text(
                    text = if (state.fps > 0) "${state.fps}" else "--",
                    color = HudGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusChip(label = "KAMERA", value = cameraLabel, color = cameraColor)
            StatusChip(label = "AI", value = aiLabel, color = aiColor)
            StatusChip(label = "MODEL", value = modelLabel, color = modelColor)
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = HudRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $value",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
