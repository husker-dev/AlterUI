package com.huskerdev.alter.internal.pipelines.d3d9.painters

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.pipelines.d3d9.*


abstract class D3D9PainterDescriptor {

    private var shadersLoaded = false

    private var varRenderType = 0L
    private var varViewportWidth = 0L
    private var varViewportHeight = 0L
    private var varDpi = 0L
    private var varTextureBounds = 0L
    private var varTextureColors = 0L
    private var varColorChannels = 0L

    private var forceViewportUpdate = false
    private var lastViewportWidth = 0f
    private var lastViewportHeight = 0f
    private var lastDpi = 0f

    lateinit var graphics: Graphics
    private val device = D3D9Pipeline.device

    lateinit var vertexShader: D3D9Shader
    lateinit var pixelShader: D3D9Shader

    abstract fun initShaders()

    fun onBeginPaint(graphics: Graphics){
        this.graphics = graphics
        graphics as D3D9Graphics

        if(!shadersLoaded){
            shadersLoaded = true
            initShaders()

            varRenderType = pixelShader.getVariableHandler("u_RenderType")
            varViewportWidth = vertexShader.getVariableHandler("u_ViewportWidth")
            varViewportHeight = vertexShader.getVariableHandler("u_ViewportHeight")
            varDpi = vertexShader.getVariableHandler("u_Dpi")

            varTextureBounds = pixelShader.getVariableHandler("u_TextureBounds")
            varTextureColors = pixelShader.getVariableHandler("u_TextureColors")
        }
        if(device.vertexShader != vertexShader || device.pixelShader != pixelShader){
            device.vertexShader = vertexShader
            device.pixelShader = pixelShader
            forceViewportUpdate = true
        }
        updateShaderViewport(graphics)

        device.surface = graphics.surface
        device.beginScene()
    }

    fun onEndPaint(){
        device.endScene()
    }

    private fun updateShaderViewport(graphics: Graphics) {
        if(lastViewportWidth != graphics.width || forceViewportUpdate) {
            lastViewportWidth = graphics.width
            vertexShader[varViewportWidth] = graphics.physicalWidth.toFloat()
        }
        if(lastViewportHeight != graphics.height || forceViewportUpdate) {
            lastViewportHeight = graphics.height
            vertexShader[varViewportHeight] = graphics.physicalHeight.toFloat()
        }
        if(lastDpi != graphics.dpi || forceViewportUpdate) {
            lastDpi = graphics.dpi
            vertexShader[varDpi] = graphics.dpi
        }
        forceViewportUpdate = false
    }

    fun clear() = D3D9Pipeline.device.clear()

    fun fillShape(shape: Shape) {
        pixelShader[varRenderType] = 1f
        VertexHelper.paintVertices(shape.fillVertices, D3D9Pipeline.device::drawVertices)
    }

    fun drawShape(shape: Shape) {
        pixelShader[varRenderType] = 1f
        VertexHelper.paintVertices(shape.drawVertices, D3D9Pipeline.device::drawVertices)
    }

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        pixelShader[varRenderType] = 1f
        VertexHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        pixelShader[varRenderType] = 2f
        VertexHelper.drawRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        pixelShader[varRenderType] = 3f
        pixelShader[varTextureColors] = image.pixelType.channels.toFloat()
        pixelShader.set(varTextureBounds, x * lastDpi, y * lastDpi, width * lastDpi, height * lastDpi)
        device.bindTexture(0, (image as D3D9Image).renderTarget.texture)
        device.linearFiltering = image.linearFiltered
        VertexHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }

    fun drawText(image: Image, x: Float, y: Float, width: Float, height: Float) {
        pixelShader[varRenderType] = 4f
        VertexHelper.fillRect(x, y, width, height, D3D9Pipeline.device::drawVertices)
    }
}