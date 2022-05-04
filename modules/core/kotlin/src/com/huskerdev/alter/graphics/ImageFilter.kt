package com.huskerdev.alter.graphics

import com.huskerdev.alter.internal.Pipeline

abstract class ImageFilter {

    companion object {

        @JvmStatic
        fun createGaussianBlur(radius: Int = 10) = Pipeline.current.createGaussianBlurFilter(radius)
    }

    abstract fun process(input: Image): Image
}