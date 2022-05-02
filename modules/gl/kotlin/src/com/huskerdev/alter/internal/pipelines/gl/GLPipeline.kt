package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.AlterUIProperties
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

        lateinit var resourceContext: GLResourceContext
        val contexts = hashMapOf<WindowPeer, GLContext>()

        @JvmStatic external fun nCreateWindow(shareWith: Long, msaa: Int): Long
        @JvmStatic external fun nMakeCurrent(handle: Long)
        @JvmStatic external fun nSwapBuffers(handle: Long)
    }

    override fun load() {
        super.load()
        MainThreadLocker.invoke {
            resourceContext = GLResourceContext()
        }
    }

    private fun getWindowGLContext(window: WindowPeer): GLContext{
        if(window !in contexts)
            contexts[window] = GLContext()
        return contexts[window]!!
    }

    override fun createGraphics(window: WindowPeer) = WindowGLGraphics(window, getWindowGLContext(window))
    override fun createGraphics(image: Image) = ImageGLGraphics(image as GLImage, resourceContext)

    override fun createImage(
        type: PixelType,
        width: Int,
        height: Int,
        data: ByteBuffer?
    ) = GLImage(type, width, height, width, height, 1f, data, resourceContext)

    override fun createSurfaceImage(
        window: WindowPeer,
        type: PixelType,
        physicalWidth: Int,
        physicalHeight: Int,
        logicWidth: Int,
        logicHeight: Int,
        dpi: Float
    ) = GLImage(type, physicalWidth, physicalHeight, logicWidth, logicHeight, dpi, null, resourceContext)

    override fun isMainThreadRequired() = true

    override fun createWindow(): WindowPeer {
        lateinit var newWindow: WindowPeer
        resourceContext.invokeOnResourceThread {
            nMakeCurrent(0)
            MainThreadLocker.invoke {
                newWindow = Platform.current.createWindowInstance(nCreateWindow(resourceContext.windowHandle, AlterUIProperties.msaa))
                nMakeCurrent(newWindow.handle)
            }
            nMakeCurrent(resourceContext.windowHandle)
        }
        windows.add(newWindow)
        return newWindow
    }
}