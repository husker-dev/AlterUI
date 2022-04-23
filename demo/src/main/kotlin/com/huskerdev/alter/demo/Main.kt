package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.Monitor
import com.huskerdev.alter.geom.*
import com.huskerdev.alter.graphics.*
import com.huskerdev.alter.graphics.font.Font
import com.huskerdev.alter.internal.WindowStyle
import com.huskerdev.alter.internal.platforms.win.WMonitorPeer
import com.huskerdev.alter.internal.utils.MainThreadLocker
import kotlin.concurrent.thread

fun main() = AlterUI.run {

    //g.fillShape(ellipse)

    val ellipse = Ellipse(10f, 150f, 100f, 100f)
    val rectangle = Rectangle(150f, 230f, 100f, 100f)

    ellipse.stroke.apply {
        width = 4f
        join = LineJoin.Round
    }
    rectangle.stroke = ellipse.stroke

    val font = Font.get("arial")
    println(font.family.copyright)

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)


            gr.color = Color.white

            gr.drawShape(ellipse)
            gr.drawShape(rectangle)

        }
    }


    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true
    frame.background = Color.rgba(0.2f, 0.3f, 0.6f, 1f)

    frame.onMouseMoved {
        frame.repaint()
    }
     
}