package com.huskerdev.alter.graphics

interface Painter {
    fun enable()
    fun disable()

    fun fillRect(x: Float, y: Float, width: Float, height: Float)
}