package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreatePixelShader
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateVertexShader
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nGetShaderVariableHandle
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetPixelShader
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetShaderMatrix
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetShaderValue1f
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetShaderValue3f
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetShaderValue4f
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetVertexShader
import com.huskerdev.alter.internal.utils.BufferUtils

enum class D3D9ShaderType {
    Pixel,
    Vertex
}

class D3D9Shader(private val content: String, private val type: D3D9ShaderType) {

    companion object {
        fun pixelFromResources(path: String) = D3D9Shader(this::class.java.getResourceAsStream(path)!!.reader().readText(), D3D9ShaderType.Pixel)
        fun vertexFromResources(path: String) = D3D9Shader(this::class.java.getResourceAsStream(path)!!.reader().readText(), D3D9ShaderType.Vertex)
    }

    var pointer = -1L

    fun compile(): D3D9Shader{
        pointer = if(type == D3D9ShaderType.Pixel)
            nCreatePixelShader(
                BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                content.length
            )
        else
            nCreateVertexShader(
                BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                content.length
            )
        return this
    }

    fun apply(){
        if(type == D3D9ShaderType.Pixel)
            nSetPixelShader(pointer)
        else
            nSetVertexShader(pointer)
    }

    fun getVariableHandler(name: String) =
        nGetShaderVariableHandle(pointer, BufferUtils.createByteBuffer(*name.c_str))

    fun set3f(varHandle: Long, v1: Float, v2: Float, v3: Float) =
        nSetShaderValue3f(pointer, varHandle, v1, v2, v3)

    fun set4f(varHandle: Long, v1: Float, v2: Float, v3: Float, v4: Float) =
        nSetShaderValue4f(pointer, varHandle, v1, v2, v3, v4)

    operator fun set(varHandle: Long, v: Float) =
        nSetShaderValue1f(pointer, varHandle, v)

    fun setMatrix(varHandle: Long, matrix: Matrix4) =
        nSetShaderMatrix(pointer, varHandle, BufferUtils.createFloatBuffer(*matrix.elements))

}