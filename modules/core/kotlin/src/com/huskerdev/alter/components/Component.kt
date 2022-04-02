package com.huskerdev.alter.components

import com.huskerdev.alter.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType

abstract class Component {

    companion object {
        protected const val FLAG_UPDATE        = 0b00000001
        protected const val FLAG_REPAINT       = 0b00000010
    }

    var x = 0f
    var y = 0f
    var width = 0f
       set(value) {
           if(field != value) {
               field = value
               componentUpdated()
           }
       }
    var height = 0f
        set(value) {
            if(field != value) {
                field = value
                componentUpdated()
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

    private var flags = 0
    private var texture: Image? = null

    var parent: Component? = null
    var frame: Frame? = null
        set(value) {
            field = value
            componentUpdated()
        }

    val children = object: ChildrenList(){
        override fun onAdd(component: Component) {
            component.parent = this@Component
            component.componentUpdated()
        }

        override fun onRemove(component: Component) {

        }
    }

    protected fun componentUpdated(){
        var parent: Component? = this
        while(parent != null){
            parent.addFlag(FLAG_UPDATE)
            parent = parent.parent
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

    internal fun update(){
        if(checkFlag(FLAG_UPDATE)){
            for(child in children)
                child.update()

            doLayout()
        }
    }

    internal fun paint(gr: Graphics) {
        if(width <= 0 || height <= 0)
            return
        if(texture == null ||
            width.toInt() != texture!!.width ||
            height.toInt() != texture!!.height
        ){
            texture?.dispose()
            texture = Image.createEmpty(width.toInt(), height.toInt(), PixelType.RGBA)

            val contentGraphics = texture!!.graphics
            paintComponent(contentGraphics)
            for(child in children)
                child.paint(contentGraphics)
        }
        gr.color = Color.white
        gr.drawImage(texture!!, x, y, width, height)
    }

    fun repaint(){

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