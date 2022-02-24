package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.internal.c_str

import com.huskerdev.alter.internal.utils.BufferUtils

enum class D3D11ShaderType {
    Pixel,
    Vertex
}

class D3D11Shader(private val content: String, private val type: D3D11ShaderType) {


    companion object {
        fun pixelFromResources(path: String) = D3D11Shader(this::class.java.getResourceAsStream(path)!!.reader().readText(), D3D11ShaderType.Pixel)
        fun vertexFromResources(path: String) = D3D11Shader(this::class.java.getResourceAsStream(path)!!.reader().readText(), D3D11ShaderType.Vertex)
    }

    var pointer = -1L

    fun apply(){
        if(pointer == -1L) {
            pointer = if(type == D3D11ShaderType.Pixel)
                D3D11Pipeline.nCreatePixelShader(
                    BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                    content.length
                )
            else
                D3D11Pipeline.nCreateVertexShader(
                    BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                    content.length
                )
        }
        if(type == D3D11ShaderType.Pixel)
            D3D11Pipeline.nSetPixelShader(pointer)
        else
            D3D11Pipeline.nSetVertexShader(pointer)
    }

    fun set3f(name: String, v1: Float, v2: Float, v3: Float){
        if(pointer != -1L)
            D3D11Pipeline.nSetShaderValue3f(pointer, BufferUtils.createByteBuffer(*name.c_str), v1, v2, v3)
    }

    fun set4f(name: String, v1: Float, v2: Float, v3: Float, v4: Float){
        if(pointer != -1L)
            D3D11Pipeline.nSetShaderValue4f(pointer, BufferUtils.createByteBuffer(*name.c_str), v1, v2, v3, v4)
    }

    fun set(name: String, v: Float){
        if(pointer != -1L)
            D3D11Pipeline.nSetShaderValue1f(pointer, BufferUtils.createByteBuffer(*name.c_str), v)
    }

    fun setMatrix(name: String, matrix: Matrix4){
        if(pointer != -1L)
            D3D11Pipeline.nSetShaderMatrix(pointer, BufferUtils.createByteBuffer(*name.c_str), BufferUtils.createFloatBuffer(*matrix.elements))
    }
}