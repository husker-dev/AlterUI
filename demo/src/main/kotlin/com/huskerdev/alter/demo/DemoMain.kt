package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.geom.*
import com.huskerdev.alter.graphics.*
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.internal.Pipeline
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask
import kotlin.math.*

fun main() = AlterUI.run {
    var image: Image? = null
    var blurredImage: Image? = null

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)
            gr.color = Color.white

            if(image != null)
                gr.drawImage(image!!, 0f, 0f, 300f, 300f)
            if(blurredImage != null)
                gr.drawImage(blurredImage!!, 300f, 0f, 300f, 300f)
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true
    frame.background = Color.rgba(0.2f, 0.3f, 0.6f, 1f)


    thread {
        image = Image.fromFile("C:\\Users\\redfa\\Desktop\\15104f78cb83e3cefaf63ecc718a2a43.jpg")
        frame.repaint()

        blurredImage = GaussianBlur.process(image!!, 40)
        frame.repaint()
    }


}
