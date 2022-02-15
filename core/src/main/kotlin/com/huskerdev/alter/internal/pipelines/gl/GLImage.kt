package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.createEmptyTexture
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.createTexture
import java.nio.ByteBuffer

class GLImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?): Image(width, height, type) {

    var texId = 0

    init {
        (Pipeline.current as GLPipeline).invokeOnResourceThread {
            texId = if(data != null)
                createTexture(width, height, type.channels, data)
            else createEmptyTexture(width, height, type.channels)
        }
    }
}