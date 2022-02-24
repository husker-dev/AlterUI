package com.huskerdev.alter.geom

@JvmInline
value class Matrix4(
    val elements: FloatArray
) {

    companion object {
        fun create(vararg elements: Float) = Matrix4(elements)

        fun ortho(width: Float, height: Float) = create(
            2/width,    0f,         0f,             -1f,
            0f,         2/height,   0f,             -1f,
            0f,         0f,         1f,             0f,
            0f,         0f,         0f,             1f
        )

        fun scale(x: Float, y: Float, z: Float) = create(
            x,          0f,         0f,             0f,
            0f,         y,          0f,             0f,
            0f,         0f,         z,              0f,
            0f,         0f,         0f,             1f
        )

        val identity = create(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }

    operator fun get(i: Int, r: Int) = elements[r * 4 + i]
    operator fun set(i: Int, r: Int, value: Float) {
        elements[r * 4 + i] = value
    }

    operator fun times(matrix: Matrix4): Matrix4{
        val result = identity
        for(i in 0..3){
            for(r in 0..3){
                var sum = 0f
                for(m in 0..3)
                    sum += this[i, m] * matrix[m, r]
                result[i, r] = sum
            }
        }
        return result
    }

    operator fun times(vector: Vector4): Vector4{
        val result = Vector4(0f, 0f, 0f, 0f)
        for(i in 0..3){
            var sum = 0f
            for(m in 0..3)
                sum += this[i, m] * vector[m]
            result[i] = sum
        }
        return result
    }

    override fun toString(): String {
        return "Matrix4{\n" +
                "\t${elements[0]}, \t${elements[1]}, \t${elements[2]}, \t${elements[3]},\n" +
                "\t${elements[4]}, \t${elements[5]}, \t${elements[6]}, \t${elements[7]},\n" +
                "\t${elements[8]}, \t${elements[9]}, \t${elements[10]}, \t${elements[11]},\n" +
                "\t${elements[12]}, \t${elements[13]}, \t${elements[14]}, \t${elements[15]},\n}"
    }


}