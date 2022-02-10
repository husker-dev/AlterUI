package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.FloatBuffer

interface VertexPainter: Painter {

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        val x2 = x + width
        val y2 = y + height
        drawVertices(
            BufferUtils.createFloatBuffer(
                x,  y,  0f,
                x,  y2, 0f,
                x2, y2, 0f,
                x2, y2, 0f,
                x2, y,  0f,
                x,  y,  0f
            ), 6
        )
    }

    fun drawVertices(vertices: FloatBuffer, points: Int)
}