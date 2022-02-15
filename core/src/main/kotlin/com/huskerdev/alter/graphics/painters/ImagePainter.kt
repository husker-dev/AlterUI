package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.Painter

abstract class ImagePainter: Painter {

    private lateinit var _image: Image
    var image: Image
        get() = _image
        set(value) {
            _image = value
            imageChanged = true
        }

    private var _x = 0f
    var x: Float
        get() = _x
        set(value) {
            _x = value
            sizeChanged = true
        }

    private var _y = 0f
    var y: Float
        get() = _y
        set(value) {
            _y = value
            sizeChanged = true
        }

    private var _width = 0f
    var width: Float
        get() = _width
        set(value) {
            _width = value
            sizeChanged = true
        }

    private var _height = 0f
    var height: Float
        get() = _height
        set(value) {
            _height = value
            sizeChanged = true
        }

    private var _color = Color.black
    var color: Color
        get() = _color
        set(value) {
            _color = value
            colorChanged = true
        }

    // States
    private var sizeChanged = true
    private var imageChanged = true
    private var colorChanged = true

    override fun checkChanges() {
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