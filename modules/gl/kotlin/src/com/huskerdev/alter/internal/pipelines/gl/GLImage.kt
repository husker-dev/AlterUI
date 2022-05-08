package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.utils.kotlin.unique
import java.nio.ByteBuffer

class GLImage(
    type: PixelType,
    val physicalWidth: Int,
    val physicalHeight: Int,
    val logicWidth: Int,
    val logicHeight: Int,
    dpi: Float,
    data: ByteBuffer?,
    val context: GLContext
): Image(physicalWidth, physicalHeight, type, dpi) {

    var renderTarget = GLRenderTarget(width, height, type.channels, AlterUIProperties.msaa, data, context)

    override var linearFiltered by unique(true) {
        context.setLinearFiltering(renderTarget.texture, it)
    }

    override val data: ByteBuffer
        get() = context.readPixels(this, 0, 0, width, height)


    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int) =
        fromFile(width, height, pixelType, context.readPixels(this, x, y, width, height))

    override fun disposeImpl() =
        renderTarget.dispose()
}