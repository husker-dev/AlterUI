package com.huskerdev.alter.graphics.painters

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image

abstract class ImagePainter: ColorPainter() {

    var image: Image? = null
        set(value) {
            if (field != value)
                isImageChanged = true
            field = value
        }
    var x = 0f
        set(value) {
            if (field != value)
                isSizeChanged = true
            field = value
        }
    var y = 0f
        set(value) {
            if (field != value)
                isSizeChanged = true
            field = value
        }
    var width = 0f
        set(value) {
            if (field != value)
                isSizeChanged = true
            field = value
        }
    var height = 0f
        set(value) {
            if (field != value)
                isSizeChanged = true
            field = value
        }
    var isSizeChanged = true
    var isImageChanged = true

    abstract fun updateSize()
    abstract fun updateImage()

    override fun onBeginPaint(graphics: Graphics) {
        super.onBeginPaint(graphics)

        if (isSizeChanged) {
            isSizeChanged = false
            updateSize()
        }
        if (isImageChanged) {
            isImageChanged = false
            updateImage()
        }
    }
}