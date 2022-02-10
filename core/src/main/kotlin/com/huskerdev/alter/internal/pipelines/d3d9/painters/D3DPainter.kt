package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Graphics
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import java.nio.FloatBuffer


interface D3DPainter: VertexPainter {
    fun updateMatrix(matrix: Matrix)

    override fun drawVertices(vertices: FloatBuffer, points: Int) {
        D3D9Pipeline.nDrawArrays(D3D9Graphics.currentDevice, vertices, points)
    }
}