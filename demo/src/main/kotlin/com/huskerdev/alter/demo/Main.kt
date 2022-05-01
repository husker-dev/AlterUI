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
    val ellipse = Ellipse(10f, 150f, 100f, 100f)
    val rectangle = Rectangle(150f, 230f, 100f, 100f)

    ellipse.stroke.apply {
        width = 4f
        join = LineJoin.Miter
    }
    rectangle.stroke = ellipse.stroke


    val image = Image.fromFile("C:\\Users\\redfa\\Desktop\\15104f78cb83e3cefaf63ecc718a2a43.jpg")
    image.graphics.apply {
        color = Color.red
        fillRect(0f, 0f, 100f, 100f)
    }

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)
            gr.color = Color.white

            gr.drawShape(ellipse)
            gr.drawShape(rectangle)

            gr.drawImage(image, 10f, 10f, 100f, 100f)
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true
    frame.background = Color.rgba(0.2f, 0.3f, 0.6f, 1f)
    frame.titleColor = Color.red
    frame.textColor = Color.blue

    frame.onMouseMoved {
        frame.repaint()
    }
}