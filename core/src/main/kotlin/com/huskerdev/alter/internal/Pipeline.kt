package com.huskerdev.alter.internal

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
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
    abstract fun createImage(type: PixelType, width: Int, height: Int, data: ByteBuffer?): Image
    abstract fun isMainThreadRequired(): Boolean

    abstract class DefaultEventPoll(private val libName: String): Pipeline() {

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

                    if(windows.isNotEmpty())
                        Platform.current.pollEvents()
                }
            }
        }
    }
}