package com.huskerdev.alter.geom

@JvmInline
value class Matrix(
    val elements: FloatArray
) {

    companion object {

        fun create(vararg elements: Float) = Matrix(elements)

        fun ortho(width: Float, height: Float) = create(
            2/width,    0f,         0f,             -1f,
            0f,         2/height,   0f,             -1f,
            0f,         0f,         1f,             0f,
            0f,         0f,         0f,             1f
        )

        val identity = create(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }


    override fun toString(): String {
        return "Matrix(elements=${elements.contentToString()})"
    }

}