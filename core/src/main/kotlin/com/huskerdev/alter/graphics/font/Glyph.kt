package com.huskerdev.alter.graphics.font

import com.huskerdev.alter.graphics.Image

class Glyph(
    val image: Image?,
    val width: Int,
    val height: Int,
    val bearingX: Int,
    val bearingY: Int,
    val isSubpixel: Boolean
)