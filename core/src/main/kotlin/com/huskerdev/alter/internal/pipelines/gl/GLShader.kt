package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.internal.c_str
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

    fun set4f(attribute: String, v1: Float, v2: Float, v3: Float, v4: Float) =
        nSetShaderVariable4f(program, BufferUtils.createByteBuffer(*attribute.c_str), v1, v2, v3, v4)

    fun set3f(attribute: String, val1: Float, val2: Float, val3: Float) =
        nSetShaderVariable3f(program, BufferUtils.createByteBuffer(*attribute.c_str), val1, val2, val3)

    fun set(attribute: String, val1: Float) =
        nSetShaderVariable1f(program, BufferUtils.createByteBuffer(*attribute.c_str), val1)

    fun setMatrix(attribute: String, matrix: Matrix4) =
        nSetShaderMatrixVariable(program, BufferUtils.createByteBuffer(*attribute.c_str), BufferUtils.createFloatBuffer(*matrix.elements))

    fun use(){
        if(program == -1)
            program = nCreateShaderProgram(
                BufferUtils.createByteBuffer(*vertex.c_str),
                BufferUtils.createByteBuffer(*fragment.c_str)
            )
        glUseProgram(program)
    }
}