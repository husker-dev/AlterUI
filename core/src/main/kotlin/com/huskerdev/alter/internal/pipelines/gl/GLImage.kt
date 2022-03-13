package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetLinearFiltering

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
            GLPipeline.invokeOnResourceThread {
                nSetLinearFiltering(texId, value)
            }
        }
}