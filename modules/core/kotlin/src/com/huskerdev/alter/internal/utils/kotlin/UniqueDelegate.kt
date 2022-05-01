package com.huskerdev.alter.internal.utils.kotlin

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> unique(initial: T, onUpdate: (T) -> Unit) = UniqueDelegate(initial, onUpdate)

class UniqueDelegate<T>(initial: T, private val onUpdate: (T) -> Unit): ReadWriteProperty<Any?, T> {
    var value = initial

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if(value != this.value){
            this.value = value
            onUpdate(this.value)
        }
    }
}