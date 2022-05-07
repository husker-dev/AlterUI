package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.os.*
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.cos

fun main() = AlterUI.run {
    var image: Image? = null
    var blurredImage: Image? = null
    var radius = 2.0
    val maxRadius = 50.0

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)
            gr.color = Color.white

            if(image != null)
                gr.drawImage(image!!, 0f, 0f, 400f, 400f)
            if(blurredImage != null)
                gr.drawImage(blurredImage!!, 400f, 0f, 400f, 400f)

            gr.color = Color.rgb(0.0f, 0.6f, 0.0f)
            gr.fillRect(400f, 400f, 400f, 20f)

            gr.color = Color.green
            gr.fillRect(400f, 400f, (400f * ((radius - 2) / maxRadius)).toFloat(), 20f)
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 830f
    frame.height = 500f
    frame.background = Color.rgba(0.2f, 0.3f, 0.6f, 1f)
    frame.visible = true

    thread(isDaemon = true) {
        val dialog = FileDialog(FileDialogType.Open)
        dialog.directory = File("C:\\Users\\redfa\\Desktop")
        dialog.addFilters(FileDialog.ImageFilter, FileDialog.AllFiles)

        image = Image.fromFile(dialog.show()[0])
        frame.repaint()

        var anim = 0.0
        while(true){
            anim += 0.01
            radius = cos(anim) * (maxRadius / 2) + (maxRadius / 2) + 2

            val newBlurredImage = GaussianBlur.process(image!!, radius.toInt())
            blurredImage?.dispose()
            blurredImage = newBlurredImage
            frame.repaint()

            Thread.sleep(10)
        }
    }



}

