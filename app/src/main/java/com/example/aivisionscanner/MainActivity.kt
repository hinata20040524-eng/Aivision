package com.example.aivisionscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aivisionscanner.ui.screen.CameraScreen
import com.example.aivisionscanner.ui.theme.AIVisionScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIVisionScannerTheme {
                CameraScreen()
            }
        }
    }
}
