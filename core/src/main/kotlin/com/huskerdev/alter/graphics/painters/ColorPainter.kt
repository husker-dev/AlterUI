package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Painter

abstract class ColorPainter: Painter {

    private var colorChanged = true

    private var _color = Color.black
    var color: Color
        get() = _color
        set(value) {
            _color = value
            colorChanged = true
        }

    override fun checkChanges() {
        if(colorChanged){
            colorChanged = false
            updateColor()
        }
    }

    protected abstract fun updateColor()
}