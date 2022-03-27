package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Painter

abstract class ColorPainter: Painter() {

    var color = Color.rgba(-1f, -1f, -1f, -1f)

    abstract fun updateColor()

    override fun onBeginPaint(graphics: Graphics) {
        super.onBeginPaint(graphics)

        if(color != graphics.color){
            color = graphics.color
            updateColor()
        }
    }
}