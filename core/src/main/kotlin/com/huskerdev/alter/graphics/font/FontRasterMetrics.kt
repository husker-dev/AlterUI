package com.huskerdev.alter.graphics.font

import com.huskerdev.alter.graphics.font.Font.Companion.nSetFaceSize
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
        @JvmStatic private external fun nHBCreateFont(face: Long): Long
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
    private val glyphX = hashMapOf<Int, Float>()
    private val glyphY = hashMapOf<Int, Float>()

    init {
        val family = font.family
        nSetFaceSize(family.face, font.size.toInt())
        val hbFont: Long = nHBCreateFont(family.face)

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
            val glyphInfo = font.getGlyph(nHBGetGlyphId(info, i))

            val glyphWidth = glyphInfo.width
            val glyphHeight = glyphInfo.height

            val offsetX = nHBGetXOffset(positions, i) / 64
            val offsetY = nHBGetYOffset(positions, i) / 64

            glyphX[i] = currentX + offsetX + glyphInfo.bearingX
            glyphY[i] = -(currentY + offsetY) + (glyphInfo.bearingY - glyphInfo.height)

            minX = min(minX, currentX + offsetX)
            minY = min(minY, currentY + offsetY)
            maxX = max(maxX, currentX + offsetX + glyphWidth)
            maxY = max(maxY, currentY + offsetY + glyphHeight)

            if(i == 0)
                baselineX = -glyphInfo.bearingX
            baselineY = max(baselineY, -glyphInfo.bearingY)

            currentX += nHBGetXAdvance(positions, i) / 64
            currentY += nHBGetYAdvance(positions, i) / 64

            return@Array glyphInfo
        }

        width = (maxX - minX).toInt()
        height = (maxY - minY).toInt()
    }

    fun getGlyphX(index: Int) = glyphX[index]!!
    fun getGlyphY(index: Int) = glyphY[index]!!
}