package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType

abstract class Component {

    companion object {
        protected const val FLAG_SIZE_CHANGED = 0x00000001
    }

    val x = 0f
    val y = 0f
    val width = 1f
    val height = 1f

    private var flags = FLAG_SIZE_CHANGED
    private var texture: Image? = null
    val children = arrayListOf<Component>()

    protected fun addFlag(flag: Int){
        flags = flags or flag
    }

    private fun checkFlag(flag: Int): Boolean{
        val result = (flags and flag == flag)
        flags = flags xor flag
        return result
    }

    fun paint(gr: Graphics) {
        if(checkFlag(FLAG_SIZE_CHANGED)){
            doLayout()
            if(width > 0 && height > 0)
                texture = Image.createEmpty(width.toInt(), height.toInt(), PixelType.RGBA)
            texture = null
        }
    }

    abstract fun doLayout()
}