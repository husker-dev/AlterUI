package com.huskerdev.alter.internal

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.internal.utils.LibraryLoader
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer

abstract class Pipeline {

    companion object {
        val current = Class.forName("com.huskerdev.alter.internal.pipelines.${AlterUIProperties.pipeline.lowercase()}.${AlterUIProperties.pipeline.uppercase()}Pipeline")
                        .getDeclaredConstructor()
                        .newInstance() as Pipeline
        val windows = arrayListOf<WindowPeer>()

        fun initialize(){
            current.load()
        }
    }

    abstract fun load()
    abstract fun createWindow(): WindowPeer
    abstract fun createGraphics(window: WindowPeer): Graphics
    abstract fun createGraphics(image: Image): Graphics
    abstract fun createImage(
        type: PixelType,
        width: Int,
        height: Int,
        data: ByteBuffer?
    ): Image
    abstract fun createSurfaceImage(
        window: WindowPeer,
        type: PixelType,
        physicalWidth: Int,
        physicalHeight: Int,
        logicWidth: Int,
        logicHeight: Int,
        dpi: Float
    ): Image
    abstract fun isMainThreadRequired(): Boolean

    // Image filters
    abstract fun createGaussianBlurFilter(radius: Int): GaussianBlur

    abstract class DefaultEventPoll(private val libName: String): Pipeline() {

        companion object {
            // Fps timer
            private var fpsTimerStart = 0L
            private var countedFrames = 0

            private var repaintStartTime = 0L

            var currentFps = 0
        }

        override fun load() {
            LibraryLoader.loadModuleLib(libName)

            MainThreadLocker.invokeAsync {
                MainThreadLocker.queueNotifiers.add {
                    if(windows.size > 0)
                        Platform.current.sendEmptyMessage(windows[0].handle)
                }
                while(true) {
                    if(MainThreadLocker.disposed)
                        break
                    if(MainThreadLocker.tasksQueue.size > 0)
                        MainThreadLocker.tasksQueue.take()()

                    if(windows.isNotEmpty()) {
                        if(AlterUIProperties.alwaysRepaint){
                            repaintStartTime = System.currentTimeMillis()

                            Platform.current.takeEvents()
                            for(window in windows)
                                window.onDrawCallback()

                            val currentTime = System.currentTimeMillis()
                            if((currentTime - fpsTimerStart) > 1000) {
                                fpsTimerStart = currentTime
                                currentFps = countedFrames
                                countedFrames = 0
                            }
                            countedFrames++

                            if(AlterUIProperties.fpsLimit > 0) {
                                val sleepTime = 1000 / AlterUIProperties.fpsLimit - (currentTime - repaintStartTime)
                                if(sleepTime > 0)
                                    Thread.sleep(sleepTime)
                            }
                        }else Platform.current.pollEvents()
                    }
                }
            }
        }
    }
}