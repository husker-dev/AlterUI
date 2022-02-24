package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.internal.c_str
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

    fun apply(){
        if(pointer == -1L) {
            pointer = if(type == D3D9ShaderType.Pixel)
                D3D9Pipeline.nCreatePixelShader(
                    BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                    content.length
                )
            else
                D3D9Pipeline.nCreateVertexShader(
                    BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                    content.length
                )
        }
        if(type == D3D9ShaderType.Pixel)
            D3D9Pipeline.nSetPixelShader(pointer)
        else
            D3D9Pipeline.nSetVertexShader(pointer)
    }

    fun set3f(name: String, v1: Float, v2: Float, v3: Float){
        if(pointer != -1L)
            D3D9Pipeline.nSetShaderValue3f(pointer, BufferUtils.createByteBuffer(*name.c_str), v1, v2, v3)
    }

    fun set4f(name: String, v1: Float, v2: Float, v3: Float, v4: Float){
        if(pointer != -1L)
            D3D9Pipeline.nSetShaderValue4f(pointer, BufferUtils.createByteBuffer(*name.c_str), v1, v2, v3, v4)
    }

    fun set(name: String, v: Float){
        if(pointer != -1L)
            D3D9Pipeline.nSetShaderValue1f(pointer, BufferUtils.createByteBuffer(*name.c_str), v)
    }

    fun setMatrix(name: String, matrix: Matrix4){
        if(pointer != -1L)
            D3D9Pipeline.nSetShaderMatrix(pointer, BufferUtils.createByteBuffer(*name.c_str), BufferUtils.createFloatBuffer(*matrix.elements))
    }
}