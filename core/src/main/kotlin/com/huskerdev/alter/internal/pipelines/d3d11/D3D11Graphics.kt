package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.d3d11.painters.D3D11ColorPainter

class D3D11Graphics(window: Window): Graphics() {

    private val colorPainterInstance = D3D11ColorPainter()





    override val width: Float
        get() = TODO("Not yet implemented")
    override val height: Float
        get() = TODO("Not yet implemented")
    override val physicalHeight: Int
        get() = TODO("Not yet implemented")
    override val physicalWidth: Int
        get() = TODO("Not yet implemented")
    override val dpi: Float
        get() = TODO("Not yet implemented")
    override val pixelType: PixelType
        get() = TODO("Not yet implemented")

    override fun finish() {
        TODO("Not yet implemented")
    }

    /*
    override fun clear() {
        D3D11Pipeline.nClear(window.handle)
    }

     */


    override fun getColorPainter() = colorPainterInstance

    override fun getImagePainter(): ImagePainter {
        TODO("Not yet implemented")
    }

}