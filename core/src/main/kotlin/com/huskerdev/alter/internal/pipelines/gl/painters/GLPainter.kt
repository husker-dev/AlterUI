package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.VertexDrawType
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.pipelines.gl.GLShader
import java.nio.FloatBuffer

interface GLPainter: VertexPainter {

    val shader: GLShader

    fun updateMatrix(matrix: Matrix) = shader.setMatrix("u_Matrix", matrix)

    override fun drawVertices(vertices: FloatBuffer, points: Int, type: VertexDrawType) {
        GLPipeline.drawArray(vertices, points, type.ordinal)
    }

}

