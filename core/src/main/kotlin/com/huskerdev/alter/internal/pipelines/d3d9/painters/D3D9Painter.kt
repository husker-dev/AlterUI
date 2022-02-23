package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.VertexDrawType
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader
import java.nio.FloatBuffer


interface D3D9Painter: VertexPainter {

    val vertex: D3D9Shader
    val pixel: D3D9Shader

    fun updateHeight(height: Float) = pixel.set("u_Height", height)
    fun updateMatrix(matrix: Matrix) = vertex.setMatrix("u_Matrix", matrix)

    override fun drawVertices(vertices: FloatBuffer, points: Int, type: VertexDrawType) {
        D3D9Pipeline.nDrawArrays(vertices, points, type.ordinal + 1)
    }
}