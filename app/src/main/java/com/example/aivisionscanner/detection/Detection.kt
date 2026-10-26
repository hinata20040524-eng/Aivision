package com.example.aivisionscanner.detection

data class Detection(
    val classId: Int,
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class DetectionResult(
    val detections: List<Detection>,
    val frameWidth: Int,
    val frameHeight: Int,
    val inferenceMs: Long
)
