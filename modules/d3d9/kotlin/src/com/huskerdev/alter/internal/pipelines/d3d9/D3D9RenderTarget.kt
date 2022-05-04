package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.device
import com.huskerdev.alter.internal.pipelines.model.DefaultRenderTarget
import java.nio.ByteBuffer

class D3D9RenderTarget(
    width: Int,
    height: Int,
    components: Int,
    samples: Int,
    data: ByteBuffer?
): DefaultRenderTarget<Long>(width, height, components, samples, data) {

    val surface by ::renderSurface
    val texture by ::renderTexture

    init {
        loadTarget()
    }

    override fun disposeTexture(texture: Long) =
        device.releaseTexture(texture)
    override fun disposeRenderSurface(surface: Long) =
        device.releaseSurface(surface)

    override fun createTexture(width: Int, height: Int, components: Int) =
        device.createTexture(width, height, components)
    override fun getTextureRenderSurface(texture: Long) =
        device.getTextureSurface(texture)

    override fun createMSAARenderSurface(
        width: Int,
        height: Int,
        components: Int,
        samples: Int,
        data: ByteBuffer?
    ) = if(data == null)
        device.createEmptySurface(width, height, components, samples)
    else
        device.createSurface(width, height, components, samples, data)

    override fun resolveMSAA(sourceSurface: Long, targetSurface: Long) =
        device.stretchRect(sourceSurface, targetSurface)
}