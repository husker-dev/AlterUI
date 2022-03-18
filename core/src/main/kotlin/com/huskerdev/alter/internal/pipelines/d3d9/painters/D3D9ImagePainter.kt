package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.graphics.painters.VertexPaintHelper
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Image
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetLinearFiltering
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader

object D3D9ImagePainter: ImagePainter(), D3D9Painter {

    override lateinit var vertex: D3D9Shader
    override lateinit var pixel: D3D9Shader

    override fun onLoad() {
        super.onLoad()

        vertex = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl")
        pixel = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/textureFragment.hlsl")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun onBeginPaint(graphics: Graphics) {
        super.onBeginPaint(graphics)

        vertex.apply()
        pixel.apply()
        nSetLinearFiltering(image!!.linearFiltered)
    }

    override fun updateColor() {
        TODO("Not yet implemented")
    }

    // Property updates
    //override fun updateColor() = pixel.set4f("u_Color", color.r, color.g, color.b, color.a)
    override fun updateSize() = pixel.set4f("u_Bounds", x, y, width, height)
    override fun updateImage() {
        pixel.set("u_TextureColors", image!!.pixelType.channels.toFloat())
        nSetTexture((image as D3D9Image).ptr)
    }

    // Paint functions
    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        pixel.set("u_RenderType", 1f)
        VertexPaintHelper.fillRect(x, y, width, height, ::drawVertices)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        pixel.set("u_RenderType", 2f)
        VertexPaintHelper.drawRect(x, y, width, height, ::drawVertices)
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        pixel.set("u_RenderType", 3f)
        VertexPaintHelper.fillRect(x, y, width, height, ::drawVertices)
    }

    override fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float) {
        TODO("Not yet implemented")
    }
}