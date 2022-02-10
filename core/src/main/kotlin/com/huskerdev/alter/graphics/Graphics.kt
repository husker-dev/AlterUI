package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Matrix
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Window

abstract class Graphics(var window: Window) {

    protected lateinit var matrix: Matrix
    private lateinit var lastMatrix: Matrix
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
            lastMatrix = Matrix.ortho(window.width.toFloat(), window.height.toFloat(), 100f, -1f)
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
        if(width > 0 && height > 0)
            painter.fillRect(x, y, width, height)
    }

    // Painters
    fun setColorPainter(color: Color){
        _painter = getColorPainter().apply { this.color = color }
        _painter.enable()
    }
    protected abstract fun getColorPainter(): ColorPainter
}