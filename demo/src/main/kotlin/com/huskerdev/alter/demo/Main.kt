package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ResizeAlgorithm
import java.lang.Thread.sleep
import kotlin.concurrent.thread

fun main() = AlterUI.takeMain {
    AlterUI.load()

    var image: Image? = null
    var scaled = arrayListOf<Image>()
    var filtered = false

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            if(image == null)
                return
            //gr.color = Color.black
            gr.color = if(filtered) Color.green else Color.red
            gr.fillRect(0f, height - 100f, 100f, 100f)

            gr.color = Color.white
            var x = 0f
            for(i in 0..5){
                gr.drawImage(if(filtered) scaled[i] else image!!, x, 0f, x + 20, x + 20)
                x += x + 20
            }

        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true

    thread {
        image = Image.createFromURL("https://sun2-12.userapi.com/impg/Iuzz_OiomMyAAo3a6zScURWTb-G378DibE-rlA/55RTcyR-NEk.jpg?keep_aspect_ratio=1&size=1228x608&quality=95&sign=e03cb6e9bd0ba501fc9b86dc424c0e22&c_uniq_tag=PabUN7G5EFHlWCgCIZPN7KJRBtMvHE4wypGoT1zlopc")

        var x = 0
        for(i in 0..5){
            scaled.add(image!!.getResized(x + 20, x + 20, ResizeAlgorithm.CatmullRom))
            x += x + 20
        }

        frame.repaint()
        while(true){
            sleep(1000)
            filtered = !filtered
            frame.repaint()
        }
    }
}