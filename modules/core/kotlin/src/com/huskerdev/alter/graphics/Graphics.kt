package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.font.Font
import com.huskerdev.alter.graphics.painters.*
import com.huskerdev.alter.internal.Platform

enum class PixelType(val channels: Int) {
    MONO(1),
    RGB(3),
    RGBA(4)
}

abstract class Graphics {

    //protected var matrix = Matrix4.identity

    abstract val width: Float
    abstract val height: Float
    abstract val physicalWidth: Int
    abstract val physicalHeight: Int

    abstract val dpi: Float
    abstract val pixelType: PixelType

    // ColorPainter is default
    open var painter: Painter? = null
        set(value) {
            field?.onDisable()
            field = value ?: getColorPainter()
            field!!.onEnable()
        }

    var color = Color.black
    var font = Font.get(Platform.current.defaultFontFamily)

    init {
        reset()
    }

    open fun reset(){
        color = Color.black
        painter = null
        font = Font.get(Platform.current.defaultFontFamily)
    }

    abstract fun finish()

    private fun isValidRect(x: Float, y: Float, width: Float, height: Float): Boolean {
        return (width > 0) && (height > 0) &&
                (x + width > 0) && (y + height > 0) &&
                (x <= this.width) && (y <= this.height)
    }

    open fun clear() {
        painter!!.runPaint {
            clear()
        }
    }

    open fun fillShape(shape: Shape) {
        if(shape.fillVertices.isNotEmpty()) {
            painter!!.runPaint {
                fillShape(shape)
            }
        }
    }

    open fun drawShape(shape: Shape) {
        painter!!.runPaint {
            drawShape(shape)
        }
    }

    open fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        if(isValidRect(x, y, width, height)) {
            painter!!.runPaint {
                fillRect(x, y, width, height)
            }
        }
    }

    open fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        if(isValidRect(x, y, width, height)) {
            painter!!.runPaint {
                drawRect(x, y, width, height)
            }
        }
    }

    open fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        if(isValidRect(x, y, width, height)) {
            painter!!.runPaint {
                drawImage(image, x, y, width, height)
            }
        }
    }

    open fun drawText(text: String, x: Float, y: Float) {
        val rasterizer = font.derived(font.size * dpi).getRasterMetrics(text, false)
        painter!!.runPaint {
            drawText(rasterizer.rasterImage, x, y, rasterizer.width / dpi, rasterizer.height / dpi)
        }
    }

    private inline fun Painter.runPaint(block: Painter.() -> Unit){
        onBeginPaint(this@Graphics)
        block()
        onEndPaint()
    }

/*
    open fun drawText(text: String, x: Float, y: Float, useSubpixel: Boolean = false){
        val rasterInfo = font.derived(font.size * dpi).getRasterMetrics(text, useSubpixel)
        painter = getImagePainter().apply {
            this.color = this@Graphics.color
            this.isLcd = useSubpixel
        }

        for(i in 0 until rasterInfo.count) {
            val glyph = rasterInfo.glyphs[i]
            val rx = x + (rasterInfo.getGlyphX(i) + rasterInfo.baselineX) / dpi
            val ry = y + (rasterInfo.getGlyphY(i) + rasterInfo.baselineY) / dpi
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
            //painter!!.checkPropertyChanges()
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

     */

    protected abstract fun getColorPainter(): ColorPainter
    protected abstract fun getImagePainter(): ImagePainter
}