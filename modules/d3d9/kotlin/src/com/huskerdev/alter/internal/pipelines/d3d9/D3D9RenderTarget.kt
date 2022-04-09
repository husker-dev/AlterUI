package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateEmptySurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nReleaseSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nReleaseTexture
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nStretchRect
import java.nio.ByteBuffer

class D3D9RenderTarget(
    width: Int,
    height: Int,
    components: Int,
    samples: Int,
    data: ByteBuffer?
) {
    var contentChanged = true

    private val pureTexture = nCreateTexture(width, height, components)
    val texture: Long
        get() {
            if(contentChanged) {
                nStretchRect(surface, pureTexture)
                contentChanged = false
            }
            return pureTexture
        }

    val surface = if(data == null)
        nCreateEmptySurface(width, height, components, samples)
    else
        nCreateSurface(width, height, components, samples, data)

    fun dispose(){
        nReleaseSurface(surface)
        nReleaseTexture(texture)
    }
}