package com.huskerdev.alter.geom

class LineStrip(vararg points: Point): Shape() {

    init {
        connectEnds = false
    }

    override val points = FloatArray(points.size * 2){
        return@FloatArray if(it % 2 == 0) points[it / 2].x else points[it / 2].y
    }

    override fun contains(point: Point) = false
}