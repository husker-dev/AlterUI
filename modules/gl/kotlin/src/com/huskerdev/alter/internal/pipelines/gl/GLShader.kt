package com.huskerdev.alter.internal.pipelines.gl

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

    fun compile(context: GLContext): GLShader {
        program = context.createShaderProgram(vertex, fragment)
        return this
    }

    fun set(context: GLContext, location: Int, v1: Float, v2: Float, v3: Float, v4: Float) =
        context.glUniform1f(this, location, v1, v2, v3, v4)

    fun set(context: GLContext, location: Int, val1: Float, val2: Float, val3: Float) =
        context.glUniform1f(this, location, val1, val2, val3)

    /*
    fun setMatrix(location: Int, matrix: Matrix4) =
        nSetShaderMatrixVariable(program, location, BufferUtils.createFloatBuffer(*matrix.elements))
     */

    fun defineTextureVariable(context: GLContext, name: String, index: Int) =
        context.glUniform1i(this, getVariableLocation(context, name), index)

    fun getVariableLocation(context: GLContext, name: String) =
        context.glGetUniformLocation(this, name)

    operator fun set(context: GLContext, location: Int, val1: Float) =
        context.glUniform1f(this, location, val1)

    operator fun get(context: GLContext, name: String) = getVariableLocation(context, name)

}