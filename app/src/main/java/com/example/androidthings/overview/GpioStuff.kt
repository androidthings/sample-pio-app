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

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.lang.Thread.sleep

const val GPIO_TAG: String = "GpioStuff"

class GpioStuff(lifecycleOwner: LifecycleOwner) : LifecycleObserver {

    /* The pin names for each dev board are available in the hardware section of the Android Things
     * site at https://developer.android.com/things/hardware
     * e.g the IMX7d pinout is at https://developer.android.com/things/hardware/imx7d-pico-io
     */
    private val gpioForButton = "GPIO6_IO14"

    // Handler which will perform work on the main UI thread. Typically only used to post results
    // after the work is finished.
    private var mainThreadHandler = Handler(Looper.getMainLooper())

    // Worker thread which will do work off the main thread (blocking or computationally expensive
    // work -- network calls, IO, cryptography, etc)
    private var workerThread = HandlerThread(gpioForButton)
    private var workerHandler : Handler

    init {
        workerThread.start()
        workerHandler = Handler(workerThread.looper)

        initButton(gpioForButton)
            .addToLifecycle(lifecycleOwner.lifecycle, ::initButton)

        lifecycleOwner.lifecycle.addObserver(this)
    }

    /* Initializes a single button.
     */
    private fun initButton(pinName : String): Gpio {
        val buttonGpio: Gpio = PeripheralManager.getInstance().openGpio(pinName)
        buttonGpio.setDirection(Gpio.DIRECTION_IN)
        buttonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING)

        buttonGpio.registerGpioCallback(workerHandler) {

            workerHandler.post {
                // Do some very intense work
                sleep(3000)

                // Post the results in the main UI thread
                mainThreadHandler.post {
                    Log.i(GPIO_TAG, "Job's done! ${System.currentTimeMillis()}")
                }
            }
            true
        }
        return buttonGpio
    }

    /* Helper class which uses Delegate to "add" LifecycleObserver interface to Gpio.
     * With multiple GPIO peripherals (sensors, lights, buttons, etc), an Activity's onStart/onStop
     * methods get large and repetitive very quickly.  Using Android Lifecycles can avoid much of
     * the complexity, by making each individual component listen for lifecycle changes and respond
     * accordingly.
     * The below class will release a Gpio pin when a LifeCycleOwner (usually an Activity) fires its
     * onStop method, and re-initialize the Gpio when onStart is fired.
     * Info on Lifecycle framework is available at:
     * https://developer.android.com/topic/libraries/architecture/lifecycle
     */
    class LifecycleAwareGpio(
        var gpio: Gpio,
        val gpioInit: (String ) -> Gpio,
        var pinName: String = gpio.name,
        private var skipForFirstRun: Boolean = true
    ) : Gpio by gpio, LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            close()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            if (skipForFirstRun) {
                skipForFirstRun = false
            } else {
                gpio = gpioInit(pinName)
            }
        }
    }

    fun Gpio.addToLifecycle(lifecycle: Lifecycle, gpioInit: (String) -> Gpio): Gpio {
        val observer: Gpio = LifecycleAwareGpio(this, gpioInit)
        lifecycle.addObserver(observer as LifecycleObserver)
        return observer
    }

    /* Add lifecycle events to quit / initialize the worker thread when necessary.
     * This isn't happening within the GPIO lifecycle methods, because in this case we only need
     * one background thread to handle all GPIO calls, and don't a separate thread initialized per
     * button / LED.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        workerThread.quitSafely()
        try {
            workerThread.join(1000)
        } catch (e: InterruptedException) {
            Log.e(GPIO_TAG, "Exception closing down handler thread!", e)
        }
    }
}
