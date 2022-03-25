package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nCreateTexture
import java.nio.ByteBuffer

class D3D9Image(val texture: Long, val surface: Long, width: Int, height: Int, type: PixelType): Image(width, height, type) {

    override val data: ByteBuffer
        get() = TODO("Not yet implemented")

    override fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int): Image {
        TODO("Not yet implemented")
    }
}