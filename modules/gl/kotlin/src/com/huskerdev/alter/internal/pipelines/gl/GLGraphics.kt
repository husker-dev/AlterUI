package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSwapBuffers
import com.huskerdev.alter.internal.pipelines.gl.painters.GLColorPainter
import com.huskerdev.alter.internal.pipelines.gl.painters.GLImagePainter

abstract class GLGraphics(
    val framebuffer: Int,
    var context: GLContext,
    val inverseY: Boolean
): Graphics() {

    override fun clear() {
        synchronized(GLPipeline.contexts){
            super.clear()
        }
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(GLPipeline.contexts) {
            super.fillRect(x, y, width, height)
        }
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(GLPipeline.contexts) {
            super.drawRect(x, y, width, height)
        }
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        synchronized(GLPipeline.contexts) {
            super.drawImage(image, x, y, width, height)
        }
    }

    override fun drawText(text: String, x: Float, y: Float) {
        synchronized(GLPipeline.contexts) {
            super.drawText(text, x, y)
        }
    }

    override fun fillShape(shape: Shape) {
        synchronized(GLPipeline.contexts) {
            super.fillShape(shape)
        }
    }

    override fun drawShape(shape: Shape) {
        synchronized(GLPipeline.contexts) {
            super.drawShape(shape)
        }
    }

    override fun getColorPainter() = GLColorPainter
    override fun getImagePainter() = GLImagePainter
}

open class ImageGLGraphics(val image: GLImage): GLGraphics(image.renderTarget.framebuffer, image.context, true) {
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun clear() {
        super.clear()
        image.renderTarget.contentChanged = true
    }

    override fun fillShape(shape: Shape) {
        super.fillShape(shape)
        image.renderTarget.contentChanged = true
    }

    override fun drawShape(shape: Shape) {
        super.drawShape(shape)
        image.renderTarget.contentChanged = true
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        super.fillRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        super.drawRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        super.drawImage(image, x, y, width, height)
        this@ImageGLGraphics.image.renderTarget.contentChanged = true
    }

    override fun drawText(text: String, x: Float, y: Float) {
        super.drawText(text, x, y)
        image.renderTarget.contentChanged = true
    }

    override fun finish() {}
}

class ResourceImageGLGraphics(
    image: GLImage,
    private val resourceContext: GLResourceContext
): ImageGLGraphics(image) {

    override fun clear() = resourceContext.invokeOnResourceThread {
        super.clear()
    }

    override fun fillShape(shape: Shape) = resourceContext.invokeOnResourceThread {
        super.fillShape(shape)
    }

    override fun drawShape(shape: Shape) = resourceContext.invokeOnResourceThread {
        super.drawShape(shape)
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.fillRect(x, y, width, height)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.drawRect(x, y, width, height)
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.drawImage(image, x, y, width, height)
    }

    override fun drawText(text: String, x: Float, y: Float) = resourceContext.invokeOnResourceThread {
        super.drawText(text, x, y)
    }

    override fun finish() = context.glFinish()
}


class WindowGLGraphics(private val window: WindowPeer, context: GLContext): GLGraphics(0, context, false) {
    override val width: Float
        get() = window.width
    override val height: Float
        get() = window.height
    override val physicalHeight: Int
        get() = window.clientHeight
    override val physicalWidth: Int
        get() = window.clientWidth
    override val dpi: Float
        get() = window.dpi
    override val pixelType: PixelType
        get() = PixelType.RGBA

    override fun finish() {
        nSwapBuffers(window.handle)
    }
}