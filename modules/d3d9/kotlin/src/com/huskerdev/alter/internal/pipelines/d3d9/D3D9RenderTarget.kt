package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateEmptySurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nGetTextureSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nReleaseSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nReleaseTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nStretchRect
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

    override fun disposeTexture(texture: Long) =
        nReleaseTexture(texture)
    override fun disposeRenderSurface(surface: Long) =
        nReleaseSurface(surface)

    override fun createTexture(width: Int, height: Int, components: Int) =
        nCreateTexture(width, height, components)
    override fun getTextureRenderSurface(texture: Long) =
        nGetTextureSurface(texture)

    override fun createMSAARenderSurface(
        width: Int,
        height: Int,
        components: Int,
        samples: Int,
        data: ByteBuffer?
    ) = if(data == null)
        nCreateEmptySurface(width, height, components, samples)
    else
        nCreateSurface(width, height, components, samples, data)

    override fun resolveMSAA(sourceSurface: Long, targetSurface: Long) =
        nStretchRect(sourceSurface, targetSurface)
}