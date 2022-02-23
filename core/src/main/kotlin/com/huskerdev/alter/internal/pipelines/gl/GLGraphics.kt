package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_COLOR_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.GL_DEPTH_BUFFER_BIT
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glClear
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glClearColor
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.glViewport
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nInitContext
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nMakeCurrent
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.nSwapBuffers
import com.huskerdev.alter.internal.pipelines.gl.painters.GLColorPainter
import com.huskerdev.alter.internal.pipelines.gl.painters.GLImagePainter
import com.huskerdev.alter.internal.pipelines.gl.painters.GLPainter

class GLGraphics(window: Window): Graphics(window) {

    private var initialized = false

    override fun beginImpl() {
        nMakeCurrent(window.handle)
        glViewport(0, 0, window.width.toInt(), window.height.toInt())
        if(!initialized){
            initialized = true
            nInitContext()
        }
    }

    override fun endImpl() {
        nSwapBuffers(window.handle)
    }

    override fun updateMatrix() {
        (painter as GLPainter).updateMatrix(matrix)
    }

    override fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0f, 0f, 0f, 1f)
    }

    override fun setPainter(painter: Painter) {
        super.setPainter(painter)
        (painter as GLPainter).updateMatrix(matrix)
    }

    override fun getColorPainter() = GLColorPainter
    override fun getImagePainter() = GLImagePainter
}