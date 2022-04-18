package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.geom.*
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.LineCap
import com.huskerdev.alter.graphics.LineJoin
import com.huskerdev.alter.graphics.font.Font

fun main() = AlterUI.run {
    val ellipse = Ellipse(100f, 100f, 100f, 100f)
    val rectangle = Rectangle(250f, 100f, 100f, 100f)

    println(Font.get("arial").family.copyright)

    ellipse.stroke.join = LineJoin.None
    rectangle.stroke = ellipse.stroke

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)
            gr.color = Color.rgba(0f, 0f, 0f, 0.5f)

            ellipse.stroke.width = mousePosition!!.toVector().length / 20f

            val pos1 = Point(400f, 200f)
            val pos2 = mousePosition ?: Point(0f, 0f)
            val pos3 = Point(100f, 300f)
            val pos4 = Point(200f, 300f)
            val pos5 = Point(300f, 300f)

            val lineStrip = LineStrip(pos1, pos2, pos3, pos5)
            lineStrip.stroke.width = 20f
            lineStrip.stroke.join = LineJoin.Round
            lineStrip.stroke.cap = LineCap.None

            gr.drawShape(lineStrip)
            //gr.drawShape(ellipse)
            //gr.drawShape(rectangle)
        }
    }


    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true

    frame.onMouseMoved {
        frame.repaint()
    }
}