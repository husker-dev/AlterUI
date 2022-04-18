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
    val accuracy: Float = 0.7f
): Shape() {

    constructor(x: Float, y: Float, width: Float, height: Float, radius: Float, accuracy: Float = 0.7f):
            this(x, y, width, height, radius, radius, radius, radius, radius, radius, radius, radius, accuracy)

    constructor(width: Float, height: Float, radius: Float, accuracy: Float = 0.7f):
            this(0f, 0f, width, height, radius, radius, radius, radius, radius, radius, radius, radius, accuracy)

    constructor(x: Float, y: Float, width: Float, height: Float, radius1: Float, radius2: Float, radius3: Float, radius4: Float, accuracy: Float = 0.7f):
            this(x, y, width, height, radius1, radius1, radius2, radius2, radius3, radius3, radius4, radius4, accuracy)

    constructor(width: Float, height: Float, radius1: Float, radius2: Float, radius3: Float, radius4: Float, accuracy: Float = 0.7f):
            this(0f, 0f, width, height, radius1, radius1, radius2, radius2, radius3, radius3, radius4, radius4, accuracy)

    override val points by lazy {
        val points = arrayListOf<Float>()
        var steps = 0f

        // Left-top corner
        points.add(x)
        points.add(y + radius1height)
        steps = PI.toFloat() * (radius1width + radius1height) / 4 * accuracy
        repeat(steps.toInt()){ i ->
            val angle = i / steps * 90 - 180

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius1width + (x + radius1width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius1height + (y + radius1height))
        }
        points.add(x + radius1width)
        points.add(y)

        // Right top corner
        points.add(x + width - radius2width)
        points.add(y)
        steps = PI.toFloat() * (radius2width + radius2height) / 4 * accuracy
        repeat(steps.toInt()){ i ->
            val angle = i / steps * 90 - 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius2width + (x + width - radius2width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius2height + (y + radius2height))
        }
        points.add(x + width)
        points.add(y + radius2height)

        // Right bottom corner
        points.add(x + width)
        points.add(y + height - radius3height)
        steps = PI.toFloat() * (radius3width + radius3height) / 4 * accuracy
        repeat(steps.toInt()){ i ->
            val angle = i / steps * 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius3width + (x + width - radius3width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius3height + (y + height - radius3height))
        }
        points.add(x + width - radius3width)
        points.add(y + height)

        // Left bottom corner
        points.add(x + radius4width)
        points.add(y + height)
        steps = PI.toFloat() * (radius4width + radius4height) / 4 * accuracy
        repeat(steps.toInt()){ i ->
            val angle = i / steps * 90 + 90

            points.add(cos(Math.toRadians(angle.toDouble())).toFloat() * radius4width + (x + radius4width))
            points.add(sin(Math.toRadians(angle.toDouble())).toFloat() * radius4height + (y + height - radius4height))
        }
        points.add(x)
        points.add(y + height - radius4height)

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