package com.huskerdev.alter.geom

data class Rectangle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
): Shape() {

    override val points = floatArrayOf(
        x, y,
        x + width, y,
        x + width, y + height,
        x, y + height)

    constructor(width: Float, height: Float): this(0f, 0f, width, height)

    override fun contains(point: Point) =
        (point.x >= x) && (point.y >= y) &&
        (point.x <= x + width) && (point.y <= y + height)
}