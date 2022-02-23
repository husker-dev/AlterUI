package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader

class D3D9ColorPainter: ColorPainter(), D3D9Painter {

    override val vertex = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl")
    override val pixel = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/colorFragment.hlsl")

    override fun enable() {
        pixel.apply()
        vertex.apply()
    }

    override fun disable() {}

    override fun updateColor() = pixel.set4f("u_Color", color.r, color.g, color.b, color.a)
}