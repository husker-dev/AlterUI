package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import kotlin.concurrent.thread

fun main() = AlterUI.run {
    var image: Image? = null

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            gr.color = Color.white
            if(image != null) {
                gr.drawImage(image!!, (mousePosition?.x ?: 0f) - 50f, (mousePosition?.y ?: 0f) - 50f, 100f, 100f)
                gr.drawImage(image!!, width - 100f, height - 100f, 100f, 100f)
            }
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    //frame.style = WindowStyle.Undecorated
    frame.visible = true

    frame.onMouseMoved {
        frame.repaint()
    }

    thread {
        image = Image.fromURL("https://sun9-57.userapi.com/impf/1gtNjgQZDAlwcQturNhBY48C8dWAX1ldwF-mcA/HC1UBUhu9WQ.jpg?size=381x287&quality=96&sign=e40baa38284de39e16c1fe70f9be435a&type=album")
        frame.repaint()
    }

}