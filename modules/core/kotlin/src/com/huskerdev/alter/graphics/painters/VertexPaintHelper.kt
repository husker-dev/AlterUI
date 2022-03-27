package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.FloatBuffer


typealias VertexPaintFunction = (
    buffer: FloatBuffer,
    count: Int,
    type: VertexPaintHelper.DrawType
) -> Unit

class VertexPaintHelper {

    enum class DrawType {
        PointList,
        LineList,
        LineStrip,
        TriangleList,
        TriangleStrip,
        TriangleFan
    }

    companion object {

        inline fun fillRect(x: Float, y: Float, width: Float, height: Float, paintFunc: VertexPaintFunction) {
            val x2 = x + width
            val y2 = y + height
            paintFunc(
                BufferUtils.createFloatBuffer(
                    x, y, 0f,
                    x, y2, 0f,
                    x2, y2, 0f,
                    x2, y2, 0f,
                    x2, y, 0f,
                    x, y, 0f
                ), 6,
                DrawType.TriangleList
            )
        }

        inline fun drawRect(x: Float, y: Float, width: Float, height: Float, paintFunc: VertexPaintFunction) {
            val x2 = x + width
            val y2 = y + height
            paintFunc(
                BufferUtils.createFloatBuffer(
                    x, y, 0f,
                    x, y2, 0f,
                    x2, y2, 0f,
                    x2, y2, 0f,
                    x2, y, 0f,
                    x, y, 0f
                ), 6,
                DrawType.LineList
            )
        }
    }
}

