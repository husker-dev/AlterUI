package com.huskerdev.alter.graphics.font

import com.huskerdev.alter.internal.c_str
import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class FontRasterMetrics(
    text: String,
    font: Font
){
    companion object {
        @JvmStatic private external fun nHBSetBufferText(buffer: Long, text: ByteBuffer)
        @JvmStatic private external fun nHBCreateFont(face: Long, size: Int): Long
        @JvmStatic private external fun nHBShape(font: Long, buffer: Long)
        @JvmStatic private external fun nHBGetGlyphCount(buffer: Long): Int
        @JvmStatic private external fun nHBGetGlyphInfo(buffer: Long): Long
        @JvmStatic private external fun nHBGetGlyphPositions(buffer: Long): Long
        @JvmStatic private external fun nHBGetGlyphId(info: Long, index: Int): Int
        @JvmStatic private external fun nHBGetXOffset(positions: Long, index: Int): Int
        @JvmStatic private external fun nHBGetYOffset(positions: Long, index: Int): Int
        @JvmStatic private external fun nHBGetXAdvance(positions: Long, index: Int): Int
        @JvmStatic private external fun nHBGetYAdvance(positions: Long, index: Int): Int
    }

    val count: Int
    var baselineX = 0
    var baselineY = 0
    val width: Int
    val height: Int

    val glyphs: Array<Glyph>
    private val glyphX = hashMapOf<Int, Int>()
    private val glyphY = hashMapOf<Int, Int>()

    init {
        val family = font.family
        val hbFont: Long = nHBCreateFont(family.face, font.size.toInt())

        nHBSetBufferText(family.hbBuffer, BufferUtils.createByteBuffer(*text.c_str))
        nHBShape(hbFont, family.hbBuffer)

        count = nHBGetGlyphCount(family.hbBuffer)
        val info = nHBGetGlyphInfo(family.hbBuffer)
        val positions = nHBGetGlyphPositions(family.hbBuffer)

        // Calculate chars positions
        var minX = 0f
        var minY = 0f
        var maxX = 0f
        var maxY = 0f
        var currentX = 0f
        var currentY = 0f

        glyphs = Array(count){ i ->
            val glyph = font.getGlyph(nHBGetGlyphId(info, i))

            val glyphWidth = glyph.width
            val glyphHeight = glyph.height

            val offsetX = nHBGetXOffset(positions, i) / 64f
            val offsetY = nHBGetYOffset(positions, i) / 64f

            glyphX[i] = (currentX + offsetX + glyph.bearingX).toInt()
            glyphY[i] = (-(currentY + offsetY) + (glyph.bearingY - glyph.height)).toInt()

            minX = min(minX, currentX + offsetX)
            minY = min(minY, currentY + offsetY)
            maxX = max(maxX, currentX + offsetX + glyphWidth)
            maxY = max(maxY, currentY + offsetY + glyphHeight)

            if(i == 0)
                baselineX = -glyph.bearingX
            baselineY = max(baselineY, -glyph.bearingY)

            currentX += nHBGetXAdvance(positions, i) / 64f
            currentY += nHBGetYAdvance(positions, i) / 64f

            return@Array glyph
        }

        width = (maxX - minX).toInt()
        height = (maxY - minY).toInt()
    }

    fun getGlyphX(index: Int) = glyphX[index]!!
    fun getGlyphY(index: Int) = glyphY[index]!!
}