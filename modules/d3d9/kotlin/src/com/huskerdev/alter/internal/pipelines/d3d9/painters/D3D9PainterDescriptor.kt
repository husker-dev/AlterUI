package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexPaintHelper
import com.huskerdev.alter.internal.pipelines.d3d9.*
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nBeginScene
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nEndScene


abstract class D3D9PainterDescriptor {

    private var shadersLoaded = false

    private var varRenderType = 0L
    private var varViewportWidth = 0L
    private var varViewportHeight = 0L
    private var varDpi = 0L
    private var varTextureBounds = 0L
    private var varTextureColors = 0L
    private var varColorChannels = 0L

    private var lastViewportWidth = 0f
    private var lastViewportHeight = 0f
    private var lastDpi = 0f

    lateinit var graphics: Graphics
    private val device = D3D9Pipeline.device

    lateinit var vertex: D3D9Shader
    lateinit var pixel: D3D9Shader

    abstract fun initShaders()

    fun onBeginPaint(graphics: Graphics){
        this.graphics = graphics
        graphics as D3D9Graphics

        if(!shadersLoaded){
            shadersLoaded = true
            initShaders()

            varRenderType = pixel.getVariableHandler("u_RenderType")
            varViewportWidth = vertex.getVariableHandler("u_ViewportWidth")
            varViewportHeight = vertex.getVariableHandler("u_ViewportHeight")
            varDpi = pixel.getVariableHandler("u_Dpi")

            varTextureBounds = pixel.getVariableHandler("u_TextureBounds")
            varTextureColors = pixel.getVariableHandler("u_TextureColors")
        }
        device.vertexShader = vertex
        device.pixelShader = pixel
        device.surface = graphics.surface
        updateShaderViewport(graphics)
        nBeginScene()
    }

    fun onEndPaint(){
        nEndScene()
    }

    private fun updateShaderViewport(graphics: Graphics) {
        if(lastViewportWidth != graphics.width) {
            lastViewportWidth = graphics.width
            vertex[varViewportWidth] = graphics.width
        }
        if(lastViewportHeight != graphics.height) {
            lastViewportHeight = graphics.height
            vertex[varViewportHeight] = graphics.height
        }
        if(lastDpi != graphics.dpi) {
            lastDpi = graphics.dpi
            pixel[varDpi] = graphics.dpi
        }
    }

    fun clear() = D3D9Pipeline.device.clear()
    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        pixel[varRenderType] = 1f
        VertexPaintHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        pixel[varRenderType] = 2f
        VertexPaintHelper.drawRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        pixel[varRenderType] = 3f
        pixel[varTextureColors] = image.pixelType.channels.toFloat()
        pixel.set4f(varTextureBounds, x, y, width, height)
        device.bindTexture(0, (image as D3D9Image).texture)
        VertexPaintHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawText(image: Image, x: Float, y: Float, width: Float, height: Float) {
        pixel[varRenderType] = 4f
        VertexPaintHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }
}