package com.huskerdev.alter.graphics.filters

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageFilter

abstract class GaussianBlur(
    var radius: Int
): ImageFilter() {

    companion object {
        fun process(image: Image, radius: Int) = createGaussianBlur(radius).process(image)
    }
}