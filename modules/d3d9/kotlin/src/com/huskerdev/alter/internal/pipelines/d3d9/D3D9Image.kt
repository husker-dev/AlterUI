package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.device
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
    get() {
        renderTarget.resolve()
        return device.getSurfaceData(renderTarget.resolvedTextureSurface, 0, 0, physicalWidth, physicalHeight, pixelType.channels)
    }

    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int): Image {
        renderTarget.resolve()
        return fromBitmap(width, height, pixelType, device.getSurfaceData(renderTarget.resolvedTextureSurface, x, y, width, height, pixelType.channels))
    }

    override fun disposeImpl() = renderTarget.dispose()

}