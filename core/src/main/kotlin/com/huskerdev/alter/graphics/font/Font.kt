package com.huskerdev.alter.graphics.font

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.utils.BufferUtils
import java.io.File
import java.nio.ByteBuffer

class Font private constructor(
    val family: FontFamily,
    val size: Float
) {

    companion object {
        @JvmStatic private external fun nLoadFreeType(): Long
        @JvmStatic private external fun nCreateFace(lib: Long, data: ByteBuffer): Long

        @JvmStatic private external fun nSetFaceSize(face: Long, size: Int)
        @JvmStatic private external fun nLoadChar(face: Long, charIndex: Int)
        @JvmStatic private external fun nGetGlyphData(face: Long, useSubpixel: Boolean): ByteBuffer
        @JvmStatic private external fun nGetGlyphWidth(face: Long): Int
        @JvmStatic private external fun nGetGlyphHeight(face: Long): Int
        @JvmStatic private external fun nGetBearingX(face: Long): Int
        @JvmStatic private external fun nGetBearingY(face: Long): Int

        @JvmStatic private external fun nHBCreateBuffer(): Long

        private val ftLibrary = nLoadFreeType()
        private val registeredFonts = hashMapOf<String, Font>()

        fun register(data: ByteBuffer){
            val face = nCreateFace(ftLibrary, data)
            val family = FontFamily(face, nHBCreateBuffer())
            val font = Font(family, 14f)
            registeredFonts[font.family.familyName.lowercase()] = font
        }

        fun register(file: File) = register(BufferUtils.createByteBuffer(*file.readBytes()))

        fun get(family: String): Font {
            val actFamily = family.lowercase()
            if(actFamily !in registeredFonts){
                register(Platform.current.getFontData(actFamily)
                    ?: throw NullPointerException("Font '${family}' have not found in system"))
            }
            return registeredFonts[actFamily]!!
        }
    }

    // TODO: This functions invokes on every paint event, so may need to be cached
    fun derived(size: Float): Font {
        return Font(family, size)
    }

    fun getRasterMetrics(text: String, useSubpixel: Boolean): FontRasterMetrics {
        if(size !in family.cachedRasterMetrics)
            family.cachedRasterMetrics[size] = hashMapOf()

        if(text !in family.cachedRasterMetrics[size]!!)
            family.cachedRasterMetrics[size]!![text] = FontRasterMetrics(text, this, useSubpixel)

        return family.cachedRasterMetrics[size]!![text]!!
    }

    fun getGlyph(code: Int, useSubpixel: Boolean): Glyph {
        val cacheContainer = if(useSubpixel) family.cachedSubpixelGlyphs else family.cachedGlyphs
        if(size !in cacheContainer)
            cacheContainer[size] = hashMapOf()
        val cache = cacheContainer[size]!!

        if(code !in cache){
            nSetFaceSize(family.face, size.toInt())
            nLoadChar(family.face, code)
            
            val data = nGetGlyphData(family.face, useSubpixel)
            val width = nGetGlyphWidth(family.face)

            val height = nGetGlyphHeight(family.face)
            val bearingX = nGetBearingX(family.face)
            val bearingY = nGetBearingY(family.face)
            val image = if(width > 0 && height > 0) {
                Image.fromFile(width, height, if(useSubpixel) PixelType.RGB else PixelType.MONO, data).apply {
                    linearFiltered = false
                }
            } else null

            cache[code] = Glyph(image, width, height, bearingX, bearingY, useSubpixel)
        }

        return cache[code]!!
    }
}

