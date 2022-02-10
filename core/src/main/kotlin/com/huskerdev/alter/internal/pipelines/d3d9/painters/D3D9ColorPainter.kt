package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.graphics.painters.VertexPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Graphics
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader
import java.nio.FloatBuffer

class D3D9ColorPainter: ColorPainter(), D3DPainter {

    private val pixel = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/colorFragment.hlsl")
    private val vertex = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/colorVertex.hlsl")

    override fun updateMatrix(matrix: Matrix) {
        vertex.setMatrix("mat", matrix)
    }

    override fun enable() {
        pixel.apply()
        vertex.apply()
    }

    override fun disable() {

    }

    override fun updateColor() {
        pixel.set4f("color", color.r, color.g, color.b, color.a)
    }
}