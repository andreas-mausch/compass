package de.neonew.compass

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import java.lang.Math.toDegrees


class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var sensorManager: SensorManager
    lateinit var image: ImageView
    lateinit var accelerometer: Sensor
    lateinit var magnetometer: Sensor

    var currentDegree = 0.0f
    var lastAccelerometer = FloatArray(3)
    var lastMagnetometer = FloatArray(3)
    var lastAccelerometerSet = false
    var lastMagnetometerSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.imageViewCompass) as ImageView
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor === accelerometer) {
            lowPass(event!!.values, lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event!!.sensor === magnetometer) {
            lowPass(event!!.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)

            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            val azimuthInRadians = orientation[0]
            val azimuthInDegress = (toDegrees(azimuthInRadians.toDouble()) + 360).toFloat() % 360

            val rotateAnimation = RotateAnimation(
                    currentDegree,
                    -azimuthInDegress,
                    RELATIVE_TO_SELF, 0.5f,
                    RELATIVE_TO_SELF, 0.5f)
            rotateAnimation.duration = 1000
            rotateAnimation.fillAfter = true

            image.startAnimation(rotateAnimation)
            currentDegree = -azimuthInDegress
        }
    }

    fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }
}
