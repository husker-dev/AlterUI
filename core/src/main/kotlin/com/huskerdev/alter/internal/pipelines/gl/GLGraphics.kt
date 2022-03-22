package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSwapBuffers
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.resourcesContext
import com.huskerdev.alter.internal.pipelines.gl.painters.GLColorPainter
import com.huskerdev.alter.internal.pipelines.gl.painters.GLImagePainter

abstract class GLGraphics(
    val framebuffer: Int,
    var context: GLContext
): Graphics() {

    override fun getColorPainter() = GLColorPainter
    override fun getImagePainter() = GLImagePainter

    override fun finish() = context.flush()
}

class ImageGLGraphics(val image: GLImage): GLGraphics(image.framebuffer, resourcesContext) {
    override val width = image.width.toFloat()
    override val height = image.height.toFloat()
    override val physicalHeight = image.height
    override val physicalWidth = image.width
    override val dpi = 1f
    override val pixelType = image.pixelType
}

class WindowGLGraphics(val window: WindowPeer): GLGraphics(0, GLContext(window.handle)) {
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
        context.finish()
    }
}