package com.huskerdev.alter.graphics

import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.File
import java.net.URL
import java.nio.ByteBuffer

enum class ImageType(val channels: Int) {
    MONO(1),
    RGB(3),
    RGBA(4)
}

abstract class Image(val width: Int, val height: Int, val type: ImageType) {

    companion object {

        @JvmStatic private external fun nGetBitmap(data: ByteBuffer): ByteBuffer
        @JvmStatic private external fun nGetBitmapFromFile(path: ByteBuffer): ByteBuffer
        @JvmStatic private external fun nReleaseBitmap(data: ByteBuffer)

        fun create(data: ByteArray) = create(BufferUtils.createByteBuffer(*data))
        fun create(data: ByteBuffer): Image{
            val info = ImageInfo.get(data)
            val bitmap = nGetBitmap(data)
            val image = Pipeline.current.createImage(info.type, info.width, info.height, bitmap)
            nReleaseBitmap(bitmap)
            return image
        }

        fun createFromURL(url: String) = create(URL(url).openStream().readBytes())

        fun create(filePath: String): Image{
            val info = ImageInfo.get(filePath)
            val bitmap = nGetBitmapFromFile(BufferUtils.createByteBuffer(*filePath.c_str))
            val image = Pipeline.current.createImage(info.type, info.width, info.height, bitmap)
            nReleaseBitmap(bitmap)
            return image
        }

        fun create(width: Int, height: Int, type: ImageType, bitmap: ByteBuffer) = Pipeline.current.createImage(type, width, height, bitmap)

        fun createEmpty(width: Int, height: Int, type: ImageType = ImageType.RGBA) = Pipeline.current.createImage(type, width, height, null)
    }

    open var linearFiltered = true
}

