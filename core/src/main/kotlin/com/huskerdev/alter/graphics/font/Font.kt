package com.huskerdev.alter.graphics.font

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
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

        @JvmStatic external fun nSetFaceSize(face: Long, size: Int)
        @JvmStatic private external fun nLoadChar(face: Long, charIndex: Int)
        @JvmStatic private external fun nGetGlyphData(face: Long): ByteBuffer
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

    fun derived(size: Float): Font {
        return Font(family, size)
    }

    fun getRasterMetrics(text: String): FontRasterMetrics {
        if(size !in family.cachedRasterMetrics)
            family.cachedRasterMetrics[size] = hashMapOf()

        if(text !in family.cachedRasterMetrics[size]!!)
            family.cachedRasterMetrics[size]!![text] = FontRasterMetrics(text, this)

        return family.cachedRasterMetrics[size]!![text]!!
    }

    fun getGlyph(code: Int): Glyph {
        if(size !in family.cachedGlyphs)
            family.cachedGlyphs[size] = hashMapOf()

        if(code !in family.cachedGlyphs[size]!!){
            nSetFaceSize(family.face, size.toInt())
            nLoadChar(family.face, code)
            val data = nGetGlyphData(family.face)
            val width = nGetGlyphWidth(family.face)
            val height = nGetGlyphHeight(family.face)
            val bearingX = nGetBearingX(family.face)
            val bearingY = nGetBearingY(family.face)
            val image = if(width > 0 && height > 0) {
                Image.create(width, height, ImageType.MONO, data).apply {
                    linearFiltered = false
                }
            } else null

            family.cachedGlyphs[size]!![code] = Glyph(image, width, height, bearingX, bearingY)
        }

        return family.cachedGlyphs[size]!![code]!!
    }
}

