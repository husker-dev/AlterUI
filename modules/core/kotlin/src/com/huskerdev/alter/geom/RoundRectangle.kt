package com.huskerdev.alter.geom

import kotlin.math.*


class RoundRectangle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val radius1width: Float,    // Left top
    val radius1height: Float,
    val radius2width: Float,    // Right top
    val radius2height: Float,
    val radius3width: Float,    // Right bottom
    val radius3height: Float,
    val radius4width: Float,    // Left bottom
    val radius4height: Float,
    val roundPoints: Int = 20
): Shape() {

    constructor(x: Float, y: Float, width: Float, height: Float, radius: Float, roundPoints: Int = 20):
            this(x, y, width, height, radius, radius, radius, radius, radius, radius, radius, radius, roundPoints)

    constructor(width: Float, height: Float, radius: Float, roundPoints: Int = 20):
            this(0f, 0f, width, height, radius, radius, radius, radius, radius, radius, radius, radius, roundPoints)

    constructor(x: Float, y: Float, width: Float, height: Float, radius1: Float, radius2: Float, radius3: Float, radius4: Float, roundPoints: Int = 20):
            this(x, y, width, height, radius1, radius1, radius2, radius2, radius3, radius3, radius4, radius4, roundPoints)

    constructor(width: Float, height: Float, radius1: Float, radius2: Float, radius3: Float, radius4: Float, roundPoints: Int = 20):
            this(0f, 0f, width, height, radius1, radius1, radius2, radius2, radius3, radius3, radius4, radius4, roundPoints)

    override val points by lazy {
        val points = arrayListOf<Float>()

        // TODO: Optimize count of points in Ellipse

        // Left-top corner
        for(i in 0..roundPoints){
            val angle = i / roundPoints.toFloat() * 90 - 180

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius1width + (x + radius1width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius1height + (y + radius1height))
        }

        // Right top corner
        for(i in 0..roundPoints){
            val angle = i / roundPoints.toFloat() * 90 - 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius2width + (x + width - radius2width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius2height + (y + radius2height))
        }

        // Right bottom corner
        for(i in 0..roundPoints){
            val angle = i / roundPoints.toFloat() * 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius3width + (x + width - radius3width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius3height + (y + height - radius3height))
        }

        // Left bottom corner
        for(i in 0..roundPoints){
            val angle = i / roundPoints.toFloat() * 90 + 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius4width + (x + radius4width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius4height + (y + height - radius4height))
        }

        return@lazy points.toFloatArray()
    }

    override fun contains(point: Point): Boolean {
        if(point.x >= x && point.y >= y && point.x <= x + width && point.y <= y + height){
            val localX = point.x - x
            val localY = point.y - y

            if(localX < radius1width && localY < radius1height)
                return (localX - radius1width).pow(2) / radius1width.pow(2) + (localY - radius1height).pow(2) / radius1height.pow(2) <= 1

            if(localX > width - radius2width && localY < radius2height)
                return (localX - (width - radius2width)).pow(2) / radius2width.pow(2) + (localY - radius2height).pow(2) / radius2height.pow(2) <= 1

            if(localX > width - radius3width && localY > height - radius3height)
                return (localX - (width - radius3width)).pow(2) / radius3width.pow(2) + (localY - (height - radius3height)).pow(2) / radius3height.pow(2) <= 1

            if(localX < radius4width && localY > height - radius4height)
                return (localX - radius4width).pow(2) / radius4width.pow(2) + (localY - (height - radius4height)).pow(2) / radius4height.pow(2) <= 1

            return true
        }
        return false
    }
}