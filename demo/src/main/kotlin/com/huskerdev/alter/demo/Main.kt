package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.WindowStyle
import kotlin.concurrent.thread

fun main() = AlterUI.takeMain {
    AlterUIProperties.pipeline = "d3d9"
    AlterUI.load()

    val images = arrayListOf<Image>()

    val frame = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)


            //gr.color = Color.black
            gr.color = Color.white
            var x = 0
            var y = 0
            for(image in images){
                gr.drawImage(image, x.toFloat(), y.toFloat(), 150f, 150f)

                x += 150
                if(x > 500){
                    x = 0
                    y += 150
                }
            }
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.style = WindowStyle.Undecorated
    frame.visible = true

    thread {
        val urls = arrayListOf(
            "https://funart.pro/uploads/posts/2021-03/1617054432_6-p-oboi-priroda-4k-6.jpg",
            "https://i.pinimg.com/originals/89/0b/d5/890bd505a9782aaa483b93183d647a78.jpg",
            "https://images4.alphacoders.com/772/772692.jpg",
            "https://www.xtrafondos.com/wallpapers/galaxia-en-el-espacio-4052.jpg",
            "https://i.pinimg.com/originals/63/e6/f7/63e6f79d0687eeed56b74d925aaa9169.jpg",
            "https://www.100hdwallpapers.com/wallpapers/2560x1440/astronaut_dream_1-hd_wallpapers.jpg",
            "https://wallpaper-mania.com/wp-content/uploads/2018/09/High_resolution_wallpaper_background_ID_77701924923.jpg",
            "https://wallpaper-mania.com/wp-content/uploads/2018/09/High_resolution_wallpaper_background_ID_77701914283.jpg",
            "https://i.pinimg.com/originals/e1/df/fc/e1dffc73b671a341dd0c34cd5cd5b400.jpg",
            "https://i.pinimg.com/originals/9c/df/56/9cdf56a9bbe523bfbbbb94bf82058caa.jpg",
            "https://free4kwallpapers.com/uploads/originals/2016/12/03/deep-space-nebula-4k-8k-wallpaper.jpg"
        )

        for(url in urls) {
            try {
                images.add(Image.fromURL(url))
                frame.repaint()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}