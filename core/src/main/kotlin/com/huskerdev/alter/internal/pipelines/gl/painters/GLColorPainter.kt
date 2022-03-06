package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.gl.GLShader

object GLColorPainter: ColorPainter(), GLPainter {

    override val shader = GLShader.fromResources(
        "/com/huskerdev/alter/resources/gl/shaders/defaultVertex.glsl",
        "/com/huskerdev/alter/resources/gl/shaders/colorFragment.glsl"
    )
    override var matrix = Matrix4.identity

    override fun enable() = shader.use()
    override fun disable() {}

    override fun updateColor() = shader.set4f("u_Color", color.r, color.g, color.b, color.a)
}