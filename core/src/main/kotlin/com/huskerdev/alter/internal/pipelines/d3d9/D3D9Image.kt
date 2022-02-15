package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateTexture
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer

class D3D9Image(width: Int, height: Int, type: ImageType, data: ByteBuffer?): Image(width, height, type) {

    var ptr = 0L

    init {

            ptr = nCreateTexture(width, height, type.channels, data!!)

    }
}