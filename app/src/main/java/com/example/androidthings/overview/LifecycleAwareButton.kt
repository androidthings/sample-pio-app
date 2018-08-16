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
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.Button.LogicState.PRESSED_WHEN_LOW

class LifecycleAwareButton(
    lifecycleOwner: LifecycleOwner,
    pinName: String,
    callback: OnButtonEventListener
) : Button(pinName, PRESSED_WHEN_LOW) {

    init {
        setOnButtonEventListener(callback)
        lifecycleOwner.lifecycle.addObserver(this as LifecycleObserver)
    }

    // Using the lifecycle framework, the button will automatically close itself (releasing the GPIO
    // pin) when the lifecycle owner's ON_STOP event fires.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        close()
    }
}