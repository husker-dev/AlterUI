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

    override fun getColorPainter() = GLColorPainter
    override fun getImagePainter() = GLImagePainter
}

class ImageGLGraphics(
    val image: GLImage,
    private val resourceContext: GLResourceContext
): GLGraphics(
    image.renderTarget.framebuffer,
    resourceContext,
    true
) {
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun clear() = resourceContext.invokeOnResourceThread {
        super.clear()
        image.renderTarget.contentChanged = true
    }

    override fun fillShape(shape: Shape) = resourceContext.invokeOnResourceThread {
        super.fillShape(shape)
        image.renderTarget.contentChanged = true
    }

    override fun drawShape(shape: Shape) = resourceContext.invokeOnResourceThread {
        super.drawShape(shape)
        image.renderTarget.contentChanged = true
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.fillRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.drawRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = resourceContext.invokeOnResourceThread {
        super.drawImage(image, x, y, width, height)
        this@ImageGLGraphics.image.renderTarget.contentChanged = true
    }

    override fun drawText(text: String, x: Float, y: Float) = resourceContext.invokeOnResourceThread {
        super.drawText(text, x, y)
        image.renderTarget.contentChanged = true
    }

    override fun finish() = resourceContext.invokeOnResourceThread {
        context.glFlush()
    }
}

/*
class SurfaceImageGLGraphics(val image: GLImage): GLGraphics(image.renderTarget.framebuffer, image.context, true) {
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun finish() {}
}

 */

class WindowGLGraphics(val window: WindowPeer, context: GLContext): GLGraphics(0, context, false) {
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