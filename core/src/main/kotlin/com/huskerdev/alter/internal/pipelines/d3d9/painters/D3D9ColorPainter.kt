package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader

class D3D9ColorPainter: ColorPainter(), D3D9Painter {

    override val vertex = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl")
    override val pixel = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/colorFragment.hlsl")
    override fun updateColor() {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }


    //override fun updateColor() = pixel.set4f("u_Color", color.r, color.g, color.b, color.a)
    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        TODO("Not yet implemented")
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        TODO("Not yet implemented")
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        TODO("Not yet implemented")
    }
}