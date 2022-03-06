package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.graphics.painters.VertexDrawType
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Image
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetLinearFiltering
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader
import com.huskerdev.alter.internal.pipelines.gl.painters.GLImagePainter
import java.nio.FloatBuffer

class D3D9ImagePainter: ImagePainter(), D3D9Painter {

    override val vertex = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl")
    override val pixel = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/textureFragment.hlsl")

    override fun enable() {
        vertex.apply()
        pixel.apply()
    }

    override fun disable() {}

    override fun drawVertices(vertices: FloatBuffer, points: Int, type: VertexDrawType) {
        nSetLinearFiltering(image!!.linearFiltered)
        super.drawVertices(vertices, points, type)
    }

    override fun updateColor() = pixel.set4f("u_Color", color.r, color.g, color.b, color.a)
    override fun updateSize() = pixel.set4f("u_Bounds", x, y, width, height)
    override fun updateImage() {
        pixel.set("u_TextureColors", image!!.type.channels.toFloat())
        nSetTexture((image as D3D9Image).ptr)
    }

}