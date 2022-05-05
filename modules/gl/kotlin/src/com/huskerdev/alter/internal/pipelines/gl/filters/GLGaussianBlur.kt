package com.huskerdev.alter.internal.pipelines.gl.filters

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.pipelines.gl.GLImage
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline.Companion.resourceContext
import com.huskerdev.alter.internal.pipelines.gl.GLShader

class GLGaussianBlur(radius: Int): GaussianBlur(radius) {

    companion object {
         val shader = GLShader.fromResources(
             "/com/huskerdev/alter/resources/gl/shaders/defaultVertex.glsl",
             "/com/huskerdev/alter/resources/gl/shaders/gaussian/gaussianBlurFragment.glsl"
         ).compile(resourceContext).apply {
             defineTextureVariable(resourceContext, "u_Texture", 0)
         }
    }

    override fun processImpl(input: Image): Image {
        input as GLImage
        var newImage: GLImage? = null

        resourceContext.invokeOnResourceThread {
            newImage = Image.createEmpty(input.width, input.height, input.pixelType) as GLImage

            val tmpTexture = resourceContext.createTexture(input.width, input.height, input.pixelType.channels)
            val tmpFramebuffer = resourceContext.createTextureFramebuffer(tmpTexture, false)

            resourceContext.shader = shader

            val sizeVariable = shader.getVariableLocation(resourceContext, "u_size")
            val radiusVariable = shader.getVariableLocation(resourceContext, "u_radius")
            val typeVariable = shader.getVariableLocation(resourceContext, "u_type")
            shader.set(resourceContext, sizeVariable, 0f, 0f, input.width.toFloat(), input.height.toFloat())
            shader[resourceContext, radiusVariable] = radius.toFloat()
            resourceContext.glViewport(input.width, input.height)

            // Horizontal
            shader[resourceContext, typeVariable] = 0f
            resourceContext.glBindTexture(0, input.renderTarget.texture)
            resourceContext.framebuffer = tmpFramebuffer
            VertexHelper.fillRect(-1f, -1f, 2f, 2f, resourceContext::drawArray)

            // Vertical
            shader[resourceContext, typeVariable] = 1f
            resourceContext.glBindTexture(0, tmpTexture)
            resourceContext.framebuffer = newImage!!.renderTarget.framebuffer
            VertexHelper.fillRect(-1f, -1f, 2f, 2f, resourceContext::drawArray)

            newImage!!.renderTarget.contentChanged = true
            resourceContext.glDeleteTexture(tmpTexture)
            resourceContext.glDeleteFramebuffer(tmpFramebuffer)

            resourceContext.glFinish()
        }

        return newImage!!
    }
}