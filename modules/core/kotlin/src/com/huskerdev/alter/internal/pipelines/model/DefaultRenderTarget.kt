package com.huskerdev.alter.internal.pipelines.model

import java.nio.ByteBuffer

abstract class DefaultRenderTarget <T: Number>(
    val width: Int,
    val height: Int,
    val components: Int,
    val samples: Int,
    val data: ByteBuffer?
) {
    var contentChanged = true

    lateinit var simpleTexture: T
    lateinit var simpleTextureSurface: T

    protected lateinit var renderSurface: T
    protected val renderTexture: T
        get() {
            resolveContent()
            return simpleTexture
        }

    fun resolveContent(){
        if(contentChanged) {
            resolveMSAA(renderSurface, simpleTextureSurface)
            contentChanged = false
        }
    }

    fun loadTarget(){
        renderSurface = createMSAARenderSurface(width, height, components, samples, data)

        simpleTexture = createTexture(width, height, components)
        simpleTextureSurface = getTextureRenderSurface(simpleTexture)
        resolveContent()
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