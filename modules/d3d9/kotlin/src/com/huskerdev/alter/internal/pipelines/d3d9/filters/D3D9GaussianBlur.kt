package com.huskerdev.alter.internal.pipelines.d3d9.filters

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Image
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.device
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Shader

class D3D9GaussianBlur(radius: Int): GaussianBlur(radius) {

    companion object {
        private val pixelShader = D3D9Shader.pixelFromResources("/com/huskerdev/alter/resources/d3d9/shaders/gaussian/gaussianBlurFragment.hlsl").compile()
        private val vertexShader = D3D9Shader.vertexFromResources("/com/huskerdev/alter/resources/d3d9/shaders/defaultVertex.hlsl").compile()
    }

    override fun processImpl(input: Image): Image {
        input as D3D9Image
        val newImage = Image.createEmpty(input.width, input.height, input.pixelType) as D3D9Image

        val tmpTexture = device.createTexture(input.width, input.height, input.pixelType.channels)
        val tmpSurface = device.getTextureSurface(tmpTexture)

        synchronized(device) {
            device.pixelShader = pixelShader
            device.vertexShader = vertexShader

            val sizeVariable = pixelShader.getVariableHandler("u_size")
            val radiusVariable = pixelShader.getVariableHandler("u_radius")
            val typeVariable = pixelShader.getVariableHandler("u_type")
            pixelShader.set(sizeVariable, 0f, 0f, input.width.toFloat(), input.height.toFloat())
            pixelShader[radiusVariable] = radius.toFloat()

            device.beginScene()

            // Horizontal
            pixelShader[typeVariable] = 0f
            device.bindTexture(0, input.renderTarget.texture)
            device.surface = tmpSurface
            VertexHelper.fillRect(-1f, -1f, 2f, 2f, device::drawVertices)

            // Vertical
            pixelShader[typeVariable] = 1f
            device.bindTexture(0, tmpTexture)
            device.surface = newImage.renderTarget.surface
            VertexHelper.fillRect(-1f, -1f, 2f, 2f, device::drawVertices)

            device.endScene()
        }

        newImage.renderTarget.contentChanged = true
        device.releaseTexture(tmpTexture)
        device.releaseSurface(tmpSurface)
        return newImage
    }

}