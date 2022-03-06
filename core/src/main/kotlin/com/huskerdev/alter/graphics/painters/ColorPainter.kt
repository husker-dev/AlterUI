package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Painter

abstract class ColorPainter: Painter {

    private var colorChanged = true

    open var color = Color.black
        set(value) {
            field = value
            colorChanged = true
        }

    override fun checkPropertyChanges() {
        if(colorChanged){
            colorChanged = false
            updateColor()
        }
    }

    protected abstract fun updateColor()
}