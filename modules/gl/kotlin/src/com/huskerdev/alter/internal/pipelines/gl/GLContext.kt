package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexPaintHelper
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_COLOR_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_DEPTH_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_FRAMEBUFFER
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glBindFramebuffer
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glBindTexture
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glFinish
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glFlush
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glGetUniformLocation
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glUniform1i
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glUseProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glViewport
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.invokeOnResourceThread
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nCreateShaderProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nReadPixels
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nReleaseFrameBuffer
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nReleaseTexture
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetLinearFiltering
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable1f
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable3f
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable4f
import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

open class GLContext(val window: Long) {

    private var viewportWidth = 0
    private var viewportHeight = 0
    private var boundTexture0 = 0
    private var boundTexture1 = 0

    open var boundFramebuffer = 0
        set(value) {
            if(field != value)
                glBindFramebuffer(GL_FRAMEBUFFER, value)
            field = value
        }

    open var shader: GLShader? = null
        set(value) {
            if(field != value) {
                field = value
                glUseProgram(value!!.program)
            }
        }

    open fun flush() =
        glFlush()

    open fun finish() =
        glFinish()

    open fun getShaderVariable(shader: GLShader, name: String) =
        glGetUniformLocation(shader.program, BufferUtils.createByteBuffer(*name.c_str))

    open fun setShaderVariable(shader: GLShader, location: Int, val1: Float) =
        nSetShaderVariable1f(shader.program, location, val1)

    open fun setShaderVariable(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float) =
        nSetShaderVariable3f(shader.program, location, val1, val2, val3)

    open fun setShaderVariable(shader: GLShader, location: Int, val1: Float, val2: Float, val3: Float, val4: Float) =
        nSetShaderVariable4f(shader.program, location, val1, val2, val3, val4)

    open fun defineShaderTexture(shader: GLShader, location: Int, index: Int) =
        glUniform1i(location, index)

    open fun createShaderProgram(vertex: String, fragment: String): Int{
        return nCreateShaderProgram(
            BufferUtils.createByteBuffer(*vertex.c_str),
            BufferUtils.createByteBuffer(*fragment.c_str)
        )
    }

    open fun bindTexture(index: Int, id: Int){
        if(index == 0 && boundTexture0 != id)
            glBindTexture(GLPipeline.GL_TEXTURE_2D, id)
        if(index == 1 && boundTexture1 != id)
            glBindTexture(GLPipeline.GL_TEXTURE_2D + 1, id)
    }

    open fun setViewport(width: Int, height: Int){
        if(width != viewportWidth || height != viewportHeight) {
            viewportWidth = width
            viewportHeight = height
            glViewport(0, 0, width, height)
        }
    }

    open fun drawArray(array: FloatBuffer, count: Int, type: VertexPaintHelper.DrawType) =
        GLPipeline.nDrawArray(array, count, type.ordinal)

    open fun clear() =
        GLPipeline.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    fun setLinearFiltering(texId: Int, linear: Boolean) =
        nSetLinearFiltering(texId, linear)

    fun readPixels(image: Image, x: Int, y: Int, width: Int, height: Int) =
        nReadPixels((image as GLImage).framebuffer, image.pixelType.channels, x, y, width, height)

    fun releaseTexture(texId: Int) =
        nReleaseTexture(texId)

    fun releaseFrameBuffer(framebuffer: Int) =
        nReleaseFrameBuffer(framebuffer)

}
