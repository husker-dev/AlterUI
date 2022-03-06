package com.huskerdev.alter.graphics.font

import java.nio.charset.StandardCharsets

class FontFamily(
    internal val face: Long,
    internal val hbBuffer: Long
) {

    companion object {
        @JvmStatic private external fun nGetFacePropertiesCount(face: Long): Int
        @JvmStatic private external fun nGetFaceProperty(face: Long, property: Int): Array<Any>
    }

    val metadataCount = nGetFacePropertiesCount(face)

    val copyright: String by lazy { getMetadata(0) }
    val familyName: String by lazy { getMetadata(1) }
    val subfamily: String by lazy { getMetadata(2) }
    val id: String by lazy { getMetadata(3) }
    val fullName: String by lazy { getMetadata(4) }
    val version: String by lazy { getMetadata(5) }
    val name: String by lazy { getMetadata(6) }
    val trademark: String by lazy { getMetadata(7) }
    val manufacturer: String by lazy { getMetadata(8) }
    val designer: String by lazy { getMetadata(9) }
    val description: String by lazy { getMetadata(10) }
    val vendorURL: String by lazy { getMetadata(11) }
    val designerURL: String by lazy { getMetadata(12) }

    internal val cachedGlyphs = hashMapOf<Float, HashMap<Int, Glyph>>()
    internal val cachedRasterMetrics = hashMapOf<Float, HashMap<String, FontRasterMetrics>>()

    fun getMetadata(propertyId: Int): String{
        if(propertyId > metadataCount)
            return ""

        val result = nGetFaceProperty(face, propertyId)
        val bytes = result[0] as ByteArray
        val encoding = result[1] as IntArray

        return if(
            (encoding[0] == 3 && encoding[1] == 1) ||
            (encoding[0] == 0 && encoding[1] == 3))
            bytes.toString(StandardCharsets.UTF_16BE)
        else
            bytes.toString(StandardCharsets.UTF_8)
    }
}