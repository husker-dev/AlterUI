package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Matrix4
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.Window

abstract class Graphics(var window: Window) {

    protected var matrix = Matrix4.identity
    private var lastMatrix = Matrix4.identity
    var width = -1
    var height = -1
    var dpi = 1f

    open var painter: Painter? = null
        protected set(value){
            field = value
            value?.enable()
        }

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
        if(width != window.width ||
            height != window.height ||
            dpi != window.dpi
        ) {
            width = window.width
            height = window.height
            dpi = window.dpi
            lastMatrix = Matrix4.ortho(window.width.toFloat(), window.height.toFloat())
        }
        matrix = lastMatrix
        updateTransforms()
    }
    fun end() = endImpl()

    protected abstract fun beginImpl()
    protected abstract fun endImpl()
    protected abstract fun updateTransforms()
    abstract fun clear()

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            painter!!.checkChanges()
            painter!!.fillRect(x, y, width, height)
        }
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        if(width > 0 && height > 0) {
            painter!!.checkChanges()
            painter!!.drawRect(x, y, width, height)
        }
    }

    fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white) {
        if(width > 0 && height > 0) {
            val oldPainter = painter
            setImagePainter(image, x, y, width, height, color)
            fillRect(x, y, width, height)
            painter = oldPainter
        }
    }

    // Painters
    fun setColorPainter(color: Color){
        painter = getColorPainter()
        (painter as ColorPainter).apply {
            this.color = color
        }
    }

    fun setImagePainter(image: Image, x: Float, y: Float, width: Float, height: Float, color: Color = Color.white){
        painter = getImagePainter()
        (painter as ImagePainter).apply {
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