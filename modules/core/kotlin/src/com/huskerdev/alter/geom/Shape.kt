package com.huskerdev.alter.geom

import com.huskerdev.alter.internal.utils.Earcut


abstract class Shape {
    abstract val points: FloatArray

    val vertices by lazy {
        val points = this.points
        val pointIndices = Earcut.earcut(points, null, 2)

        val vertices = arrayOfNulls<Float>(pointIndices.size * 3)
        pointIndices.forEachIndexed{ i, value ->
            vertices[i * 3] = points[2 * value]
            vertices[i * 3 + 1] = points[2 * value + 1]
            vertices[i * 3 + 2] = 0f
        }

        return@lazy vertices.requireNoNulls().toFloatArray()
    }

    abstract operator fun contains(point: Point): Boolean
}