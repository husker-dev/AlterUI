package com.huskerdev.alter.geom

@JvmInline
value class Vector4(
    val elements: FloatArray
) {
    constructor(x: Float, y: Float, z: Float, w: Float): this(floatArrayOf(x, y, z, w))
    constructor(x: Float, y: Float, z: Float): this(floatArrayOf(x, y, z, 0f))

    operator fun get(index: Int) = elements[index]

    operator fun set(index: Int, value: Float) {
        elements[index] = value
    }

    override fun toString(): String {
        return "Vector4{\n\t${elements[0]}, ${elements[1]}, ${elements[2]}, ${elements[3]}\n}"
    }


}