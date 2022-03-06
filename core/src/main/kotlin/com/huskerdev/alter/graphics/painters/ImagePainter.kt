package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.Painter

abstract class ImagePainter: Painter {

    var image: Image? = null
        set(value) {
            field = value
            imageChanged = true
        }

    var x = 0f
        set(value) {
            field = value
            sizeChanged = true
        }

    var y = 0f
        set(value) {
            field = value
            sizeChanged = true
        }

    var width = 0f
        set(value) {
            field = value
            sizeChanged = true
        }

    var height = 0f
        set(value) {
            field = value
            sizeChanged = true
        }

    var color = Color.black
        set(value) {
            field = value
            colorChanged = true
        }

    // States
    private var sizeChanged = true
    private var imageChanged = true
    private var colorChanged = true

    override fun checkPropertyChanges() {
        if(colorChanged){
            colorChanged = false
            updateColor()
        }
        if(sizeChanged){
            sizeChanged = false
            updateSize()
        }
        if(imageChanged){
            imageChanged = false
            updateImage()
        }
    }

    protected abstract fun updateColor()
    protected abstract fun updateSize()
    protected abstract fun updateImage()
}