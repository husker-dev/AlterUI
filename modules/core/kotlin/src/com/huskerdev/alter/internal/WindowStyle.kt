package com.huskerdev.alter.internal

abstract class WindowStyle private constructor(){
    companion object {
        protected const val defaultIndex      = 0
        protected const val undecoratedIndex  = 1
        protected const val noTitleIndex      = 2

        val Default by lazy { DefaultImpl() }
        val Undecorated by lazy { UndecoratedImpl() }
        val NoTitle by lazy { NoTitleImpl(false) { x, y -> return@NoTitleImpl false } }
        val NoTitleResizable by lazy { NoTitleImpl(true) { x, y -> return@NoTitleImpl false } }
    }

    abstract val styleIndex: Int
    abstract fun hitTest(window: WindowPeer, x: Int, y: Int): WindowHitPosition

    class DefaultImpl: WindowStyle(){
        override val styleIndex = defaultIndex
        override fun hitTest(window: WindowPeer, x: Int, y: Int) = WindowHitPosition.Default
    }

    class UndecoratedImpl: WindowStyle(){
        override val styleIndex = undecoratedIndex
        override fun hitTest(window: WindowPeer, x: Int, y: Int) = WindowHitPosition.Caption
    }

    class NoTitleImpl(private val resizable: Boolean, private val borderWidth: Int = 10, private val dragTest: (x: Int, y: Int) -> Boolean): WindowStyle(){
        override val styleIndex = noTitleIndex

        override fun hitTest(window: WindowPeer, x: Int, y: Int): WindowHitPosition {
            if(!resizable)
                return WindowHitPosition.Client
            val border = borderWidth * window.dpi

            if(x < border){
                return if(y < border) WindowHitPosition.TopLeft
                else if(y > window.physicalHeight - border) WindowHitPosition.BottomLeft
                else WindowHitPosition.Left
            }
            if(x > window.physicalWidth - border){
                return if(y < border) WindowHitPosition.TopRight
                else if(y > window.physicalHeight - border) WindowHitPosition.BottomRight
                else WindowHitPosition.Right
            }
            if(y > window.physicalHeight - border)
                return WindowHitPosition.Bottom
            if(y < 3)
                return WindowHitPosition.Top
            if(dragTest(x, y))
                return WindowHitPosition.Caption
            return WindowHitPosition.Client
        }
    }
}