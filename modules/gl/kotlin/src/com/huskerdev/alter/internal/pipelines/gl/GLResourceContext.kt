package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nCreateWindow
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nMakeCurrent
import com.huskerdev.alter.internal.utils.Trigger
import com.huskerdev.alter.internal.utils.kotlin.unique
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class GLResourceContext: GLContext() {

    var windowHandle = 0L
    var thread: Thread? = null

    private val queue = LinkedBlockingQueue<() -> Unit>()

    init {
        val readyTrigger = Trigger()
        thread = thread(name = "Alter OpenGL resource", isDaemon = true) {
            windowHandle = nCreateWindow(0, 0)
            nMakeCurrent(windowHandle)

            readyTrigger.ready()

            while(true)
                queue.take().invoke()
        }
        readyTrigger.waitForReady()
    }

    fun <T> invokeOnResourceThread(run: () -> T): T {
        return if(Thread.currentThread() == thread)
            run()
        else {
            var result: T? = null
            val trigger = Trigger()
            queue.offer {
                result = run()
                trigger.ready()
            }
            trigger.waitForReady()

            result!!
        }
    }

    fun invokeOnResourceThreadAsync(run: () -> Unit){
        if(Thread.currentThread() == thread)
            run()
        else queue.offer(run)
    }

    override var framebuffer = 0
        set(value) {
            field = value
            invokeOnResourceThread {
                super.framebuffer = value
            }
        }

    override var shader by unique<GLShader?>(null){
        invokeOnResourceThread {
            super.shader = it
        }
    }

    override fun glClear() = invokeOnResourceThread {
        super.glClear()
    }

    override fun glViewport(width: Int, height: Int) = invokeOnResourceThread {
        super.glViewport(width, height)
    }

    override fun glBindTexture(index: Int, texture: Int) = invokeOnResourceThread {
        super.glBindTexture(index, texture)
    }

    override fun glFlush() = invokeOnResourceThread {
        super.glFlush()
    }

    override fun glFinish() = invokeOnResourceThread {
        super.glFinish()
    }

    override fun createTexture(width: Int, height: Int, channels: Int) = invokeOnResourceThread {
        super.createTexture(width, height, channels)
    }

    override fun createMSAATexture(width: Int, height: Int, channels: Int, samples: Int, data: ByteBuffer?) = invokeOnResourceThread {
        super.createMSAATexture(width, height, channels, samples, data)
    }

    override fun createTextureFramebuffer(texture: Int, isMSAA: Boolean) = invokeOnResourceThread {
        super.createTextureFramebuffer(texture, isMSAA)
    }

    override fun glBlitFramebuffer(source: Int, target: Int, width: Int, height: Int) = invokeOnResourceThread {
        super.glBlitFramebuffer(source, target, width, height)
    }

    override fun drawArray(array: FloatBuffer, count: Int, type: VertexHelper.DrawType) = invokeOnResourceThread {
        super.drawArray(array, count, type)
    }

    override fun setLinearFiltering(texture: Int, linear: Boolean) = invokeOnResourceThread {
        super.setLinearFiltering(texture, linear)
    }

    override fun readPixels(image: Image, x: Int, y: Int, width: Int, height: Int) = invokeOnResourceThread {
        super.readPixels(image, x, y, width, height)
    }

    override fun glDeleteTexture(texture: Int) = invokeOnResourceThread {
        super.glDeleteTexture(texture)
    }

    override fun glDeleteFramebuffer(framebuffer: Int) = invokeOnResourceThread {
        super.glDeleteFramebuffer(framebuffer)
    }

    override fun glGetUniformLocation(shader: GLShader, name: String) = invokeOnResourceThread {
        super.glGetUniformLocation(shader, name)
    }

    override fun glUniform1f(shader: GLShader, location: Int, val1: Float) = invokeOnResourceThread {
        super.glUniform1f(shader, location, val1)
    }

    override fun glUniform1f(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float) = invokeOnResourceThread {
        super.glUniform1f(shader, location, val1, val2, val3)
    }

    override fun glUniform1f(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float, val4: Float) = invokeOnResourceThread {
        super.glUniform1f(shader, location, val1, val2, val3, val4)
    }

    override fun glUniform1i(shader: GLShader, location: Int, index: Int) = invokeOnResourceThread {
        super.glUniform1i(shader, location, index)
    }

    override fun createShaderProgram(vertex: String, fragment: String) = invokeOnResourceThread {
        super.createShaderProgram(vertex, fragment)
    }
}