package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.invokeOnResourceThread
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSwapBuffers
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.resourcesContext
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

class ImageGLGraphics(val image: GLImage): GLGraphics(image.framebuffer, resourcesContext, true) {
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun reset() = invokeOnResourceThread {
        super.reset()
    }

    override fun clear() = invokeOnResourceThread {
        super.clear()
    }

    override fun fillShape(shape: Shape) = invokeOnResourceThread {
        super.fillShape(shape)
    }

    override fun drawShape(shape: Shape) = invokeOnResourceThread {
        super.drawShape(shape)
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) = invokeOnResourceThread {
        super.fillRect(x, y, width, height)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) = invokeOnResourceThread {
        super.drawRect(x, y, width, height)
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) = invokeOnResourceThread {
        super.drawImage(image, x, y, width, height)
    }

    override fun drawText(text: String, x: Float, y: Float) = invokeOnResourceThread {
        super.drawText(text, x, y)
    }

    override fun finish() = invokeOnResourceThread {
        context.flush()
    }
}

class SurfaceImageGLGraphics(val image: GLImage): GLGraphics(image.framebuffer, image.context, true) {
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun finish() {}
}

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