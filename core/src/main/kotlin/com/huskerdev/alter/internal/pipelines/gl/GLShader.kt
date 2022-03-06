package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glGetUniformLocation
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nCreateShaderProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glUseProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderMatrixVariable
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable1f
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable3f
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetShaderVariable4f
import com.huskerdev.alter.internal.utils.BufferUtils


class GLShader(private val vertex: String, private val fragment: String) {

    companion object {
        fun fromResources(vertexPath: String, fragmentPath: String) =
            GLShader(
                this::class.java.getResourceAsStream(vertexPath)!!.reader().readText(),
                this::class.java.getResourceAsStream(fragmentPath)!!.reader().readText()
            )
    }

    private var program = -1
    private var cachedLocations = hashMapOf<String, Int>()

    fun set4f(attribute: String, v1: Float, v2: Float, v3: Float, v4: Float) =
        nSetShaderVariable4f(program, getLocation(attribute), v1, v2, v3, v4)

    fun set3f(attribute: String, val1: Float, val2: Float, val3: Float) =
        nSetShaderVariable3f(program, getLocation(attribute), val1, val2, val3)

    fun set(attribute: String, val1: Float) =
        nSetShaderVariable1f(program, getLocation(attribute), val1)

    fun setMatrix(attribute: String, matrix: Matrix4) =
        nSetShaderMatrixVariable(program, getLocation(attribute), BufferUtils.createFloatBuffer(*matrix.elements))

    private fun getLocation(name: String): Int{
        if(name !in cachedLocations)
            cachedLocations[name] = glGetUniformLocation(program, BufferUtils.createByteBuffer(*name.c_str))
        return cachedLocations[name]!!
    }

    fun use(){
        if(program == -1)
            program = nCreateShaderProgram(
                BufferUtils.createByteBuffer(*vertex.c_str),
                BufferUtils.createByteBuffer(*fragment.c_str)
            )
        glUseProgram(program)
    }
}