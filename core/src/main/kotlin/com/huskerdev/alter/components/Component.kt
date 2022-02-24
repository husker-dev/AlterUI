package com.huskerdev.alter.components

abstract class Component {

    val x = 0.0
    val y = 0.0
    val width = 0.0
    val height = 0.0

    private val _children = arrayListOf<Component>()
    val children get() = _children.toArray()

    fun add(component: Component){
        _children.add(component)
    }
}