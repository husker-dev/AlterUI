package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nCreateShaderProgram
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.InputStream


class GLShader(val vertex: String, val fragment: String) {

    companion object {
        fun fromResources(vertexPath: String, fragmentPath: String) =
            GLShader(
                this::class.java.getResourceAsStream(vertexPath)!!.readAsText(),
                this::class.java.getResourceAsStream(fragmentPath)!!.readAsText()
            )

        private fun InputStream.readAsText(): String{
            val text = reader().readText()
            close()
            return text
        }
    }

    var program = 0
        private set

    fun compile(context: GLContext) {
        program = context.createShaderProgram(vertex, fragment)
    }

    fun set4f(context: GLContext, location: Int, v1: Float, v2: Float, v3: Float, v4: Float) =
        context.setShaderVariable(this, location, v1, v2, v3, v4)

    fun set3f(context: GLContext, location: Int, val1: Float, val2: Float, val3: Float) =
        context.setShaderVariable(this, location, val1, val2, val3)

    /*
    fun setMatrix(location: Int, matrix: Matrix4) =
        nSetShaderMatrixVariable(program, location, BufferUtils.createFloatBuffer(*matrix.elements))
     */

    fun defineTextureVariable(context: GLContext, name: String, index: Int) =
        context.defineShaderTexture(this, getVariableLocation(context, name), index)

    fun getVariableLocation(context: GLContext, name: String) =
        context.getShaderVariable(this, name)

    operator fun set(context: GLContext, location: Int, val1: Float) =
        context.setShaderVariable(this, location, val1)

    operator fun get(context: GLContext, name: String) = getVariableLocation(context, name)

}