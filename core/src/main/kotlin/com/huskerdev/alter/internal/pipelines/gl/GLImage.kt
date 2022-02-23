package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.createEmptyTexture
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nCreateTexture
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSetLinearFiltering
import java.nio.ByteBuffer

class GLImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?): Image(width, height, type) {

    var texId = 0

    init {
        (Pipeline.current as GLPipeline).invokeOnResourceThread {
            texId = if(data != null)
                nCreateTexture(width, height, type.channels, data)
            else createEmptyTexture(width, height, type.channels)
        }
    }

    private var _linearFiltering = false
    override var linearFiltered: Boolean
        get() = _linearFiltering
        set(value) {
            _linearFiltering = value
            (Pipeline.current as GLPipeline).invokeOnResourceThread {
                nSetLinearFiltering(texId, value)
            }
        }
}