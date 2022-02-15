package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.Window

abstract class Graphics(var window: Window) {

    protected var matrix = Matrix.identity
    private var lastMatrix = Matrix.identity
    private var lastWidth = -1.0
    private var lastHeight = -1.0

    private lateinit var _painter: Painter
    val painter: Painter
        get() = _painter

    var color: Color
        get() {
            if(painter is ColorPainter)
                return (painter as ColorPainter).color
            else throw UnsupportedOperationException("Painter is not ColorPainter")
        }
        set(value) {
            setColorPainter(value)
        }

    fun begin() {
        beginImpl()

        setColorPainter(Color.black)

        if(lastWidth != window.width || lastHeight != window.height) {
            lastWidth = window.width
            lastHeight = window.height
            lastMatrix = Matrix.ortho(window.width.toFloat(), window.height.toFloat())
        }
        matrix = lastMatrix
        updateMatrix()

    }
    fun end() = endImpl()

    protected abstract fun beginImpl()
    protected abstract fun endImpl()
    protected abstract fun updateMatrix()
    abstract fun clear()

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            painter.checkChanges()
            painter.fillRect(x, y, width, height)
        }
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            painter.checkChanges()
            painter.drawRect(x, y, width, height)
        }
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white) {
        if(width > 0 && height > 0) {
            val oldPainter = painter
            setImagePainter(image, x, y, width, height, color)
            fillRect(x, y, width, height)
            setPainter(oldPainter)
        }
    }

    // Painters
    fun setColorPainter(color: Color){
        setPainter(getColorPainter())
        (painter as ColorPainter).apply {
            this.color = color
        }
    }

    fun setImagePainter(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white){
        setPainter(getImagePainter())
        (painter as ImagePainter).apply {
            this.image = image
            this.x = x
            this.y = y
            this.width = width
            this.height = height
            this.color = color
        }
    }

    protected open fun setPainter(painter: Painter){
        _painter = painter
        _painter.enable()
    }

    protected abstract fun getColorPainter(): ColorPainter
    protected abstract fun getImagePainter(): ImagePainter
}