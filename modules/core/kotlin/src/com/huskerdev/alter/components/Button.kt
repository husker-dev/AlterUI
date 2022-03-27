package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image

open class Button: Component() {

    companion object {
        val texture by lazy {
            Image.fromFile("C:\\Users\\redfa\\Desktop\\button.png")
        }
    }

    init {
        preferredWidth = 200f
        preferredHeight = 40f
    }

    override fun paintComponent(gr: Graphics) {
        gr.color = Color.white
        gr.drawImage(texture, 0f, 0f, width, height)
        //gr.fillRect(0f, 0f, width, height)
    }

    override fun doLayout() {

    }
}