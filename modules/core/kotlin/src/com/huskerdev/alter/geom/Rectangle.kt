package com.huskerdev.alter.geom

@JvmInline
value class Rectangle(val values: FloatArray): Shape {
    constructor(width: Float, height: Float): this(floatArrayOf(0f, 0f, width, height))
    constructor(x: Float, y: Float, width: Float, height: Float): this(floatArrayOf(x, y, width, height))
}

val Rectangle.x: Float
    get() = values[0]

val Rectangle.y: Float
    get() = values[1]

val Rectangle.width: Float
    get() = values[2]

val Rectangle.height: Float
    get() = values[3]