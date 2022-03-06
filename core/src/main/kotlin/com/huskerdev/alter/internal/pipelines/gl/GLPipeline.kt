package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.OS
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker
import com.huskerdev.alter.internal.utils.Trigger
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

@ImplicitUsage
class GLPipeline: Pipeline.DefaultEventPoll("gl") {

    companion object {
        private var resourceWindow = 0L
        var resourceThread: Thread? = null

        val resourcesQueue = LinkedBlockingQueue<() -> Unit>()

        // Platform-specific
        @JvmStatic external fun nMakeCurrent(handle: Long)
        @JvmStatic external fun nSwapBuffers(handle: Long)

        // GL
        const val GL_COLOR_BUFFER_BIT = 0x4000
        const val GL_DEPTH_BUFFER_BIT = 0x100
        const val GL_TEXTURE_2D = 0xDE1

        @JvmStatic external fun glClear(mask: Int)
        @JvmStatic external fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)
        @JvmStatic external fun glViewport(x: Int, y: Int, width: Int, height: Int)
        @JvmStatic external fun glUseProgram(program: Int)
        @JvmStatic external fun glBindTexture(target: Int, texture: Int)

        @JvmStatic external fun nInitContext()
        @JvmStatic external fun nDrawArray(array: FloatBuffer, count: Int, type: Int)
        @JvmStatic external fun nCreateTexture(width: Int, height: Int, channels: Int, data: ByteBuffer): Int
        @JvmStatic external fun nCreateEmptyTexture(width: Int, height: Int, channels: Int): Int
        @JvmStatic external fun nSetLinearFiltering(tex: Int, linearFiltering: Boolean)

        // GL-Shader
        @JvmStatic external fun nCreateShaderProgram(vertexSource: ByteBuffer, fragmentSource: ByteBuffer): Int
        @JvmStatic external fun glGetUniformLocation(program: Int, name: ByteBuffer): Int
        @JvmStatic external fun nSetShaderVariable4f(program: Int, location: Int, val1: Float, val2: Float, val3: Float, val4: Float)
        @JvmStatic external fun nSetShaderVariable3f(program: Int, location: Int, val1: Float, val2: Float, val3: Float)
        @JvmStatic external fun nSetShaderVariable1f(program: Int, location: Int, val1: Float)
        @JvmStatic external fun nSetShaderMatrixVariable(program: Int, location: Int, matrix: FloatBuffer)
    }

    private external fun nCreateWindow(shareWith: Long): Long

    override fun load() {
        super.load()
        MainThreadLocker.invoke {
            resourceWindow = nCreateWindow(0)
            resourceThread = thread(name = "Alter OpenGL resource", isDaemon = true) {
                nMakeCurrent(resourceWindow)
                while(true)
                    resourcesQueue.take().invoke()
            }
        }
    }

    override fun createGraphics(window: Window) = GLGraphics(window)
    override fun createImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?): Image {
        var texId = 0
        invokeOnResourceThread {
            texId = if(data != null)
                nCreateTexture(width, height, type.channels, data)
            else nCreateEmptyTexture(width, height, type.channels)
        }
        return GLImage(texId, type, width, height)
    }

    override fun isMainThreadRequired() = OS.current != OS.Windows

    override fun createWindow(): Window {
        lateinit var newWindow: Window
        invokeOnResourceThread {
            nMakeCurrent(0)
            MainThreadLocker.invoke {
                newWindow = Platform.current.createWindowInstance(nCreateWindow(resourceWindow))
            }
            nMakeCurrent(resourceWindow)
        }
        windows.add(newWindow)
        return newWindow
    }

    inline fun invokeOnResourceThread(crossinline run: () -> Unit){
        if(Thread.currentThread() == resourceThread)
            run()
        else {
            val trigger = Trigger()
            resourcesQueue.offer {
                run()
                trigger.ready()
            }
            trigger.waitForReady()
        }
    }

    fun invokeOnResourceThreadAsync(run: () -> Unit){
        if(Thread.currentThread() == resourceThread)
            run()
        else resourcesQueue.offer(run)
    }

}