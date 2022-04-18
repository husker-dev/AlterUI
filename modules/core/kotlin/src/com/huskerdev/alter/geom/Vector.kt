package com.huskerdev.alter.geom

import kotlin.math.*

data class Vector(val x: Float, val y: Float) {

    companion object {
        fun fromPoints(from: Point, to: Point) = Vector(to.x - from.x, to.y - from.y)
    }

    val length by lazy { sqrt(x.pow(2) + y.pow(2)) }
    val angle by lazy { atan2(x, y) }

    operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
    operator fun times(other: Vector) = x * other.x + y * other.y

    fun getAngleBetween(other: Vector) = atan2(x * other.y - y * other.x, x * other.x + y * other.y)

}