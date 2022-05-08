package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

open class GLContext {

    companion object {
        const val GL_COLOR_BUFFER_BIT = 0x4000
        const val GL_DEPTH_BUFFER_BIT = 0x100
        const val GL_TEXTURE_2D = 0xDE1
        const val GL_FRAMEBUFFER = 0x8D40

        @JvmStatic private external fun nglClear(mask: Int)
        @JvmStatic private external fun nglViewport(x: Int, y: Int, width: Int, height: Int)
        @JvmStatic private external fun nglBindTexture(target: Int, texture: Int)
        @JvmStatic private external fun nglBindFramebuffer(n: Int, buffer: Int)
        @JvmStatic private external fun nglFlush()
        @JvmStatic private external fun nglFinish()

        @JvmStatic private external fun nDrawArray(array: FloatBuffer, count: Int, type: Int)
        @JvmStatic private external fun nCreateTexture(width: Int, height: Int, channels: Int): Int
        @JvmStatic private external fun nCreateEmptyMSAATexture(width: Int, height: Int, channels: Int, samples: Int): Int
        @JvmStatic private external fun nCreateMSAATexture(width: Int, height: Int, channels: Int, samples: Int, data: ByteBuffer): Int
        @JvmStatic private external fun nCreateTextureFramebuffer(texture: Int, isMSAA: Boolean): Int
        @JvmStatic private external fun nglBlitFramebuffer(source: Int, target: Int, width: Int, height: Int)
        @JvmStatic private external fun nSetLinearFiltering(tex: Int, linearFiltering: Boolean)
        @JvmStatic private external fun nReadPixels(framebuffer: Int, channels: Int, x: Int, y: Int, width: Int, height: Int): ByteBuffer
        @JvmStatic private external fun nglDeleteTexture(tex: Int)
        @JvmStatic private external fun nglDeleteFramebuffer(tex: Int)

        // Shader
        @JvmStatic private external fun nCreateShaderProgram(vertexSource: ByteBuffer, fragmentSource: ByteBuffer): Int
        @JvmStatic private external fun nglGetUniformLocation(program: Int, name: ByteBuffer): Int
        @JvmStatic private external fun nglUniform4f(program: Int, location: Int, val1: Float, val2: Float, val3: Float, val4: Float)
        @JvmStatic private external fun nglUniform3f(program: Int, location: Int, val1: Float, val2: Float, val3: Float)
        @JvmStatic private external fun nglUniform1f(program: Int, location: Int, val1: Float)
        @JvmStatic private external fun nglUniform1i(location: Int, v0: Int)
        @JvmStatic private external fun nglUseProgram(program: Int)
    }

    private var viewportWidth = 0
    private var viewportHeight = 0
    private val boundTextures = IntArray(5)

    open var framebuffer = 0
        set(value) {
            field = value
            nglBindFramebuffer(GL_FRAMEBUFFER, value)
        }

    open var shader: GLShader? = null
        set(value) {
            field = value
            if(value != null)
                nglUseProgram(value.program)
        }

    // ----------
    // Basics
    // ----------

    open fun glClear() =
        nglClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    open fun glViewport(width: Int, height: Int){
        if(width != viewportWidth || height != viewportHeight) {
            viewportWidth = width
            viewportHeight = height
            nglViewport(0, 0, width, height)
        }
    }

    open fun glBindTexture(index: Int, texture: Int){

            boundTextures[index] = texture
            nglBindTexture(GL_TEXTURE_2D + index, texture)

    }

    open fun glFlush() =
        nglFlush()

    open fun glFinish() =
        nglFinish()

    // ----------
    // Textures
    // ----------

    open fun createTexture(width: Int, height: Int, channels: Int) =
        nCreateTexture(width, height, channels)

    open fun createMSAATexture(width: Int, height: Int, channels: Int, samples: Int, data: ByteBuffer?): Int {
        return if(data == null)
            nCreateEmptyMSAATexture(width, height, channels, samples)
        else nCreateMSAATexture(width, height, channels, samples, data)
    }

    open fun createTextureFramebuffer(texture: Int, isMSAA: Boolean) =
        nCreateTextureFramebuffer(texture, isMSAA)

    open fun glBlitFramebuffer(source: Int, target: Int, width: Int, height: Int) =
        nglBlitFramebuffer(source, target, width, height)

    open fun drawArray(array: FloatBuffer, count: Int, type: VertexHelper.DrawType) =
        nDrawArray(array, count, type.ordinal)

    open fun setLinearFiltering(texture: Int, linear: Boolean) =
        nSetLinearFiltering(texture, linear)

    open fun readPixels(image: Image, x: Int, y: Int, width: Int, height: Int) =
        nReadPixels((image as GLImage).renderTarget.framebuffer, image.pixelType.channels, x, y, width, height)

    open fun glDeleteTexture(texture: Int) =
        nglDeleteTexture(texture)

    open fun glDeleteFramebuffer(framebuffer: Int) =
        nglDeleteFramebuffer(framebuffer)

    // ----------
    // Shaders
    // ----------

    open fun glGetUniformLocation(shader: GLShader, name: String) =
        nglGetUniformLocation(shader.program, BufferUtils.createByteBuffer(*name.c_str))

    open fun glUniform1f(shader: GLShader, location: Int, val1: Float) =
        nglUniform1f(shader.program, location, val1)

    open fun glUniform1f(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float) =
        nglUniform3f(shader.program, location, val1, val2, val3)

    open fun glUniform1f(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float, val4: Float) =
        nglUniform4f(shader.program, location, val1, val2, val3, val4)

    open fun glUniform1i(shader: GLShader, location: Int, index: Int) =
        nglUniform1i(location, index)

    open fun createShaderProgram(vertex: String, fragment: String): Int {
        return nCreateShaderProgram(
            BufferUtils.createByteBuffer(*vertex.c_str),
            BufferUtils.createByteBuffer(*fragment.c_str)
        )
    }
}
