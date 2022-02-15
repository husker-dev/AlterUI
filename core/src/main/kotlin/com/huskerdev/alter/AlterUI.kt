package com.huskerdev.alter

import com.huskerdev.alter.internal.AlterCore
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.utils.MainThreadLocker
import kotlin.concurrent.thread

class AlterUI {

    companion object {
        const val version = "1.0"

        @JvmStatic fun load(){
            AlterCore.initialize()
            Platform.initialize()
            Pipeline.initialize()
        }

        @JvmStatic inline fun takeMain(crossinline toInvoke: () -> Unit) {
            thread { toInvoke() }
            MainThreadLocker.lock()
        }

        @JvmStatic fun invokeOnMainThreadAsync(toInvoke: () -> Unit) = MainThreadLocker.invokeAsync(toInvoke)
        @JvmStatic fun invokeOnMainThread(toInvoke: () -> Unit) = MainThreadLocker.invoke(toInvoke)
    }
}