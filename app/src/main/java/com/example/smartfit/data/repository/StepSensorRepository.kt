package com.example.smartfit.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class StepSensorRepository(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 1. Try to get the Real Step Counter
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // 2. Get the Accelerometer as a backup
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val stepCount: Flow<Int> = callbackFlow {
        var manualSteps = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {

                    // --- OPTION A: REAL HARDWARE (Real Phone) ---
                    if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        val steps = it.values[0].toInt()
                        // On real phones, this returns total steps since reboot
                        trySend(steps)
                    }

                    // --- OPTION B: EMULATOR FALLBACK (Accelerometer) ---
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]

                        // Calculate how hard the phone is moving
                        val magnitude = sqrt(x * x + y * y + z * z)

                        // Threshold: Gravity is ~9.8.
                        // If magnitude > 12, the phone is being shaken/moved.
                        if (magnitude > 12) {
                            manualSteps++
                            Log.d("StepSensor", "Shake detected! Count: $manualSteps")
                            trySend(manualSteps)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // LOGIC: Which sensor do we register?
        if (stepSensor != null) {
            Log.d("StepSensor", "✅ Using Real Step Counter Hardware")
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
        else if (accelerometer != null) {
            Log.d("StepSensor", "⚠️ Real sensor missing. Using Accelerometer Shake Detection.")
            // SENSOR_DELAY_GAME makes it update faster so shakes are caught easily
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        else {
            Log.e("StepSensor", "❌ No sensors available at all.")
            trySend(0)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}