package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import java.nio.ByteBuffer

class D3D9Image(
    samples: Int,
    val physicalWidth: Int,
    val physicalHeight: Int,
    val logicWidth: Int,
    val logicHeight: Int,
    type: PixelType,
    dpi: Float,
    data: ByteBuffer?
): Image(physicalWidth, physicalHeight, type, dpi) {

    val renderTarget = D3D9RenderTarget(physicalWidth, physicalHeight, type.channels, samples, data)

    override val data: ByteBuffer
        get() = TODO("Not yet implemented")

    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int): Image {
        TODO("Not yet implemented")
    }

    override fun disposeImpl() {
        renderTarget.dispose()
    }

}