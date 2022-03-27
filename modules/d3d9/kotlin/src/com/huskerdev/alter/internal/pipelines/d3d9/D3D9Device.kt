package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.painters.VertexPaintHelper
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nClear
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nDrawArrays
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetPixelShader
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetRenderTarget
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetVertexShader
import java.nio.FloatBuffer

class D3D9Device {

    var pixelShader: D3D9Shader? = null
        set(value) {
            if(field != value && value != null)
                nSetPixelShader(value.pointer)
            field = value
        }
    var vertexShader: D3D9Shader? = null
        set(value) {
            if(field != value && value != null)
                nSetVertexShader(value.pointer)
            field = value
        }

    var surface: Long = 0
        set(value) {
            if(value != field){
                field = value
                nSetRenderTarget(value)
            }
        }

    fun clear() = nClear()

    fun drawVertices(buffer: FloatBuffer, count: Int, type: VertexPaintHelper.DrawType) =
        nDrawArrays(buffer, count, type.ordinal + 1)

    fun bindTexture(index: Int, texture: Long) =
        nSetTexture(index, texture)
}