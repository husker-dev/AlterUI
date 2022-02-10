package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import java.nio.FloatBuffer

interface GLPainter: VertexPainter {

    fun updateMatrix(matrix: Matrix)

    override fun drawVertices(vertices: FloatBuffer, points: Int) {
        GLPipeline.drawArray(vertices, points)
    }

}

