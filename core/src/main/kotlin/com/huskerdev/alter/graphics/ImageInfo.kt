package com.huskerdev.alter.graphics

import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.File
import java.nio.ByteBuffer

data class ImageInfo(val width: Int, val height: Int, val type: PixelType) {

    companion object {
        @JvmStatic private external fun nGetBitmapInfo(data: ByteBuffer): IntArray
        @JvmStatic private external fun nGetBitmapInfoFromFile(path: ByteBuffer): IntArray

        fun get(file: File) = processInfo(nGetBitmapInfoFromFile(BufferUtils.createByteBuffer(*file.absolutePath.c_str)))
        fun get(filePath: String) = get(File(filePath))
        fun get(data: ByteBuffer) = processInfo(nGetBitmapInfo(data))
        fun get(data: ByteArray) = get(BufferUtils.createByteBuffer(*data))

        private fun processInfo(info: IntArray): ImageInfo{
            return ImageInfo(info[0], info[1], when(info[2]){
                1 -> PixelType.MONO
                3 -> PixelType.RGB
                4 -> PixelType.RGBA
                else -> throw UnsupportedOperationException("Unsupported image format")
            })
        }
    }
}