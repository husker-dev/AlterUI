package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader

object D3D9ColorPainter: ColorPainter() {

    private val descriptor = object: D3D9PainterDescriptor(){
        var colorBar = 0L

        override fun initShaders() {
            pixelShader = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/colorFragment.hlsl").compile()
            vertexShader = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl").compile()

            colorBar = pixelShader.getVariableHandler("u_Color")
        }
    }

    override fun updateColor() =
        descriptor.pixelShader.set4f(descriptor.colorBar, color.r, color.g, color.b, color.a)

    override fun onBeginPaint(graphics: Graphics) {
        descriptor.onBeginPaint(graphics)
        super.onBeginPaint(graphics)
    }

    override fun onEndPaint() {
        descriptor.onEndPaint()
        super.onEndPaint()
    }

    override fun clear() = descriptor.clear()
    override fun fillShape(shape: Shape) = descriptor.fillShape(shape)
    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = descriptor.fillRect(x, y, width, height)
    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = descriptor.drawRect(x, y, width, height)
    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawImage(image, x, y, width, height)
    override fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float) = descriptor.drawText(textImage, x, y, width, height)
}