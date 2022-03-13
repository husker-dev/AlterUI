package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.painters.VertexPaintHelper
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader
import java.nio.FloatBuffer


interface D3D9Painter {

    val vertex: D3D9Shader
    val pixel: D3D9Shader

    //fun updateHeight(height: Float) = pixel.set("u_Height", height)
    //fun updateDpi(dpi: Float) = pixel.set("u_Dpi", dpi)
    //fun updateMatrix(matrix: Matrix4) = vertex.setMatrix("u_Matrix", matrix)

    fun drawVertices(buffer: FloatBuffer,
                     count: Int,
                     type: VertexPaintHelper.DrawType) {
        D3D9Pipeline.nDrawArrays(buffer, count, type.ordinal + 1)
    }
}