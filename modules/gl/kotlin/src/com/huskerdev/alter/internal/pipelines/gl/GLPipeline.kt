package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.WindowPeer
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
        var resourceWindow = 0L
        var resourceThread: Thread? = null

        val resourcesQueue = LinkedBlockingQueue<() -> Unit>()

        lateinit var resourcesContext: GLContext
        val contexts = hashMapOf<WindowPeer, GLContext>()

        // Platform-specific
        @JvmStatic external fun nCreateWindow(shareWith: Long): Long
        @JvmStatic external fun nMakeCurrent(handle: Long)
        @JvmStatic external fun nSwapBuffers(handle: Long)

        // GL
        const val GL_COLOR_BUFFER_BIT = 0x4000
        const val GL_DEPTH_BUFFER_BIT = 0x100
        const val GL_TEXTURE_2D = 0xDE1
        const val GL_FRAMEBUFFER = 0x8D40
        const val GL_SRC_ALPHA = 0x0302
        const val GL_ONE_MINUS_SRC_ALPHA = 0x0303
        const val GL_SRC1_COLOR = 0x88F9
        const val GL_ONE_MINUS_SRC1_COLOR = 0x88FA

        @JvmStatic external fun glClear(mask: Int)
        @JvmStatic external fun glViewport(x: Int, y: Int, width: Int, height: Int)
        @JvmStatic external fun glUseProgram(program: Int)
        @JvmStatic external fun glBindTexture(target: Int, texture: Int)
        @JvmStatic external fun glBlendFunc(sfactor: Int, dfactor: Int)
        @JvmStatic external fun glBindFramebuffer(n: Int, buffer: Int)
        @JvmStatic external fun glFlush()
        @JvmStatic external fun glFinish()

        @JvmStatic external fun nInitContext()
        @JvmStatic external fun nDrawArray(array: FloatBuffer, count: Int, type: Int)
        @JvmStatic external fun nCreateTexture(width: Int, height: Int, channels: Int, data: ByteBuffer): Int
        @JvmStatic external fun nCreateEmptyTexture(width: Int, height: Int, channels: Int): Int
        @JvmStatic external fun nBindTextureBuffer(texId: Int): Int
        @JvmStatic external fun nSetLinearFiltering(tex: Int, linearFiltering: Boolean)
        @JvmStatic external fun nReadPixels(framebuffer: Int, channels: Int, x: Int, y: Int, width: Int, height: Int): ByteBuffer
        @JvmStatic external fun nReleaseTexture(tex: Int)
        @JvmStatic external fun nReleaseFrameBuffer(tex: Int)

        // GL-Shader
        @JvmStatic external fun nCreateShaderProgram(vertexSource: ByteBuffer, fragmentSource: ByteBuffer): Int
        @JvmStatic external fun glGetUniformLocation(program: Int, name: ByteBuffer): Int
        @JvmStatic external fun nSetShaderVariable4f(program: Int, location: Int, val1: Float, val2: Float, val3: Float, val4: Float)
        @JvmStatic external fun nSetShaderVariable3f(program: Int, location: Int, val1: Float, val2: Float, val3: Float)
        @JvmStatic external fun nSetShaderVariable1f(program: Int, location: Int, val1: Float)
        @JvmStatic external fun nSetShaderMatrixVariable(program: Int, location: Int, matrix: FloatBuffer)
        @JvmStatic external fun glUniform1i(location: Int, v0: Int)

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

    override fun load() {
        super.load()
        MainThreadLocker.invoke {
            resourceThread = thread(name = "Alter OpenGL resource", isDaemon = true) {
                resourceWindow = nCreateWindow(0)
                nMakeCurrent(resourceWindow)
                nInitContext()
                resourcesContext = GLContext(resourceWindow)

                while(true)
                    resourcesQueue.take().invoke()
            }
        }
    }

    private fun getWindowGLContext(window: WindowPeer): GLContext{
        if(window !in contexts)
            contexts[window] = GLContext(window.handle)
        return contexts[window]!!
    }

    override fun createGraphics(window: WindowPeer) = WindowGLGraphics(window, getWindowGLContext(window))
    override fun createGraphics(image: Image) =
        if((image as GLImage).context == resourcesContext) ImageGLGraphics(image) else SurfaceImageGLGraphics(image)

    override fun createImage(
        type: PixelType,
        width: Int,
        height: Int,
        data: ByteBuffer?
    ): Image {
        var texId = 0
        var framebuffer = 0
        invokeOnResourceThread {
            texId = if(data != null)
                nCreateTexture(width, height, type.channels, data)
            else nCreateEmptyTexture(width, height, type.channels)
            framebuffer = nBindTextureBuffer(texId)
        }
        return GLImage(texId, framebuffer, type, width, height, width, height, 1f, resourcesContext)
    }

    override fun createSurfaceImage(
        window: WindowPeer,
        type: PixelType,
        physicalWidth: Int,
        physicalHeight: Int,
        logicWidth: Int,
        logicHeight: Int,
        dpi: Float
    ): Image {
        val texId = nCreateEmptyTexture(physicalWidth, physicalHeight, type.channels)
        val framebuffer = nBindTextureBuffer(texId)

        return GLImage(texId, framebuffer, type, physicalWidth, physicalHeight, logicWidth, logicHeight, dpi, getWindowGLContext(window))
    }

    override fun isMainThreadRequired() = true

    override fun createWindow(): WindowPeer {
        lateinit var newWindow: WindowPeer
        invokeOnResourceThread {
            nMakeCurrent(0)
            MainThreadLocker.invoke {
                newWindow = Platform.current.createWindowInstance(nCreateWindow(resourceWindow))
                nMakeCurrent(newWindow.handle)
                nInitContext()
            }
            nMakeCurrent(resourceWindow)
        }
        windows.add(newWindow)
        return newWindow
    }

}