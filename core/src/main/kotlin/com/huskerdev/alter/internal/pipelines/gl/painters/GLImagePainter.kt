package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.pipelines.gl.GLImage
import com.huskerdev.alter.internal.pipelines.gl.GLShader

object GLImagePainter: ImagePainter() {

    private val descriptor = object: GLPainterDescriptor(){
        var colorVar = 0
        var boundsVar = 0
        var textureColorsVar = 0

        override fun initShader() {
            shader = GLShader.fromResources(
                "/com/huskerdev/alter/resources/gl/shaders/defaultVertex.glsl",
                "/com/huskerdev/alter/resources/gl/shaders/textureFragment.glsl"
            )
            shader.compile(context)
            context.shader = shader // TODO: Is it necessary?
            shader.defineTextureVariable(context, "u_Texture1", 1)
            colorVar = shader[context, "u_Color"]
            boundsVar = shader[context, "u_Bounds"]
            textureColorsVar = shader[context, "u_TextureColors"]
        }
    }

    override fun onEnable() {
        isImageChanged = true
    }

    override fun onBeginPaint(graphics: Graphics) {
        descriptor.onBeginPaint(graphics)
        super.onBeginPaint(graphics)
    }

    override fun onEndPaint() {
        descriptor.onEndPaint()
        super.onEndPaint()
    }

    override fun updateColor() = descriptor.shader.set4f(descriptor.context, descriptor.colorVar, color.r, color.g, color.b, color.a)
    override fun updateSize() = descriptor.shader.set4f(descriptor.context, descriptor.boundsVar, x, y, width, height)
    override fun updateImage() {
        descriptor.shader[descriptor.context, descriptor.textureColorsVar] = image!!.pixelType.channels.toFloat()
        descriptor.context.bindTexture(1, (image as GLImage).texId)
    }

    override fun clear() = descriptor.clear()
    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = descriptor.fillRect(x, y, width, height)
    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = descriptor.drawRect(x, y, width, height)
    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawImage(image, x, y, width, height)
    override fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawText(textImage, x, y, width, height)
}