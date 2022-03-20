package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nGetWindowSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nPresent
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ImagePainter

abstract class D3D9Graphics(open val surface: Long): Graphics() {

    /*
    private val colorPainterInstance by lazy { D3D9ColorPainter() }
    private val imagePainterInstance by lazy { D3D9ImagePainter() }

    private var oldWidth = -1
    private var oldHeight = -1

    override fun beginImpl() {
        if(oldWidth != window.clientWidth || oldHeight != window.clientHeight) {
            oldWidth = window.clientWidth
            oldHeight = window.clientHeight
            nSetViewport(window.handle, oldWidth, oldHeight)
        }
        nBeginScene(window.handle)
    }

    override fun endImpl() {
        nEndScene(window.handle)
    }

    override fun updateTransforms() {
        //if(painter is D3D9Painter)
        //    (painter as D3D9Painter).updateMatrix(matrix)
    }

     */


    /*
    override var painter: Painter?
        get() = super.painter
        set(value) {
            super.painter = value
            if(value is D3D9Painter) {
                //value.updateMatrix(matrix)
                value.updateHeight(oldHeight.toFloat())
                value.updateDpi(dpi)
            }
        }
*/
    //override fun clear() = nClear()

    override fun getColorPainter() = D3D9ColorPainter
    override fun getImagePainter() = D3D9ImagePainter

    override fun finish() {}
}

class D3D9ImageGraphics(val image: Image): D3D9Graphics((image as D3D9Image).surface){
    override val width = image.width.toFloat()
    override val height = image.height.toFloat()
    override val physicalHeight = image.height
    override val physicalWidth = image.width
    override val dpi = 1f
    override val pixelType = image.pixelType
}

class D3D9WindowGraphics(val window: Window): D3D9Graphics(0){
    override val width: Float
        get() = window.width
    override val height: Float
        get() = window.height
    override val physicalHeight: Int
        get() = window.physicalHeight
    override val physicalWidth: Int
        get() = window.physicalWidth
    override val dpi: Float
        get() = window.dpi
    override val pixelType = PixelType.RGBA

    override val surface: Long
        get() = nGetWindowSurface(window.handle)

    override fun finish() = nPresent(window.handle)
}