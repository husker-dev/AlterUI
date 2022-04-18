package com.huskerdev.alter.graphics

enum class LineJoin {
    None,
    Round,
    Miter,
    Bevel
}

enum class LineCap {
    None,
    Round
}

data class Stroke(
    var width: Float = 1f,
    var join: LineJoin = LineJoin.Miter,
    var cap: LineCap = LineCap.None
)