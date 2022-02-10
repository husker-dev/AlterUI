package com.huskerdev.alter

import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.utils.MainThreadLocker
import kotlin.concurrent.thread

class AlterUI {

    companion object {
        const val version = "1.0"

        @JvmStatic fun load(){
            Platform.initialize()
            Pipeline.initialize()
        }

        @JvmStatic fun takeMain(toInvoke: Runnable) {
            thread { toInvoke.run() }
            MainThreadLocker.lock()
        }

        @JvmStatic fun invokeOnMainThreadAsync(toInvoke: Runnable) = MainThreadLocker.invokeAsync(toInvoke)
        @JvmStatic fun invokeOnMainThread(toInvoke: Runnable) = MainThreadLocker.invoke(toInvoke)

    }
}