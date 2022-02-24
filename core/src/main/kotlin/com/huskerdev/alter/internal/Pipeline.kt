package com.huskerdev.alter.internal

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.utils.LibraryLoader
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.concurrent.TimeUnit

abstract class Pipeline {

    companion object {
        val current = Class.forName("com.huskerdev.alter.internal.pipelines.${AlterUIProperties.pipeline.lowercase()}.${AlterUIProperties.pipeline.uppercase()}Pipeline")
                        .getDeclaredConstructor()
                        .newInstance() as Pipeline
        val windows = arrayListOf<Window>()

        fun initialize(){
            current.load()
        }
    }

    abstract fun load()
    abstract fun createWindow(): Window
    abstract fun createGraphics(window: Window): Graphics
    abstract fun createImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?): Image
    abstract fun isMainThreadRequired(): Boolean

    abstract class WindowPoll(private val libName: String): Pipeline() {

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
                        MainThreadLocker.tasksQueue.poll(1, TimeUnit.MILLISECONDS)!!()

                    if(windows.isNotEmpty())
                        Platform.current.pollEvents()
                }
            }
        }
    }
}