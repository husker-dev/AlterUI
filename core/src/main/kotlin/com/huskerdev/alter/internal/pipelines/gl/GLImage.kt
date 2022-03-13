package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.resourcesContext
import java.nio.ByteBuffer

class GLImage(
    val texId: Int,
    val framebuffer: Int,
    type: ImageType,
    width: Int,
    height: Int
): Image(width, height, type) {

    private var _linearFiltering = false
    override var linearFiltered: Boolean
        get() = _linearFiltering
        set(value) {
            _linearFiltering = value
            resourcesContext.setLinearFiltering(texId, value)
        }

    override val data: ByteBuffer
        get() = resourcesContext.readPixels(this, 0, 0, width, height)

    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int) =
        create(width, height, type, resourcesContext.readPixels(this, x, y, width, height))

}