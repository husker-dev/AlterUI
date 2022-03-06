package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.font.Font
import com.huskerdev.alter.graphics.painters.*
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window

abstract class Graphics(var window: Window) {

    protected var matrix = Matrix4.identity
    private var lastMatrix = Matrix4.identity
    var width = -1f
    var height = -1f
    var dpi = 1f

    open var requiredPainter: Painter? = null                   // Preferred painter
    protected open var painter: Painter? = null                 // Actual painter
        set(value){
            if(field != value) {
                value?.enable()
                field = value
            }
        }

    var color: Color
        get() {
            if(requiredPainter is ColorPainter)
                return (requiredPainter as ColorPainter).color
            else throw UnsupportedOperationException("Painter is not ColorPainter")
        }
        set(value) {
            setColorPainter(value)
        }

    var font = Font.get(Platform.current.defaultFontFamily)

    fun begin() {
        beginImpl()

        setColorPainter(Color.black)
        checkPainter()
        if(width !=  window.width ||
            height != window.height ||
            dpi != window.dpi
        ) {
            width = window.width
            height = window.height
            dpi = window.dpi
            lastMatrix = Matrix4.ortho(window.clientWidth / dpi, window.clientHeight / dpi)
        }
        matrix = lastMatrix
        updateTransforms()
    }
    fun end() = endImpl()

    private fun checkPainter(){
        if(painter != requiredPainter)
            painter = requiredPainter
    }

    protected abstract fun beginImpl()
    protected abstract fun endImpl()
    protected abstract fun updateTransforms()
    abstract fun clear()

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            checkPainter()
            painter!!.checkPropertyChanges()
            painter!!.fillRect(x, y, width, height)
        }
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            checkPainter()
            painter!!.checkPropertyChanges()
            painter!!.drawRect(x, y, width, height)
        }
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white) {
        if(width > 0 && height > 0) {
            painter = getImagePainter()
            (painter as ImagePainter).apply {
                this.image = image
                this.x = x
                this.y = y
                this.width = width
                this.height = height
                this.color = color
            }
            painter!!.checkPropertyChanges()
            painter!!.fillRect(x, y, width, height)
        }
    }

    fun drawText(text: String, x: Float, y: Float){
        val rasterInfo = font.derived(font.size * dpi).getRasterMetrics(text)
        painter = getImagePainter()
        (painter as ImagePainter).color = color

        for(i in 0 until rasterInfo.count) {
            val glyph = rasterInfo.glyphs[i]
            val rx = x + rasterInfo.getGlyphX(i) / dpi
            val ry = y + rasterInfo.getGlyphY(i) / dpi
            val width = glyph.width.toFloat() / dpi
            val height = glyph.height.toFloat() / dpi

            if(width == 0f || height == 0f)
                continue

            (painter as ImagePainter).apply {
                this.image = glyph.image
                this.x = rx
                this.y = ry
                this.width = width
                this.height = height
            }
            painter!!.checkPropertyChanges()
            painter!!.fillRect(rx, ry, width, height)
        }

    }

    // Painters
    fun setColorPainter(color: Color){
        requiredPainter = getColorPainter()
        (requiredPainter as ColorPainter).apply {
            this.color = color
        }
    }

    fun setImagePainter(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white){
        requiredPainter = getImagePainter()
        (requiredPainter as ImagePainter).apply {
            this.image = image
            this.x = x
            this.y = y
            this.width = width
            this.height = height
            this.color = color
        }
    }

    protected abstract fun getColorPainter(): ColorPainter
    protected abstract fun getImagePainter(): ImagePainter
}