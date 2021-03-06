package com.huskerdev.alter.internal.pipelines.gl.painters

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.pipelines.gl.*

abstract class GLPainterDescriptor {

    private var isShaderLoaded = false

    lateinit var shader: GLShader

    private var varRenderType = 0
    private var varViewportWidth = 0
    private var varViewportHeight = 0
    private var varInverseY = 0
    private var varDpi = 0
    private var varTextureBounds = 0
    private var varTextureColors = 0
    private var varColorChannels = 0

    private var lastViewportWidth = 0f
    private var lastViewportHeight = 0f
    private var lastDpi = 0f

    private lateinit var graphics: Graphics
    lateinit var context: GLContext

    abstract fun initShader()

    fun onBeginPaint(graphics: Graphics) {
        this.graphics = graphics
        this.context = (graphics as GLGraphics).context

        if(!isShaderLoaded){
            isShaderLoaded = true
            initShader()
            //context.shader = shader // TODO: Is it necessary?
            shader.defineTextureVariable(context, "u_Texture", 0)

            varRenderType = shader[context, "u_RenderType"]
            varViewportWidth = shader[context, "u_ViewportWidth"]
            varViewportHeight = shader[context, "u_ViewportHeight"]
            varInverseY = shader[context, "u_InverseY"]
            varDpi = shader[context, "u_Dpi"]
            varTextureBounds = shader[context, "u_TextureBounds"]
            varTextureColors = shader[context, "u_TextureColors"]
            varColorChannels = shader[context, "u_ColorChannels"]
        }
        context.shader = shader
        context.framebuffer = graphics.framebuffer
        context.glViewport(graphics.physicalWidth, graphics.physicalHeight)
        updateShaderViewport(graphics)
        shader[context, varColorChannels] = graphics.pixelType.channels.toFloat()
        shader[context, varInverseY] = if(graphics.inverseY) 1f else 0f
    }

    fun onEndPaint(){
        context.glFinish()
    }

    private fun updateShaderViewport(graphics: Graphics) {
        if(lastViewportWidth != graphics.width) {
            lastViewportWidth = graphics.physicalWidth.toFloat()
            shader[context, varViewportWidth] = graphics.physicalWidth.toFloat()
        }
        if(lastViewportHeight != graphics.height) {
            lastViewportHeight = graphics.physicalHeight.toFloat()
            shader[context, varViewportHeight] = graphics.physicalHeight.toFloat()
        }
        if(lastDpi != graphics.dpi) {
            lastDpi = graphics.dpi
            shader[context, varDpi] = graphics.dpi
        }
    }

    fun clear() = context.glClear()

    fun fillShape(shape: Shape) {
        shader[context, varRenderType] = 1f
        VertexHelper.paintVertices(shape.fillVertices, context::drawArray)
    }

    fun drawShape(shape: Shape) {
        shader[context, varRenderType] = 1f
        VertexHelper.paintVertices(shape.drawVertices, context::drawArray)
    }

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        shader[context, varRenderType] = 1f
        VertexHelper.fillRect(x, y, width, height, context::drawArray)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        shader[context, varRenderType] = 2f
        VertexHelper.drawRect(x, y, width, height, context::drawArray)
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        shader[context, varRenderType] = 3f
        shader[context, varTextureColors] = image.pixelType.channels.toFloat()
        shader.set(context, varTextureBounds, x * lastDpi, y * lastDpi, width * lastDpi, height * lastDpi)
        context.glBindTexture(0, (image as GLImage).renderTarget.texture)
        VertexHelper.fillRect(x, y, width, height, context::drawArray)
    }

    fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float) {
        shader[context, varRenderType] = 4f
        shader[context, varTextureColors] = textImage.pixelType.channels.toFloat()
        shader.set(context, varTextureBounds, x, y, width, height)
        context.glBindTexture(0, (textImage as GLImage).renderTarget.texture)
        VertexHelper.fillRect(x, y, width, height, context::drawArray)
    }

}

