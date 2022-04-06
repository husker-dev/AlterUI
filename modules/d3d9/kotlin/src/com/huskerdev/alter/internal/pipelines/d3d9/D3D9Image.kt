package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import java.nio.ByteBuffer

class D3D9Image(
    val texture: Long,
    val surface: Long,
    val physicalWidth: Int,
    val physicalHeight: Int,
    val logicWidth: Int,
    val logicHeight: Int,
    type: PixelType,
    dpi: Float
): Image(physicalWidth, physicalHeight, type, dpi) {

    override val data: ByteBuffer
        get() = TODO("Not yet implemented")

    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int): Image {
        TODO("Not yet implemented")
    }

    override fun disposeImpl() {
        D3D9Pipeline.nReleaseTexture(texture)
        D3D9Pipeline.nReleaseSurface(surface)
    }

}