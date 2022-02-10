package com.huskerdev.alter.geom

data class Rectangle(
    var x: Double,
    var y: Double,
    var width: Double,
    var height: Double
): Shape {

    constructor(width: Double, height: Double): this(0.0, 0.0, width, height)
}