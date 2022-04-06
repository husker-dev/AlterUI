package com.huskerdev.alter.geom

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


class Ellipse(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val roundPoints: Int = 60
): Shape() {

    companion object {
        fun fromOrigin(originX: Float, originY: Float, rx: Float, ry: Float, roundPoints: Int = 60) =
            Ellipse(originX - rx, originY - ry, rx * 2, + ry * 2, roundPoints)
    }

    constructor(width: Float, height: Float): this(0f, 0f, width, height)

    val rx = width / 2
    val ry = height / 2

    val originX = x + rx
    val originY = y + ry

    override val points by lazy {
        return@lazy FloatArray(roundPoints * 2){
            val angle = 360f / roundPoints * (it / 2) - 90
            return@FloatArray if(it % 2 == 0)
                (cos(Math.toRadians(angle.toDouble())) * rx).toFloat() + originX
            else
                (sin(Math.toRadians(angle.toDouble())) * ry).toFloat() + originY
        }
    }

    override fun contains(point: Point) =
        (point.x - originX).pow(2) / rx.pow(2) + (point.y - originY).pow(2) / ry.pow(2) <= 1

}