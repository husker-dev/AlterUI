package com.huskerdev.alter.internal.pipelines.gl

import com.huskerdev.alter.internal.pipelines.model.DefaultRenderTarget
import java.nio.ByteBuffer

class GLRenderTarget(
    width: Int,
    height: Int,
    components: Int,
    samples: Int,
    data: ByteBuffer?,
    val context: GLContext
): DefaultRenderTarget<Int>(width, height, components, samples, data) {

    private var msaaTexture = 0
    val framebuffer by ::renderSurface
    val texture by ::renderTexture

    override fun disposeTexture(texture: Int) =
        context.glDeleteTexture(texture)

    override fun disposeRenderSurface(surface: Int) =
        context.glDeleteFramebuffer(surface)

    override fun createTexture(width: Int, height: Int, components: Int) =
        context.createTexture(width, height, components)

    override fun getTextureRenderSurface(texture: Int) =
        context.createTextureFramebuffer(texture, false)

    override fun createMSAARenderSurface(
        width: Int,
        height: Int,
        components: Int,
        samples: Int,
        data: ByteBuffer?
    ): Int {
        msaaTexture = context.createMSAATexture(width, height, components, samples, data)
        return context.createTextureFramebuffer(msaaTexture, true)
    }

    override fun resolveMSAA(sourceSurface: Int, targetSurface: Int) =
        context.glBlitFramebuffer(sourceSurface, targetSurface, width, height)

    override fun dispose() {
        super.dispose()
        disposeTexture(msaaTexture)
    }
}