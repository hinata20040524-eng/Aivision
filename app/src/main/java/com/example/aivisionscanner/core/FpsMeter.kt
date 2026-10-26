package com.example.aivisionscanner.core

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * Menghitung FPS aktual dari frame yang benar-benar diterima pipeline analisis.
 * onFrame() dipanggil dari thread analisis kamera.
 */
class FpsMeter(private val windowMs: Long = 1000L) {

    private val frameCount = AtomicInteger(0)
    private var windowStart = SystemClock.elapsedRealtime()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    fun onFrame() {
        val count = frameCount.incrementAndGet()
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - windowStart
        if (elapsed >= windowMs) {
            _fps.value = (count * 1000L / elapsed).toInt()
            frameCount.set(0)
            windowStart = now
        }
    }
}
