package com.huskerdev.alter.graphics

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.font.FontRasterMetrics

abstract class Painter {

    lateinit var currentGraphics: Graphics
    var isLoaded = false

    open fun onBeginPaint(graphics: Graphics){
        currentGraphics = graphics
        if(!isLoaded){
            isLoaded = true
            onLoad()
        }
    }

    // Reserved for painter purposes
    open fun onLoad(){}
    open fun onEndPaint(){}
    open fun onEnable(){}
    open fun onDisable(){}

    abstract fun clear()
    abstract fun fillShape(shape: Shape)
    abstract fun drawShape(shape: Shape)
    abstract fun fillRect(x: Float, y: Float, width: Float, height: Float)
    abstract fun drawRect(x: Float, y: Float, width: Float, height: Float)
    abstract fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float)
    abstract fun drawText(textImage: Image, x: Float, y: Float, width: Float, height: Float)
}