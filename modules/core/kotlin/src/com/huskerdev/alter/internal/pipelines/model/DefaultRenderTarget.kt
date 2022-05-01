package com.huskerdev.alter.internal.pipelines.model

import java.nio.ByteBuffer

abstract class DefaultRenderTarget <T: Number>(
    val width: Int,
    val height: Int,
    components: Int,
    samples: Int,
    data: ByteBuffer?
) {
    var contentChanged = true

    private val simpleTexture by lazy { createTexture(width, height, components) }
    private val simpleTextureSurface by lazy { getTextureRenderSurface(simpleTexture) }

    protected val renderSurface by lazy { createMSAARenderSurface(width, height, components, samples, data) }
    protected val renderTexture: T
        get() {
            if(contentChanged) {
                resolveMSAA(renderSurface, simpleTextureSurface)
                contentChanged = false
            }
            return simpleTexture
        }

    open fun dispose() {
        disposeTexture(simpleTexture)
        disposeRenderSurface(simpleTextureSurface)
        disposeRenderSurface(renderSurface)
    }

    protected abstract fun disposeTexture(texture: T)
    protected abstract fun disposeRenderSurface(surface: T)

    protected abstract fun createTexture(width: Int, height: Int, components: Int): T
    protected abstract fun getTextureRenderSurface(texture: T): T
    protected abstract fun createMSAARenderSurface(width: Int, height: Int, components: Int, samples: Int, data: ByteBuffer?): T
    protected abstract fun resolveMSAA(sourceSurface: T, targetSurface: T)
}