package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.geom.Vector
import com.huskerdev.alter.graphics.LineCap
import com.huskerdev.alter.graphics.LineJoin
import com.huskerdev.alter.internal.utils.BufferUtils
import com.huskerdev.alter.internal.utils.Earcut
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin


typealias VertexPaintFunction = (
    buffer: FloatBuffer,
    count: Int,
    type: VertexHelper.DrawType
) -> Unit

class VertexHelper {

    enum class DrawType {
        PointList,
        LineList,
        LineStrip,
        TriangleList,
        TriangleStrip,
        TriangleFan
    }

    companion object {

        private fun ArrayList<Float>.addRectangle(
            x1: Float, y1: Float,   // Left top
            x2: Float, y2: Float,   // Left bottom
            x3: Float, y3: Float,   // Right bottom
            x4: Float, y4: Float    // Tight top
        ) {
            floatArrayOf(
                x1, y1, 0f,
                x2, y2, 0f,
                x3, y3, 0f,
                x3, y3, 0f,
                x4, y4, 0f,
                x1, y1, 0f
            ).forEach { add(it) }
        }

        inline fun paintVertices(vertices: FloatArray, paintFunc: VertexPaintFunction) =
            paintFunc(BufferUtils.createFloatBuffer(*vertices), vertices.size / 3, DrawType.TriangleList)


        inline fun fillRect(x: Float, y: Float, width: Float, height: Float, paintFunc: VertexPaintFunction) {
            val x2 = x + width
            val y2 = y + height
            paintFunc(
                BufferUtils.createFloatBuffer(
                    x, y, 0f,
                    x, y2, 0f,
                    x2, y2, 0f,
                    x2, y2, 0f,
                    x2, y, 0f,
                    x, y, 0f
                ), 6,
                DrawType.TriangleList
            )
        }

        inline fun drawRect(x: Float, y: Float, width: Float, height: Float, paintFunc: VertexPaintFunction) {
            val x2 = x + width
            val y2 = y + height
            paintFunc(
                BufferUtils.createFloatBuffer(
                    x, y, 0f,
                    x, y2, 0f,
                    x2, y2, 0f,
                    x2, y2, 0f,
                    x2, y, 0f,
                    x, y, 0f
                ), 6,
                DrawType.LineList
            )
        }

        fun getFillVertices(shape: Shape): FloatArray {
            val points = shape.points
            val pointIndices = Earcut.earcut(points, null, 2)

            val vertices = FloatArray(pointIndices.size * 3)
            pointIndices.forEachIndexed{ i, value ->
                vertices[i * 3] = points[2 * value]
                vertices[i * 3 + 1] = points[2 * value + 1]
                vertices[i * 3 + 2] = 0f
            }
            return vertices
        }

        fun getLineVertices(shape: Shape): FloatArray{
            val stroke = shape.stroke

            val vertices = arrayListOf<Float>()
            val shapePoints = shape.points

            val curves = arrayListOf<Curve>()

            for(i in shapePoints.indices step 2){
                val point = Point(shapePoints[i], shapePoints[i + 1])

                val nextPoint = if(i != shapePoints.lastIndex - 1)
                    Point(shapePoints[i + 2], shapePoints[i + 3])
                else Point(shapePoints[0], shapePoints[1])
                val prevPoint = if(i != 0)
                    Point(shapePoints[i - 2], shapePoints[i - 1])
                else Point(shapePoints[shapePoints.lastIndex - 1], shapePoints[shapePoints.lastIndex])

                val isLast = i == shapePoints.lastIndex - 1
                val v1 = Vector.fromPoints(point, prevPoint)
                val v2 = Vector.fromPoints(point, nextPoint)

                if(!shape.connectEnds && (i == 0 || i == shapePoints.lastIndex - 1)) {
                    val vector = if(i == 0) v2 else v1

                    /*  ====================
                               Caps
                        ====================
                    */
                    curves += when(stroke.cap){
                        LineCap.None    -> NoneCap(point, vector, stroke.width, isLast)
                        LineCap.Round   -> RoundCap(point, vector, stroke.width, isLast)
                    }
                }else{
                    val angle = v1.getAngleBetween(v2)

                    // If angle is 180 degrees, then add ButtCap
                    if((PI - angle.absoluteValue.toDouble()).absoluteValue < 0.0001){
                        curves += NoneCap(point, v2, stroke.width, isLast)
                        continue
                    }

                    val d = stroke.width / 2 / sin(angle)

                    val u = Vector(v1.x / v1.length * d, v1.y / v1.length * d)
                    val v = Vector(v2.x / v2.length * d, v2.y / v2.length * d)

                    /*  ====================
                               Joins
                        ====================
                    */
                    curves += when(stroke.join){
                        LineJoin.Miter  -> MiterCurve(point, u, v)
                        LineJoin.None   -> NoneCurve(v1, v2, angle, stroke.width, point, u, v)
                        LineJoin.Bevel  -> BevelCurve(v1, v2, angle, stroke.width, point, u, v)
                        LineJoin.Round  -> RoundCurve(v1, v2, angle, stroke.width, point, u, v)
                    }
                }
            }

            curves.forEachIndexed { i, curve ->
                val isLast = i == curves.lastIndex
                if(shape.connectEnds || !isLast){
                    val nextCurve = if(isLast) curves[0] else curves[i + 1]

                    vertices.addRectangle(
                        curve.endPoint2.x, curve.endPoint2.y,
                        nextCurve.startPoint2.x, nextCurve.startPoint2.y,
                        nextCurve.startPoint1.x, nextCurve.startPoint1.y,
                        curve.endPoint1.x, curve.endPoint1.y
                    )
                }
                curve.additionalVertices.forEach { vertices += it }
            }

            return vertices.toFloatArray()
        }

        interface Curve {
            val endPoint1: Point
            val endPoint2: Point
            val startPoint1: Point
            val startPoint2: Point
            val additionalVertices: FloatArray
        }

        abstract class Cap: Curve {
            override val endPoint1 by ::startPoint1
            override val endPoint2 by ::startPoint2
        }

        open class NoneCap(
            point: Point,
            vector: Vector,
            width: Float,
            isLast: Boolean
        ): Cap() {
            private val a = width / 2 / vector.length
            private val tx = vector.y * a
            private val ty = vector.x * a

            override lateinit var startPoint1: Point
            override lateinit var startPoint2: Point

            init {
                if(isLast) {
                    startPoint1 = Point(point.x + tx, point.y - ty)
                    startPoint2 = Point(point.x - tx, point.y + ty)
                }else{
                    startPoint1 = Point(point.x - tx, point.y + ty)
                    startPoint2 = Point(point.x + tx, point.y - ty)
                }
            }

            override val additionalVertices = FloatArray(0)
        }

        class RoundCap(
            point: Point,
            vector: Vector,
            width: Float,
            isLast: Boolean
        ): NoneCap(point, vector, width, isLast) {

            companion object {
                private const val steps = 45
            }

            override val additionalVertices: FloatArray by lazy {
                val vertices = FloatArray(3 * 3 * (steps + 1))
                val startAngle = if(isLast)
                    -(Vector.fromPoints(startPoint2, startPoint1).angle - PI / 2)
                else
                    -(Vector.fromPoints(startPoint1, startPoint2).angle - PI / 2)

                var previousPoint = if(isLast) startPoint2 else startPoint1
                for(i in 0..steps){
                    val currentAngle = startAngle + PI * ((i + 1f) / (steps + 1))

                    val newPoint = Point(
                        point.x - width / 2 * cos(currentAngle).toFloat(),
                        point.y - width / 2 * sin(currentAngle).toFloat()
                    )
                    vertices[9 * i]     = newPoint.x
                    vertices[9 * i + 1] = newPoint.y
                    vertices[9 * i + 2] = 0f
                    vertices[9 * i + 3] = point.x
                    vertices[9 * i + 4] = point.y
                    vertices[9 * i + 5] = 0f
                    vertices[9 * i + 6] = previousPoint.x
                    vertices[9 * i + 7] = previousPoint.y
                    vertices[9 * i + 8] = 0f

                    previousPoint = newPoint
                }


                return@lazy vertices
            }
        }

        /**
         *  More at StackExchange:
         *
         *  [Calculate miter points of stroked vectors in Cartesian plane](https://math.stackexchange.com/a/1849845/1047895)
         */
        class MiterCurve(
            point: Point,
            u: Vector,
            v: Vector
        ): Curve{
            private val p1 = Point(point.x - u.x - v.x, point.y - u.y - v.y)
            private val p2 = Point(point.x + u.x + v.x, point.y + u.y + v.y)

            override val startPoint1 = p1
            override val startPoint2 = p2
            override val endPoint1 = p1
            override val endPoint2 = p2

            override val additionalVertices = FloatArray(0)
        }

        open class NoneCurve(
            v1: Vector,
            v2: Vector,
            angle: Float,
            width: Float,
            point: Point,
            u: Vector,
            v: Vector
        ): Curve {
            override lateinit var startPoint1: Point
            override lateinit var startPoint2: Point
            override lateinit var endPoint1: Point
            override lateinit var endPoint2: Point

            protected val isConvex = angle > 0 && angle < PI

            init {
                val a1 = width / 2 / v1.length
                val a2 = width / 2 / v2.length

                if(isConvex){
                    val crossing = Point(point.x + u.x + v.x, point.y + u.y + v.y)
                    startPoint1 = Point(point.x + (v1.y * a1), point.y - (v1.x * a1))
                    startPoint2 = crossing
                    endPoint1 = Point(point.x - (v2.y * a2), point.y + (v2.x * a2))
                    endPoint2 = crossing
                }else {
                    val crossing = Point(point.x - u.x - v.x, point.y - u.y - v.y)
                    startPoint1 = crossing
                    startPoint2 = Point(point.x - (v1.y * a1), point.y + (v1.x * a1))
                    endPoint1 = crossing
                    endPoint2 = Point(point.x + (v2.y * a2), point.y - (v2.x * a2))
                }
            }

            override val additionalVertices = floatArrayOf(
                startPoint1.x, startPoint1.y, 0f,
                startPoint2.x, startPoint2.y, 0f,
                point.x, point.y, 0f,
                endPoint1.x, endPoint1.y, 0f,
                endPoint2.x, endPoint2.y, 0f,
                point.x, point.y, 0f
            )
        }

        class BevelCurve(
            v1: Vector,
            v2: Vector,
            angle: Float,
            width: Float,
            point: Point,
            u: Vector,
            v: Vector
        ): NoneCurve(v1, v2, angle, width, point, u, v) {

            override val additionalVertices = if(isConvex)
                floatArrayOf(
                    startPoint1.x, startPoint1.y, 0f,
                    startPoint2.x, startPoint2.y, 0f,
                    endPoint1.x, endPoint1.y, 0f,
                )
            else
                floatArrayOf(
                    startPoint1.x, startPoint1.y, 0f,
                    startPoint2.x, startPoint2.y, 0f,
                    endPoint2.x, endPoint2.y, 0f,
                )
        }

        class RoundCurve(
            v1: Vector,
            v2: Vector,
            val angle: Float,
            val width: Float,
            val point: Point,
            u: Vector,
            v: Vector
        ): NoneCurve(v1, v2, angle, width, point, u, v) {

            override val additionalVertices: FloatArray by lazy {
                val roundAngle = PI - this.angle.toDouble().absoluteValue
                val startAngle = -(Vector.fromPoints(point, if(isConvex) endPoint1 else startPoint2).angle + PI / 2)
                val steps = (PI * width * (roundAngle / (2 * PI)) / 2).toInt()

                val vertices = FloatArray(3 * 3 * (2 + steps + 1))

                // right part under round
                vertices[0] = startPoint1.x
                vertices[1] = startPoint1.y
                vertices[2] = 0f
                vertices[3] = startPoint2.x
                vertices[4] = startPoint2.y
                vertices[5] = 0f
                vertices[6] = point.x
                vertices[7] = point.y
                vertices[8] = 0f

                // left part under round
                vertices[9] = endPoint1.x
                vertices[10] = endPoint1.y
                vertices[11] = 0f
                vertices[12] = endPoint2.x
                vertices[13] = endPoint2.y
                vertices[14] = 0f
                vertices[15] = point.x
                vertices[16] = point.y
                vertices[17] = 0f

                var previousPoint = if(isConvex) endPoint1 else startPoint2
                for(i in 0 .. steps){
                    val currentAngle = startAngle + roundAngle * ((i + 1f) / (steps + 1))

                    val newPoint = Point(
                        point.x - width / 2 * cos(currentAngle).toFloat(),
                        point.y - width / 2 * sin(currentAngle).toFloat(),
                    )

                    vertices[18 + i * 9] = newPoint.x
                    vertices[19 + i * 9] = newPoint.y
                    vertices[20 + i * 9] = 0f
                    vertices[21 + i * 9] = previousPoint.x
                    vertices[22 + i * 9] = previousPoint.y
                    vertices[23 + i * 9] = 0f
                    vertices[24 + i * 9] = point.x
                    vertices[25 + i * 9] = point.y
                    vertices[26 + i * 9] = 0f

                    previousPoint = newPoint
                }

                return@lazy vertices
            }
        }
    }
}

