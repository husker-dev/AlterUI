package com.huskerdev.alter.geom

import com.huskerdev.alter.graphics.Stroke
import com.huskerdev.alter.graphics.painters.VertexHelper



abstract class Shape {
    abstract val points: FloatArray

    var connectEnds = true
        protected set(value) {
            if(field != value) {
                resetDrawVertices = true
                field = value
            }
        }

    private val oldStroke = Stroke()
    var stroke = Stroke()
        set(value) {
            resetDrawVertices = true
            field = value
        }

    val fillVertices by lazy {
        VertexHelper.getFillVertices(this)
    }

    private var resetDrawVertices = true
    var drawVertices = FloatArray(0)
        get() {
            if(resetDrawVertices || (oldStroke != stroke)) {
                oldStroke.join  = stroke.join
                oldStroke.cap   = stroke.cap
                oldStroke.width = stroke.width

                resetDrawVertices = false
                field = VertexHelper.getLineVertices(this)
            }
            return field
        }

    abstract operator fun contains(point: Point): Boolean
}