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

import android.os.Build

/* Helper class to choose pin names based on which developer board is detected.
 * The pin names for each dev board are available in the hardware section of the Android Things the
 * site at https://developer.android.com/things/hardware
 * e.g the IMX7d pinout is at https://developer.android.com/things/hardware/imx7d-pico-io
 */
object BoardDefaults {
    private const val DEVICE_RPI3 = "rpi3"
    private const val DEVICE_IMX7D_PICO = "imx7d_pico"

    /**
     * Return the GPIO pin that a button is connected on.
     * The default pins in this class were chosen to align with the Rainbow hat's "A", "B" and "C"
     * buttons, but the hat isn't necessary to try this sample.
     */
    val gpioForButton = when (Build.DEVICE) {
        DEVICE_RPI3 -> "BCM21"
        DEVICE_IMX7D_PICO -> "GPIO6_IO14"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }

    val gpioForLed = when (Build.DEVICE) {
        DEVICE_RPI3 -> "BCM6"
        DEVICE_IMX7D_PICO -> "GPIO2_IO02"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }
}