package com.huskerdev.alter.graphics

interface Painter {
    fun enable()
    fun disable()

    fun checkChanges()

    fun fillRect(x: Float, y: Float, width: Float, height: Float)
    fun drawRect(x: Float, y: Float, width: Float, height: Float)
}