package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.components.Button
import com.huskerdev.alter.components.FlowPane
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.pipelines.gl.GLResourceContext
import com.huskerdev.alter.os.*
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.cos

fun main() = AlterUI.run {

    val image = Image.fromFile("C:\\Users\\redfa\\Desktop\\avatar_snow.png")
    var blurred: Image? = null

    val frame = object: Frame(){
        init {
            content = object: FlowPane() {
                init {
                    children.add(Button())
                }
            }
        }

        override fun paint(gr: Graphics) {
            super.paint(gr)

            if(blurred != null)
                gr.drawImage(blurred!!, 0f, 0f, 400f, 400f)
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 1030f
    frame.height = 500f
    frame.background = Color.white
    frame.visible = true


    thread(isDaemon = true) {

        var anim = 0.0
        while(true) {
            anim += 0.1
            val radius = cos(anim) * 200 + 202

            val newImage = GaussianBlur.process(image, radius.toInt())

            blurred?.dispose()
            blurred = newImage
            frame.repaint()

            Thread.sleep(100)
        }
    }


}

