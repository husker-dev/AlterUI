package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.device
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.InputStream

enum class D3D9ShaderType {
    Pixel,
    Vertex
}

class D3D9Shader(private var content: String, private val type: D3D9ShaderType) {

    companion object {
        fun pixelFromResources(path: String) = D3D9Shader(this::class.java.getResourceAsStream(path)!!.readAsText(), D3D9ShaderType.Pixel)
        fun vertexFromResources(path: String) = D3D9Shader(this::class.java.getResourceAsStream(path)!!.readAsText(), D3D9ShaderType.Vertex)

        private fun InputStream.readAsText(): String{
            val text = reader().readText()
            close()
            return text
        }
    }

    var pointer = -1L

    fun compile(): D3D9Shader{
        pointer = if(type == D3D9ShaderType.Pixel)
            device.createPixelShader(
                BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                content.length
            )
        else
            device.createVertexShader(
                BufferUtils.createByteBuffer(*content.encodeToByteArray()),
                content.length
            )
        content = ""
        return this
    }

    fun getVariableHandler(name: String) =
        device.getShaderVariableHandle(pointer, BufferUtils.createByteBuffer(*name.c_str))

    fun set(varHandle: Long, v1: Float, v2: Float, v3: Float) =
        device.setShaderValue(pointer, varHandle, v1, v2, v3)

    fun set(varHandle: Long, v1: Float, v2: Float, v3: Float, v4: Float) =
        device.setShaderValue(pointer, varHandle, v1, v2, v3, v4)

    operator fun set(varHandle: Long, v: Float) =
        device.setShaderValue(pointer, varHandle, v)



}