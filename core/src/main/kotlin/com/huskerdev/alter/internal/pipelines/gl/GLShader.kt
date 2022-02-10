package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.createShaderProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glUseProgram
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.setShaderMatrixVariable
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.setShaderVariable3f
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.setShaderVariable4f
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
        setShaderVariable4f(program, BufferUtils.createByteBuffer(*attribute.c_str), v1, v2, v3, v4)

    fun set3f(attribute: String, val1: Float, val2: Float, val3: Float) =
        setShaderVariable3f(program, BufferUtils.createByteBuffer(*attribute.c_str), val1, val2, val3)

    fun setMatrix(attribute: String, matrix: Matrix) =
        setShaderMatrixVariable(program, BufferUtils.createByteBuffer(*attribute.c_str), BufferUtils.createFloatBuffer(*matrix.elements))

    fun use(){
        if(program == -1)
            program = createShaderProgram(
                BufferUtils.createByteBuffer(*vertex.c_str),
                BufferUtils.createByteBuffer(*fragment.c_str)
            )
        glUseProgram(program)
    }
}