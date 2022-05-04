package com.huskerdev.alter.graphics

import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.File
import java.net.URL
import java.nio.ByteBuffer

enum class ResizeAlgorithm(val index: Int){
    Default(0),
    Box(1),
    Triangle(2),
    CubicSpline(3),
    CatmullRom(4),
    Mitchell(5)
}

enum class ImageFileType(val index: Int){
    PNG(0),
    JPEG(1),
    BMP(2),
    TGA(3)
}

abstract class Image(
    val width: Int,
    val height: Int,
    val pixelType: PixelType,
    val dpi: Float
) {

    companion object {

        @JvmStatic private external fun nGetBitmap(data: ByteBuffer): ByteBuffer
        @JvmStatic private external fun nGetBitmapFromFile(path: ByteBuffer): ByteBuffer
        @JvmStatic private external fun nReleaseBitmap(data: ByteBuffer)
        @JvmStatic private external fun nResize(data: ByteBuffer, oldWidth: Int, oldHeight: Int, components: Int, newWidth: Int, newHeight: Int, type: Int): ByteBuffer
        @JvmStatic private external fun nWriteToFile(type: Int, path: ByteBuffer, data: ByteBuffer, width: Int, height: Int, components: Int, quality: Int)

        fun fromFile(data: ByteArray) = fromFile(BufferUtils.createByteBuffer(*data))
        fun fromFile(data: ByteBuffer): Image{
            val info = ImageInfo.get(data)
            val bitmap = nGetBitmap(data)
            val image = Pipeline.current.createImage(info.type, info.width, info.height, bitmap)
            nReleaseBitmap(bitmap)
            return image
        }

        fun fromURL(url: String) = fromFile(URL(url).openStream().readBytes())

        fun fromFile(filePath: String): Image{
            val info = ImageInfo.get(filePath)
            val bitmap = nGetBitmapFromFile(BufferUtils.createByteBuffer(*filePath.c_str))
            val image = Pipeline.current.createImage(info.type, info.width, info.height, bitmap)
            nReleaseBitmap(bitmap)
            return image
        }

        fun fromFile(file: File) = fromFile(file.absolutePath)

        fun fromFile(width: Int, height: Int, type: PixelType, bitmap: ByteBuffer): Image {
            if(width <= 0 || height <= 0)
                throw UnsupportedOperationException("Image size can not be <= 0")
            return Pipeline.current.createImage(type, width, height, bitmap)
        }

        fun createEmpty(width: Int, height: Int, type: PixelType = PixelType.RGBA): Image {
            if(width <= 0 || height <= 0)
                throw UnsupportedOperationException("Image size can not be <= 0")
            return Pipeline.current.createImage(type, width, height, null)
        }
    }

    open var linearFiltered = true
    open val graphics: Graphics by lazy { Pipeline.current.createGraphics(this) }
    abstract val data: ByteBuffer

    var isDisposed = false
    private set

    fun getSubImage(x: Int, y: Int, width: Int, height: Int): Image {
        if(x + width >= this.width || y + height >= this.height)
            throw UnsupportedOperationException("Out of bounds")
        return getSubImageImpl(x, y, width, height)
    }

    fun getSubImage(width: Int, height: Int) = getSubImage(0, 0, width, height)
    fun getResized(newWidth: Int, newHeight: Int, resizeType: ResizeAlgorithm = ResizeAlgorithm.Box) =
        fromFile(newWidth, newHeight, pixelType, nResize(data, width, height, pixelType.channels, newWidth, newHeight, resizeType.index))

    protected abstract fun getSubImageImpl(x: Int, y: Int, width: Int, height: Int): Image
    protected abstract fun disposeImpl()

    fun dispose(){
        if(!isDisposed){
            isDisposed = true
            disposeImpl()
        }
    }

    protected fun finalize() = dispose()

    /**
     *   quality -
     *      - For JPEG 1..100 (higher is better quality)
     *      - For PNG 1..999 (higher is better compression)
     */
    fun writeToFile(file: File, type: ImageFileType = ImageFileType.PNG, quality: Int = -1) =
        nWriteToFile(type.index, BufferUtils.createByteBuffer(*file.absolutePath.c_str), data, width, height, pixelType.channels, quality)

    /**
     *   quality -
     *      - For JPEG 0..100 (higher is better quality)
     *      - For PNG 1..999 (higher is better compression)
     */
    fun writeToFile(filePath: String, type: ImageFileType = ImageFileType.PNG, quality: Int = -1) =
        writeToFile(File(filePath), type, quality)
}

