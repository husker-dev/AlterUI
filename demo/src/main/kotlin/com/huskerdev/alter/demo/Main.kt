package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.Pipeline
import kotlin.concurrent.thread

fun main() = AlterUI.takeMain {
    AlterUI.load()

    Runtime.getRuntime().addShutdownHook(Thread{
        println("JVM shutdown")
    })

    val window = Pipeline.current.createWindow()
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    window.x = 200.0
    window.y = 200.0
    window.width = 500.0
    window.height = 500.0
    window.background = Color.rgb(0.9f, 0.9f, 0f)

    var image: Image? = null


    window.apply {
        window.onPaintEvent = { gr ->
            gr.clear()

            gr.color = background
            gr.fillRect(0f, 0f, width.toFloat(), height.toFloat())

            gr.color = Color.white
            gr.fillRect(100f, 100f, width.toFloat() - 200f, height.toFloat() - 200f)

            gr.color = Color.rgba(0f, 0f, 0f, 0.8f)
            gr.fillRect(0f, 0f, 200f, 200f)

            gr.color = Color.rgba(0f, 0f, 1f, 0.8f)
            gr.fillRect(width.toFloat() - 200f, 0f, 200f, 200f)

            gr.color = Color.rgba(1f, 0f, 0f, 0.8f)
            gr.fillRect(width.toFloat() - 200f, height.toFloat() - 200f, 200f, 200f)

            gr.color = Color.rgba(0f, 1f, 0f, 0.8f)
            gr.fillRect(0f, height.toFloat() - 200f, 200f, 200f)

            if(image != null)
                gr.drawImage(image!!, width.toFloat() - 300f, 0f, 300f, 300f)
        }
    }

    thread {
        image = Image.createFromURL("https://sun9-57.userapi.com/impg/1bZ2JejlcpxeEiIUd7OEcbyjJ5XpOvdhuOrKew/qigWWTeP694.jpg?size=1242x1515&quality=96&sign=3696360abe9d16443f11887a0ff78b82&type=album")
        window.requestRepaint()
    }

    window.visible = true
}