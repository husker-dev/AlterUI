package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_COLOR_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_DEPTH_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glClear
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glFlush
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nMakeCurrent
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

    override fun flush() = context.flush()
}

class ImageGLGraphics(val image: GLImage): GLGraphics(image.framebuffer, resourcesContext) {
    override val width: Float
        get() = image.width.toFloat()
    override val height: Float
        get() = image.height.toFloat()
    override val physicalHeight: Int
        get() = image.height
    override val physicalWidth: Int
        get() = image.width
    override val dpi: Float
        get() = 1f
}

class WindowGLGraphics(val window: Window): GLGraphics(0, GLContext(window.handle)) {
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

    override fun flush() {
        nSwapBuffers(window.handle)
    }
}