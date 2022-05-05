package com.huskerdev.alter.graphics.filters

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageFilter

abstract class GaussianBlur(
    var radius: Int
): ImageFilter() {

    companion object {
        fun process(image: Image, radius: Int) = createGaussianBlur(radius).process(image)
    }

    override fun process(input: Image): Image {
        if(radius < 1)
            throw UnsupportedOperationException("Blur radius can not less than 1")
        return processImpl(input)
    }

    protected abstract fun processImpl(input: Image): Image
}