package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nBeginScene
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nClear
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nEndScene
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetViewport
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ImagePainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9Painter

class D3D9Graphics(window: Window): Graphics() {

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

    override fun flush() {
        TODO("Not yet implemented")
    }

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

    override fun getColorPainter() = null!!
    override fun getImagePainter() = null!!


}