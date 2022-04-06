package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.gl.GLShader

object GLColorPainter: ColorPainter() {

    private val descriptor = object: GLPainterDescriptor(){
        var colorVar = 0

        override fun initShader() {
            shader = GLShader.fromResources(
                "/com/huskerdev/alter/resources/gl/shaders/defaultVertex.glsl",
                "/com/huskerdev/alter/resources/gl/shaders/colorFragment.glsl"
            )
            shader.compile(context)
            colorVar = shader[context, "u_Color"]
        }
    }

    override fun onBeginPaint(graphics: Graphics) {
        descriptor.onBeginPaint(graphics)
        super.onBeginPaint(graphics)
    }

    override fun onEndPaint() {
        descriptor.onEndPaint()
        super.onEndPaint()
    }

    override fun updateColor() {
        descriptor.shader.set4f(descriptor.context, descriptor.colorVar, color.r, color.g, color.b, color.a)
    }

    override fun clear() = descriptor.clear()
    override fun fillShape(shape: Shape) = descriptor.fillShape(shape)
    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = descriptor.fillRect(x, y, width, height)
    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = descriptor.drawRect(x, y, width, height)
    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawImage(image, x, y, width, height)
    override fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawText(textImage, x, y, width, height)
}