package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.pipelines.gl.GLShader
import java.nio.FloatBuffer

object GLColorPainter: ColorPainter(), GLPainter {

    private val shader = GLShader.fromResources(
        "/com/huskerdev/alter/resources/gl/colorVertex.glsl",
        "/com/huskerdev/alter/resources/gl/colorFragment.glsl"
    )

    override fun enable() {
        shader.use()
        updateColor()
    }

    override fun updateMatrix(matrix: Matrix) = shader.setMatrix("u_Matrix", matrix)
    override fun updateColor() = shader.set4f("u_Color", color.r, color.g, color.b, color.a)

    override fun disable() {}
}