package com.huskerdev.alter.graphics

@JvmInline
value class Color(private val value: Int) {

    val a get() = (value shr 24 and 0xFF).toFloat() / 255
    val r get() = (value shr 16 and 0xFF).toFloat() / 255
    val g get() = (value shr 8 and 0xFF).toFloat() / 255
    val b get() = (value and 0xFF).toFloat() / 255

    fun toARGB() = value
    fun toRGB() = value or 0xff000000.toInt()

    companion object {
        fun css(hex: String) = hex(hex.substring(1).toInt(radix = 16))

        fun hex(hex: Int, hasAlpha: Boolean = false): Color{
            return if(hasAlpha) Color(hex)
            else Color(hex or 0xff000000.toInt())
        }

        fun rgba(r: Float, g: Float, b: Float, a: Float) = Color(
            ((a * 255).toInt() shl 24) or
            ((r * 255).toInt() shl 16) or
            ((g * 255).toInt() shl 8) or
            (b * 255).toInt()
        )

        fun rgb(r: Float, g: Float, b: Float) = rgba(r, g, b, 1f)

        fun colorToInt(r: Float, g: Float, b: Float, a: Float = 0f): Int{
            return ((a * 255).toInt() shl 24) or
                    ((r * 255).toInt() shl 16) or
                    ((g * 255).toInt() shl 8) or
                    (b * 255).toInt()
        }

        val black = hex(0x000000)
        val white = hex(0xFFFFFF)
        val red = hex(0xFF0000)
        val green = hex(0x00FF00)
        val blue = hex(0x0000FF)
    }

    override fun toString(): String {
        return "Color(r=$r, g=$g, b=$b, a=$a)"
    }

}