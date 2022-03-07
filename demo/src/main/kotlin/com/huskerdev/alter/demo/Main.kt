package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.font.Font

fun main() = AlterUI.takeMain {
    AlterUI.load()

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            gr.color = Color.black

            var y = 0
            for(i in 10..100 step 10){
                gr.font = Font.get("lobster").derived(i.toFloat())

                gr.drawText("This is sample text", 100f, 100f + y, false)
                y += i + 2
            }
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true

}