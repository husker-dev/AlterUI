package com.huskerdev.alter.geom

open class Matrix(
    var width: Int,
    var height: Int,
    vararg val elements: Float
) {

    companion object {

        fun ortho(width: Float, height: Float, far: Float, near: Float) = Matrix4(
            2/width,    0f,         0f,             -1f,
            0f,         2/height,   0f,             -1f,
            0f,         0f,         1f,             0f,
            0f,         0f,         0f,             1f
        )

        fun getIdentity(size: Int): Matrix {
            if(size == 3) return Matrix3()
            if(size == 4) return Matrix4()

            val array = FloatArray(size * size) {
                if((it - it / size) % size == 0) 1f else 0f
            }
            return Matrix(size, size, *array)
        }
    }

    class Matrix3(vararg elements: Float): Matrix(3, 3, *elements) {

    }

    class Matrix4(vararg elements: Float): Matrix(4, 4, *elements) {

    }

    override fun toString(): String {
        return "Matrix(elements=${elements.contentToString()})"
    }


}