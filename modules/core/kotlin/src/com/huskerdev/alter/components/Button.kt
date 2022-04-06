package com.huskerdev.alter.components

import com.huskerdev.alter.geom.RoundRectangle
import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image

open class Button: Component() {

    lateinit var shape: Shape

    init {
        preferredWidth = 200f
        preferredHeight = 40f

        onResized {
            shape = RoundRectangle(0f, 0f, preferredWidth, preferredHeight, 20f)
        }
    }

    override fun paintComponent(gr: Graphics) {
        gr.color = Color.blue
        gr.fillShape(shape)
    }

    override fun doLayout() {

    }
}