package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.painters.VertexDrawType
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.pipelines.gl.GLShader
import java.nio.FloatBuffer

interface GLPainter: VertexPainter {

    val shader: GLShader

    fun updateMatrix(matrix: Matrix4) = shader.setMatrix("u_Matrix", matrix)
    fun updateDpi(dpi: Float) = shader.set("u_Dpi", dpi)

    override fun drawVertices(vertices: FloatBuffer, points: Int, type: VertexDrawType) {
        GLPipeline.nDrawArray(vertices, points, type.ordinal)
    }

}

