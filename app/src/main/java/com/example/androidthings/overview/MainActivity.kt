/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.overview

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.pio.Gpio
import java.lang.Thread.sleep

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    // Worker thread which will do work off the main thread (blocking or computationally expensive
    // work -- network calls, IO, cryptography, etc)
    private var workerThread = HandlerThread("ButtonThread")
    private lateinit var workerHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // In case screen is attached, create basic activity layout so that Toasts can be displayed.
        setContentView(TextView(this))

        // Create the background "worker" thread and handler
        workerThread.start()
        workerHandler = Handler(workerThread.looper)

        // Initialize LED and button that controls it
        val led = getLifecycleAwareGpio(BoardDefaults.gpioForLed, Gpio.DIRECTION_OUT_INITIALLY_LOW)
        val button = LifecycleAwareButton(this, BoardDefaults.gpioForButton,
            Button.OnButtonEventListener { button, pressed ->
                if (pressed) {
                    // Execute code in a background thread, making it safe to do
                    // blocking operations.
                    workerHandler.post {
                        blinkLed(led)
                    }
                }
            })
    }

    // Blinks LED 3 times, sleeping in between.  Blocking operation, should NEVER be run on
    // UI thread.
    private fun blinkLed(led: Gpio) {
        // Cause worker thread to sleep for 6 seconds, toggling the led once per second.
        repeat(6) {
            led.value = !(led.value)
            sleep(1000)
        }
        Log.d(TAG, "Job's done!  Finished at ${System.currentTimeMillis()}")

        // Blocking calls and computationally intense  work aren't allowed on the main
        // thread (aka the "UI thread"), but UI updates are *only* allowed on the main
        // thread, so it's a common pattern to work in a "worker" thread and report results
        // back.  Fortunately, there's a simple way to request code be run on the main thread.
        runOnUiThread {
            updateUi("All done!")
        }
    }

    private fun updateUi(msg: String) {
        val duration = Toast.LENGTH_SHORT
        Toast.makeText(this, msg, duration).show()
    }
}
