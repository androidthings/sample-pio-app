/*
 * Copyright 2017, The Android Open Source Project
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
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

class LifecycleAwareLed(private val ledGpio : Gpio) : LifecycleObserver, Gpio by ledGpio {

    // Using the lifecycle framework, the button will automatically close itself (releasing the GPIO
    // pin) when the lifecycle owner's ON_STOP event fires.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        ledGpio.close()
    }
}

// Returns a Gpio object in a "LifecycleObserver" wrapper.
fun getLifecycleAwareGpio(pinName : String, direction : Int) : Gpio {
    val ledGpio = PeripheralManager.getInstance().openGpio(pinName)
    ledGpio.setDirection(direction)

    return LifecycleAwareLed(ledGpio)
}
