package com.huskerdev.alter.internal.utils

import sun.misc.Unsafe

class UnsafeUtils {

    val instance = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

    init {
        //instance.
    }
}