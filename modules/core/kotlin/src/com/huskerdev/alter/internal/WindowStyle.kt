package com.huskerdev.alter.internal

abstract class WindowStyle private constructor(){
    companion object {
        protected const val defaultIndex      = 0
        protected const val undecoratedIndex  = 1
        protected const val noTitleIndex      = 2

        val Default by lazy { DefaultImpl() }
        val Undecorated by lazy { UndecoratedImpl() }
        val NoTitle by lazy { NoTitleImpl() }
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

    class NoTitleImpl: WindowStyle(){
        override val styleIndex = noTitleIndex
        override fun hitTest(window: WindowPeer, x: Int, y: Int) = WindowHitPosition.Caption
    }
}