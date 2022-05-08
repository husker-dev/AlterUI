package com.huskerdev.alter.components

import com.huskerdev.alter.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.Pipeline

abstract class Component {

    companion object {
        protected const val FLAG_UPDATE        = 0b00000001
        protected const val FLAG_REPAINT       = 0b00000010

        protected fun componentNeedsUpdate(component: Component){
            var parent: Component? = component
            while(parent != null){
                parent.flags = parent.flags or FLAG_UPDATE
                parent = parent.parent
            }
        }

        protected fun componentNeedsRepaint(component: Component){
            var parent: Component? = component
            while(parent != null){
                parent.flags = parent.flags or FLAG_REPAINT
                parent = parent.parent
            }
        }
    }

    var x = 0f
        set(value) {
            field = value
            if(parent != null)
                componentNeedsRepaint(parent!!)
        }
    var y = 0f
        set(value) {
            field = value
            if(parent != null)
                componentNeedsRepaint(parent!!)
        }
    var width = 0f
       set(value) {
           if(field != value) {
               field = value
               componentNeedsRepaint(this)
               onResizeListeners.forEach { it() }
           }
       }
    var height = 0f
        set(value) {
            if(field != value) {
                field = value
                componentNeedsRepaint(this)
                onResizeListeners.forEach { it() }
            }
        }

    var preferredWidth = 0f
        set(value) {
            if(field != value){
                field = value
                componentNeedsUpdate(this)
            }
        }

    var preferredHeight = 0f
        set(value) {
            if(field != value){
                field = value
                componentNeedsUpdate(this)
            }
        }

    private var flags = 0
    private var texture: Image? = null

    private var onResizeListeners = arrayListOf<() -> Unit>()

    var parent: Component? = null
    var frame: Frame? = null
        set(value) {
            field = value
            for(child in children)
                child.frame = value
            componentNeedsUpdate(this)
            componentNeedsRepaint(this)
        }

    val children = object: ChildrenList(){
        override fun onAdd(component: Component) {
            component.parent = this@Component
            componentNeedsUpdate(component)
        }

        override fun onRemove(component: Component) {

        }
    }

    fun onResized(event: () -> Unit) = onResizeListeners.add(event)

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

    internal fun dpiChanged(){
        componentNeedsRepaint(this)
        for(child in children)
            child.dpiChanged()
    }

    internal fun paint(gr: Graphics) {
        if(frame == null)
            return
        if(checkFlag(FLAG_REPAINT)) {
            if (width <= 0 || height <= 0)
                return
            texture?.dispose()

            texture = Pipeline.current.createSurfaceImage(
                frame!!.peer,
                PixelType.RGBA,
                (width * frame!!.dpi).toInt(), (height * frame!!.dpi).toInt(),
                width.toInt(), height.toInt(),
                frame!!.dpi,
                true
            )
            texture!!.linearFiltered = false

            val contentGraphics = texture!!.graphics
            paintComponent(contentGraphics)
            for (child in children)
                child.paint(contentGraphics)
        }
        gr.color = Color.white
        gr.drawImage(texture!!, x, y, width, height)
    }

    fun repaint(){
        componentNeedsRepaint(this)
        frame?.repaint()
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