package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType

abstract class Component {

    companion object {
        protected const val FLAG_SIZE_CHANGED = 0x00000001
        protected const val FLAG_CONTENT_CHANGED = 0x00000002
    }

    var x = 0f
    var y = 0f
    var width = 0f
       set(value) {
           if(field != value) {
               field = value
               addFlag(FLAG_SIZE_CHANGED or FLAG_CONTENT_CHANGED)
               doLayout()
           }
       }
    var height = 0f
        set(value) {
            if(field != value) {
                field = value
                addFlag(FLAG_SIZE_CHANGED or FLAG_CONTENT_CHANGED)
                doLayout()
            }
        }

    var preferredWidth = 0f
        set(value) {
            if(field != value){
                field = value
                parent?.doLayout()
            }
        }

    var preferredHeight = 0f
        set(value) {
            if(field != value){
                field = value
                parent?.doLayout()
            }
        }

    private var flags = FLAG_SIZE_CHANGED
    private var texture: Image? = null

    var parent: Component? = null
    val children = object: ChildrenList(){
        override fun onAdd(component: Component) {
            component.parent = this@Component
            doLayout()
        }

        override fun onRemove(component: Component) {

        }
    }

    protected fun addFlag(flag: Int){
        flags = flags or flag
    }

    private fun checkFlag(flag: Int): Boolean{
        val result = (flags and flag == flag)
        if(result)
            flags = flag xor flags
        return result
    }

    fun paint(gr: Graphics) {
        if(checkFlag(FLAG_SIZE_CHANGED)){
            texture?.dispose()
            texture = if(width > 0 && height > 0)
                Image.createEmpty(width.toInt(), height.toInt(), PixelType.RGBA)
            else null
        }
        if(checkFlag(FLAG_CONTENT_CHANGED) && texture != null) {
            paintComponent(texture!!.graphics)
            for(child in children)
                child.paint(gr)
        }

        if(texture != null)
            gr.drawImage(texture!!, x, y, width, height)
    }

    abstract fun paintComponent(gr: Graphics)

    abstract fun doLayout()

    abstract class ChildrenList: ArrayList<Component>(){

        abstract fun onAdd(component: Component)
        abstract fun onRemove(component: Component)

        override fun add(element: Component): Boolean {
            val result = super.add(element)
            onAdd(element)
            return result
        }

        override fun add(index: Int, element: Component) {
            super.add(index, element)
            onAdd(element)
        }

        override fun addAll(elements: Collection<Component>): Boolean {
            val result = super.addAll(elements)
            for(element in elements)
                onAdd(element)
            return result
        }

        override fun addAll(index: Int, elements: Collection<Component>): Boolean {
            val result = super.addAll(index, elements)
            for(element in elements)
                onAdd(element)
            return result
        }
    }
}