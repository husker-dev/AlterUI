package com.huskerdev.alter.internal.utils

import kotlin.jvm.JvmOverloads
import java.lang.StringBuilder
import java.util.ArrayList
import kotlin.math.max

// From https://github.com/earcut4j/earcut4j with replaced Double to Float
object Earcut {
    /**
     * Triangulates the given polygon
     *
     * @param data is a flat array of vertice coordinates like [x0,y0, x1,y1, x2,y2, ...].
     * @param holeIndices is an array of hole indices if any (e.g. [5, 8] for a 12-vertice input would mean one hole with vertices 5-7 and another with 8-11).
     * @param dim  is the number of coordinates per vertice in the input array
     * @return List containing groups of three vertice indices in the resulting array forms a triangle.
     */
    /**
     * Triangulates the given polygon
     *
     * @param data is a flat array of vertice coordinates like [x0,y0, x1,y1, x2,y2, ...].
     * @return List containing groups of three vertice indices in the resulting array forms a triangle.
     */
    @JvmOverloads
    fun earcut(data: FloatArray, holeIndices: IntArray? = null, dim: Int = 2): List<Int> {
        val hasHoles = holeIndices != null && holeIndices.size > 0
        val outerLen = if (hasHoles) holeIndices!![0] * dim else data.size
        var outerNode = linkedList(data, 0, outerLen, dim, true)
        val triangles: MutableList<Int> = ArrayList()
        if (outerNode == null || outerNode.next === outerNode.prev) return triangles
        var minX = 0f
        var minY = 0f
        var maxX = 0f
        var maxY = 0f
        var invSize = Float.MIN_VALUE
        if (hasHoles) outerNode = eliminateHoles(data, holeIndices, outerNode, dim)

        // if the shape is not too simple, we'll use z-order curve hash later;
        // calculate polygon bbox
        if (data.size > 80 * dim) {
            maxX = data[0]
            minX = maxX
            maxY = data[1]
            minY = maxY
            var i = dim
            while (i < outerLen) {
                val x = data[i]
                val y = data[i + 1]
                if (x < minX) minX = x
                if (y < minY) minY = y
                if (x > maxX) maxX = x
                if (y > maxY) maxY = y
                i += dim
            }

            // minX, minY and size are later used to transform coords into
            // integers for z-order calculation
            invSize = max(maxX - minX, maxY - minY)
            invSize = if (invSize != 0f) 1f / invSize else 0f
        }
        earcutLinked(outerNode, triangles, dim, minX, minY, invSize, Int.MIN_VALUE)
        return triangles
    }

    private fun earcutLinked(
        ear: Node?,
        triangles: MutableList<Int>,
        dim: Int,
        minX: Float,
        minY: Float,
        invSize: Float,
        pass: Int
    ) {
        var ear: Node? = ear ?: return

        // interlink polygon nodes in z-order
        if (pass == Int.MIN_VALUE && invSize != Float.MIN_VALUE) indexCurve(ear!!, minX, minY, invSize)
        var stop = ear

        // iterate through ears, slicing them one by one
        while (ear!!.prev !== ear!!.next) {
            val prev = ear!!.prev
            val next = ear.next
            if (if (invSize != Float.MIN_VALUE) isEarHashed(ear, minX, minY, invSize) else isEar(ear)) {
                // cut off the triangle
                triangles.add(prev!!.i / dim)
                triangles.add(ear.i / dim)
                triangles.add(next!!.i / dim)
                removeNode(ear)

                // skipping the next vertice leads to less sliver triangles
                ear = next.next
                stop = next.next
                continue
            }
            ear = next

            // if we looped through the whole remaining polygon and can't find
            // any more ears
            if (ear === stop) {
                // try filtering points and slicing again
                if (pass == Int.MIN_VALUE) {
                    earcutLinked(filterPoints(ear, null), triangles, dim, minX, minY, invSize, 1)

                    // if this didn't work, try curing all small
                    // self-intersections locally
                } else if (pass == 1) {
                    ear = cureLocalIntersections(filterPoints(ear, null), triangles, dim)
                    earcutLinked(ear, triangles, dim, minX, minY, invSize, 2)

                    // as a last resort, try splitting the remaining polygon
                    // into two
                } else if (pass == 2) {
                    splitEarcut(ear, triangles, dim, minX, minY, invSize)
                }
                break
            }
        }
    }

    private fun splitEarcut(
        start: Node?,
        triangles: MutableList<Int>,
        dim: Int,
        minX: Float,
        minY: Float,
        size: Float
    ) {
        // look for a valid diagonal that divides the polygon into two
        var a = start
        do {
            var b = a!!.next!!.next
            while (b !== a!!.prev) {
                if (a!!.i != b!!.i && isValidDiagonal(a, b)) {
                    // split the polygon in two by the diagonal
                    var c: Node? = splitPolygon(a, b)

                    // filter colinear points around the cuts
                    a = filterPoints(a, a.next)
                    c = filterPoints(c, c!!.next)

                    // run earcut on each half
                    earcutLinked(a, triangles, dim, minX, minY, size, Int.MIN_VALUE)
                    earcutLinked(c, triangles, dim, minX, minY, size, Int.MIN_VALUE)
                    return
                }
                b = b.next
            }
            a = a!!.next
        } while (a !== start)
    }

    private fun isValidDiagonal(a: Node?, b: Node?): Boolean {
        //return a.next.i != b.i && a.prev.i != b.i && !intersectsPolygon(a, b) && locallyInside(a, b) && locallyInside(b, a) && middleInside(a, b);
        return a!!.next!!.i != b!!.i && a.prev!!.i != b.i && !intersectsPolygon(
            a,
            b
        ) &&  // dones't intersect other edges
                (locallyInside(a, b) && locallyInside(b, a) && middleInside(a, b) &&  // locally visible
                        (area(a.prev, a, b.prev) != 0f || area(
                            a,
                            b.prev,
                            b
                        ) != 0f) ||  // does not create opposite-facing sectors
                        equals(a, b) && area(a.prev, a, a.next) > 0 && area(
                    b.prev, b, b.next
                ) > 0) // special zero-length case
    }

    private fun middleInside(a: Node?, b: Node?): Boolean {
        var p = a
        var inside = false
        val px = (a!!.x + b!!.x) / 2
        val py = (a.y + b.y) / 2
        do {
            if (p!!.y > py != p.next!!.y > py && px < (p.next!!.x - p.x) * (py - p.y) / (p.next!!.y - p.y) + p.x) inside =
                !inside
            p = p.next
        } while (p !== a)
        return inside
    }

    private fun intersectsPolygon(a: Node?, b: Node?): Boolean {
        var p = a
        do {
            if (p!!.i != a!!.i && p.next!!.i != a.i && p.i != b!!.i && p.next!!.i != b.i && intersects(
                    p,
                    p.next,
                    a,
                    b
                )
            ) return true
            p = p.next
        } while (p !== a)
        return false
    }

    private fun intersects(p1: Node?, q1: Node?, p2: Node?, q2: Node?): Boolean {
        if (equals(p1, p2) && equals(q1, q2) || equals(p1, q2) && equals(p2, q1)) return true
        val o1 = sign(area(p1, q1, p2))
        val o2 = sign(area(p1, q1, q2))
        val o3 = sign(area(p2, q2, p1))
        val o4 = sign(area(p2, q2, q1))
        if (o1 != o2 && o3 != o4) return true // general case
        if (o1 == 0f && onSegment(p1, p2, q1)) return true // p1, q1 and p2 are collinear and p2 lies on p1q1
        if (o2 == 0f && onSegment(p1, q2, q1)) return true // p1, q1 and q2 are collinear and q2 lies on p1q1
        if (o3 == 0f && onSegment(p2, p1, q2)) return true // p2, q2 and p1 are collinear and p1 lies on p2q2
        return o4 == 0f && onSegment(
                p2,
                q1,
                q2
            ) // p2, q2 and q1 are collinear and q1 lies on p2q2
    }

    // for collinear points p, q, r, check if point q lies on segment pr
    private fun onSegment(p: Node?, q: Node?, r: Node?): Boolean {
        return q!!.x <= Math.max(p!!.x, r!!.x) && q.x >= Math.min(p.x, r.x) && q.y <= Math.max(
            p.y, r.y
        ) && q.y >= Math.min(p.y, r.y)
    }

    private fun sign(num: Float): Float {
        return if (num > 0) 1f else if (num < 0) -1f else 0f
    }

    private fun cureLocalIntersections(start: Node?, triangles: MutableList<Int>, dim: Int): Node? {
        var start = start
        var p = start
        do {
            val a = p!!.prev
            val b = p.next!!.next
            if (!equals(a, b) && intersects(a, p, p.next, b) && locallyInside(a, b) && locallyInside(b, a)) {
                triangles.add(a!!.i / dim)
                triangles.add(p.i / dim)
                triangles.add(b!!.i / dim)

                // remove two nodes involved
                removeNode(p)
                removeNode(p.next)
                start = b
                p = start
            }
            p = p.next
        } while (p !== start)
        return filterPoints(p, null)
    }

    private fun isEar(ear: Node?): Boolean {
        val a = ear!!.prev
        val c = ear.next
        if (area(a, ear, c) >= 0) return false // reflex, can't be an ear

        // now make sure we don't have other points inside the potential ear
        var p = ear.next!!.next
        while (p !== ear.prev) {
            if (pointInTriangle(
                    a!!.x, a.y,
                    ear.x, ear.y, c!!.x, c.y, p!!.x, p.y
                ) && area(
                    p.prev, p, p.next
                ) >= 0
            ) return false
            p = p.next
        }
        return true
    }

    private fun isEarHashed(ear: Node?, minX: Float, minY: Float, invSize: Float): Boolean {
        val a = ear!!.prev
        val c = ear.next
        if (area(a, ear, c) >= 0) return false // reflex, can't be an ear

        // triangle bbox; min & max are calculated like this for speed
        val minTX = if (a!!.x < ear.x) if (a.x < c!!.x) a.x else c.x else if (ear.x < c!!.x) ear.x else c.x
        val minTY = if (a.y < ear.y) if (a.y < c.y) a.y else c.y else if (ear.y < c.y) ear.y else c.y
        val maxTX = if (a.x > ear.x) if (a.x > c.x) a.x else c.x else if (ear.x > c.x) ear.x else c.x
        val maxTY = if (a.y > ear.y) if (a.y > c.y) a.y else c.y else if (ear.y > c.y) ear.y else c.y

        // z-order range for the current triangle bbox;
        val minZ = zOrder(minTX, minTY, minX, minY, invSize)
        val maxZ = zOrder(maxTX, maxTY, minX, minY, invSize)

        // first look for points inside the triangle in increasing z-order
        var p = ear.prevZ
        var n = ear.nextZ
        while (p != null && p.z >= minZ && n != null && n.z <= maxZ) {
            if (p !== ear.prev && p !== ear.next && pointInTriangle(
                    a.x, a.y,
                    ear.x,
                    ear.y, c.x, c.y, p.x, p.y
                ) && area(p.prev, p, p.next) >= 0
            ) return false
            p = p.prevZ
            if (n !== ear.prev && n !== ear.next && pointInTriangle(
                    a.x, a.y,
                    ear.x,
                    ear.y, c.x, c.y, n.x, n.y
                ) && area(n.prev, n, n.next) >= 0
            ) return false
            n = n.nextZ
        }

        // look for remaining points in decreasing z-order
        while (p != null && p.z >= minZ) {
            if (p !== ear.prev && p !== ear.next && pointInTriangle(
                    a.x, a.y,
                    ear.x,
                    ear.y, c.x, c.y, p.x, p.y
                ) && area(p.prev, p, p.next) >= 0
            ) return false
            p = p.prevZ
        }

        // look for remaining points in increasing z-order
        while (n != null && n.z <= maxZ) {
            if (n !== ear.prev && n !== ear.next && pointInTriangle(
                    a.x, a.y,
                    ear.x,
                    ear.y, c.x, c.y, n.x, n.y
                ) && area(n.prev, n, n.next) >= 0
            ) return false
            n = n.nextZ
        }
        return true
    }

    // z-order of a point given coords and inverse of the longer side of data bbox
    private fun zOrder(x: Float, y: Float, minX: Float, minY: Float, invSize: Float): Float {
        // coords are transformed into non-negative 15-bit integer range
        var lx = java.lang.Float.valueOf(32767 * (x - minX) * invSize).toInt()
        var ly = java.lang.Float.valueOf(32767 * (y - minY) * invSize).toInt()
        lx = lx or (lx shl 8) and 0x00FF00FF
        lx = lx or (lx shl 4) and 0x0F0F0F0F
        lx = lx or (lx shl 2) and 0x33333333
        lx = lx or (lx shl 1) and 0x55555555
        ly = ly or (ly shl 8) and 0x00FF00FF
        ly = ly or (ly shl 4) and 0x0F0F0F0F
        ly = ly or (ly shl 2) and 0x33333333
        ly = ly or (ly shl 1) and 0x55555555
        return (lx or (ly shl 1)).toFloat()
    }

    private fun indexCurve(start: Node, minX: Float, minY: Float, invSize: Float) {
        var p: Node? = start
        do {
            if (p!!.z == Float.MIN_VALUE) p.z = zOrder(p.x, p.y, minX, minY, invSize)
            p.prevZ = p.prev
            p.nextZ = p.next
            p = p.next
        } while (p !== start)
        p.prevZ!!.nextZ = null
        p.prevZ = null
        sortLinked(p)
    }

    private fun sortLinked(list: Node?): Node? {
        var list = list
        var inSize = 1
        var numMerges: Int
        do {
            var p = list
            list = null
            var tail: Node? = null
            numMerges = 0
            while (p != null) {
                numMerges++
                var q = p
                var pSize = 0
                for (i in 0 until inSize) {
                    pSize++
                    q = q!!.nextZ
                    if (q == null) break
                }
                var qSize = inSize
                while (pSize > 0 || qSize > 0 && q != null) {
                    var e: Node?
                    if (pSize == 0) {
                        e = q
                        q = q!!.nextZ
                        qSize--
                    } else if (qSize == 0 || q == null) {
                        e = p
                        p = p!!.nextZ
                        pSize--
                    } else if (p!!.z <= q.z) {
                        e = p
                        p = p.nextZ
                        pSize--
                    } else {
                        e = q
                        q = q.nextZ
                        qSize--
                    }
                    if (tail != null) tail.nextZ = e else list = e
                    e!!.prevZ = tail
                    tail = e
                }
                p = q
            }
            tail!!.nextZ = null
            inSize *= 2
        } while (numMerges > 1)
        return list
    }

    private fun eliminateHoles(data: FloatArray, holeIndices: IntArray?, outerNode: Node, dim: Int): Node {
        var outerNode = outerNode
        val queue: MutableList<Node?> = ArrayList()
        val len = holeIndices!!.size
        for (i in 0 until len) {
            val start = holeIndices[i] * dim
            val end = if (i < len - 1) holeIndices[i + 1] * dim else data.size
            val list = linkedList(data, start, end, dim, false)
            if (list === list!!.next) list!!.steiner = true
            queue.add(getLeftmost(list))
        }
        queue.sortWith(java.util.Comparator { o1, o2 ->
            if (o1!!.x - o2!!.x > 0) return@Comparator 1 else if (o1.x - o2.x < 0) return@Comparator -2
            0
        })
        for (node in queue) {
            eliminateHole(node, outerNode)
            outerNode = filterPoints(outerNode, outerNode.next)!!
        }
        return outerNode
    }

    private fun filterPoints(start: Node?, end: Node?): Node? {
        var end = end
        if (start == null) return start
        if (end == null) end = start
        var p = start
        var again: Boolean
        do {
            again = false
            if (!p!!.steiner && equals(p, p.next) || area(
                    p.prev, p, p.next
                ) == 0f
            ) {
                removeNode(p)
                end = p.prev
                p = end
                if (p === p!!.next) break
                again = true
            } else {
                p = p.next
            }
        } while (again || p !== end)
        return end
    }

    private fun equals(p1: Node?, p2: Node?): Boolean {
        return p1!!.x == p2!!.x && p1.y == p2.y
    }

    private fun area(p: Node?, q: Node?, r: Node?): Float {
        return (q!!.y - p!!.y) * (r!!.x - q.x) - (q.x - p.x) * (r.y - q.y)
    }

    private fun eliminateHole(hole: Node?, outerNode: Node) {
        var outerNode: Node? = outerNode
        outerNode = findHoleBridge(hole, outerNode)
        if (outerNode != null) {
            val b = splitPolygon(outerNode, hole)

            // filter collinear points around the cuts
            filterPoints(outerNode, outerNode.next)
            filterPoints(b, b.next)
        }
    }

    private fun splitPolygon(a: Node?, b: Node?): Node {
        val a2 = Node(a!!.i, a.x, a.y)
        val b2 = Node(b!!.i, b.x, b.y)
        val an = a.next
        val bp = b.prev
        a.next = b
        b.prev = a
        a2.next = an
        an!!.prev = a2
        b2.next = a2
        a2.prev = b2
        bp!!.next = b2
        b2.prev = bp
        return b2
    }

    // David Eberly's algorithm for finding a bridge between hole and outer
    // polygon
    private fun findHoleBridge(hole: Node?, outerNode: Node?): Node? {
        var p = outerNode
        val hx = hole!!.x
        val hy = hole.y
        var qx = -Float.MAX_VALUE
        var m: Node? = null

        // find a segment intersected by a ray from the hole's leftmost point to
        // the left;
        // segment's endpoint with lesser x will be potential connection point
        do {
            if (hy <= p!!.y && hy >= p.next!!.y) {
                val x = p.x + (hy - p.y) * (p.next!!.x - p.x) / (p.next!!.y - p.y)
                if (x <= hx && x > qx) {
                    qx = x
                    if (x == hx) {
                        if (hy == p.y) return p
                        if (hy == p.next!!.y) return p.next
                    }
                    m = if (p.x < p.next!!.x) p else p.next
                }
            }
            p = p.next
        } while (p !== outerNode)
        if (m == null) return null
        if (hx == qx) return m // hole touches outer segment; pick leftmost endpoint

        // look for points inside the triangle of hole point, segment
        // intersection and endpoint;
        // if there are no points found, we have a valid connection;
        // otherwise choose the point of the minimum angle with the ray as
        // connection point
        val stop: Node = m
        val mx = m.x
        val my = m.y
        var tanMin = Float.MAX_VALUE
        var tan: Float
        p = m
        while (p !== stop) {
            if (hx >= p!!.x && p.x >= mx && pointInTriangle(
                    if (hy < my) hx else qx,
                    hy,
                    mx,
                    my,
                    if (hy < my) qx else hx,
                    hy,
                    p.x,
                    p.y
                )
            ) {
                tan = Math.abs(hy - p.y) / (hx - p.x) // tangential
                if (locallyInside(
                        p,
                        hole
                    ) && (tan < tanMin || tan == tanMin && (p.x > m!!.x || p.x == m.x && sectorContainsSector(m, p)))
                ) {
                    m = p
                    tanMin = tan
                }
            }
            p = p.next
        }
        return m
    }

    private fun locallyInside(a: Node?, b: Node?): Boolean {
        return if (area(a!!.prev, a, a.next) < 0) area(a, b, a.next) >= 0 && area(a, a.prev, b) >= 0 else area(
            a,
            b,
            a.prev
        ) < 0 || area(a, a.next, b) < 0
    }

    // whether sector in vertex m contains sector in vertex p in the same
    // coordinates
    private fun sectorContainsSector(m: Node?, p: Node?): Boolean {
        return area(m!!.prev, m, p!!.prev) < 0 && area(
            p.next, m, m.next
        ) < 0
    }

    private fun pointInTriangle(
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        cx: Float,
        cy: Float,
        px: Float,
        py: Float
    ): Boolean {
        return (cx - px) * (ay - py) - (ax - px) * (cy - py) >= 0 && (ax - px) * (by - py) - (bx - px) * (ay - py) >= 0 && (bx - px) * (cy - py) - (cx - px) * (by - py) >= 0
    }

    private fun getLeftmost(start: Node?): Node? {
        var p = start
        var leftmost = start
        do {
            if (p!!.x < leftmost!!.x || p.x == leftmost.x && p.y < leftmost.y) leftmost = p
            p = p.next
        } while (p !== start)
        return leftmost
    }

    private fun linkedList(data: FloatArray, start: Int, end: Int, dim: Int, clockwise: Boolean): Node? {
        var last: Node? = null
        if (clockwise == signedArea(data, start, end, dim) > 0) {
            var i = start
            while (i < end) {
                last = insertNode(i, data[i], data[i + 1], last)
                i += dim
            }
        } else {
            var i = end - dim
            while (i >= start) {
                last = insertNode(i, data[i], data[i + 1], last)
                i -= dim
            }
        }
        if (last != null && equals(last, last.next)) {
            removeNode(last)
            last = last.next
        }
        return last
    }

    private fun removeNode(p: Node?) {
        p!!.next!!.prev = p.prev
        p.prev!!.next = p.next
        if (p.prevZ != null) {
            p.prevZ!!.nextZ = p.nextZ
        }
        if (p.nextZ != null) {
            p.nextZ!!.prevZ = p.prevZ
        }
    }

    private fun insertNode(i: Int, x: Float, y: Float, last: Node?): Node {
        val p = Node(i, x, y)
        if (last == null) {
            p.prev = p
            p.next = p
        } else {
            p.next = last.next
            p.prev = last
            last.next!!.prev = p
            last.next = p
        }
        return p
    }

    private fun signedArea(data: FloatArray, start: Int, end: Int, dim: Int): Float {
        var sum = 0f
        var j = end - dim
        var i = start
        while (i < end) {
            sum += (data[j] - data[i]) * (data[i + 1] + data[j + 1])
            j = i
            i += dim
        }
        return sum
    }

    private class Node internal constructor(var i: Int, var x: Float, var y: Float) {
        var z: Float
        var steiner: Boolean
        var prev: Node? = null
        var next: Node? = null
        var prevZ: Node?
        var nextZ: Node?

        init {
            // vertice index in coordinates array

            // vertex coordinates

            // previous and next vertice nodes in a polygon ring

            // z-order curve value
            z = Float.MIN_VALUE

            // previous and next nodes in z-order
            prevZ = null
            nextZ = null

            // indicates whether this is a steiner point
            steiner = false
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("{i: ").append(i).append(", x: ").append(x).append(", y: ").append(y).append(", prev: ")
                .append(prev).append(", next: ").append(next)
            return sb.toString()
        }
    }
}