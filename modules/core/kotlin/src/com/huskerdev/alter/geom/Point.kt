package com.huskerdev.alter.geom

data class Point(
    val x: Float,
    val y: Float
){
    companion object {
        val zero = Point(0f, 0f)
    }

    fun toVector() = Vector(x, y)
}