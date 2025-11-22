package com.example.smartfit.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlin.math.sqrt

class StepSensorRepository(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Create a "Hot" Flow that remembers the last value
    val stepCount: Flow<Int> = callbackFlow {
        var currentSteps = 0
        var lastStepTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // REAL SENSOR
                    if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        val steps = it.values[0].toInt()
                        trySend(steps)
                    }

                    // ACCELEROMETER FALLBACK
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]
                        val magnitude = sqrt(x * x + y * y + z * z)

                        if (magnitude > 12) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastStepTime > 500) {
                                currentSteps++
                                lastStepTime = currentTime
                                trySend(currentSteps)
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        } else {
            trySend(0)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.shareIn(
        scope = CoroutineScope(Dispatchers.Main), // Keep alive while app is running
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1 // <--- THIS IS THE FIX: Replay the last value to new subscribers
    )
}