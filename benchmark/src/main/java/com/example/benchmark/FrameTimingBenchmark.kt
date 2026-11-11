package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class measures the frame timing of the app.
 * It's used to detect jank (lags) during UI interactions.
 */
@RunWith(AndroidJUnit4::class)
class FrameTimingBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollChat() = benchmarkRule.measureRepeated(
        packageName = "com.ensodai.avalon",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.None(), // Measure without optimizations first
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            // Setup the app to the state where we can scroll
            pressHome()
            startActivityAndWait()
        }
    ) {
        // Find the scrollable list (adjust the resource id as needed)
        // This is an example, you might need to find your actual list container
        val list = device.wait(Until.findObject(By.scrollable(true)), 5_000)

        list?.apply {
            setGestureMargin(device.displayWidth / 5)
            // Scroll down and up to measure performance
            fling(Direction.DOWN)
            fling(Direction.UP)
        }
    }
}
