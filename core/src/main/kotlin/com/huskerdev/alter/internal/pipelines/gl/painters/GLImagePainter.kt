package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.pipelines.gl.GLImage
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_TEXTURE_2D
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glBindTexture
import com.huskerdev.alter.internal.pipelines.gl.GLShader

object GLImagePainter: ImagePainter(), GLPainter {

    override val shader = GLShader.fromResources(
        "/com/huskerdev/alter/resources/gl/shaders/defaultVertex.glsl",
        "/com/huskerdev/alter/resources/gl/shaders/textureFragment.glsl"
    )
    override var matrix = Matrix4.identity

    override fun enable() = shader.use()
    override fun disable() {}

    override fun updateColor() = shader.set4f("u_Color", color.r, color.g, color.b, color.a)
    override fun updateSize() = shader.set4f("u_Bounds", x, y, width, height)
    override fun updateImage() {
        shader.set("u_TextureColors", image!!.type.channels.toFloat())
        glBindTexture(GL_TEXTURE_2D, (image as GLImage).texId)
    }

}