package com.example.assignment5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MarbleViewModel : ViewModel(), SensorEventListener {

    var marbleState by mutableStateOf(MarbleState())
        private set

    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    // screen bounds
    private var maxX = 0f
    private var maxY = 0f
    private val marbleRadius = 40f

    // physics constants
    private val friction = 0.98f
    private val scale = 50f // Increased scale for better responsiveness with real dt

    // time tracking for physics
    private var lastUpdateTime = System.currentTimeMillis()

    fun setupSensor(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // try gravity sensor first, fallback to accelerometer
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

        if (gravitySensor == null) {
            // use accelerometer as fallback for emulator
            gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    fun registerSensor() {
        gravitySensor?.let { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        // Reset time when sensor starts
        lastUpdateTime = System.currentTimeMillis()
    }

    fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }

    fun updateBounds(width: Float, height: Float) {
        maxX = width - marbleRadius * 2
        maxY = height - marbleRadius * 2

        // initialize marble at center if needed
        if (marbleState.x == 0f && marbleState.y == 0f) {
            marbleState = marbleState.copy(
                x = maxX / 2,
                y = maxY / 2
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentTime = System.currentTimeMillis()
            val dt = (currentTime - lastUpdateTime) / 1000f // Convert to seconds
            lastUpdateTime = currentTime

            // Skip if dt is too large (e.g., app was paused)
            if (dt > 0.1f) return

            // sensor's y axis points up the screen, but offset's y points down
            // We DON'T negate because gravity sensor already accounts for direction
            val gravityX = event.values[0]
            val gravityY = event.values[1]  // Removed negation

            updatePhysics(gravityX, gravityY, dt)
        }
    }

    private fun updatePhysics(gravityX: Float, gravityY: Float, dt: Float) {
        // update velocity based on gravity
        var newVelX = marbleState.velocityX + (dt * scale * gravityX)
        var newVelY = marbleState.velocityY + (dt * scale * gravityY)

        // apply friction
        newVelX *= friction
        newVelY *= friction

        // update position
        var newX = marbleState.x + newVelX
        var newY = marbleState.y + newVelY

        // boundary checking - bounce off walls
        if (newX < 0) {
            newX = 0f
            newVelX = -newVelX * 0.7f // lose some energy on bounce
        } else if (newX > maxX) {
            newX = maxX
            newVelX = -newVelX * 0.7f
        }

        if (newY < 0) {
            newY = 0f
            newVelY = -newVelY * 0.7f
        } else if (newY > maxY) {
            newY = maxY
            newVelY = -newVelY * 0.7f
        }

        marbleState = MarbleState(
            x = newX,
            y = newY,
            velocityX = newVelX,
            velocityY = newVelY
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not needed for this assignment
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensor()
    }
}