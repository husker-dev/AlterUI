package com.huskerdev.alter

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.internal.Platform

class Mouse {

    companion object {
        val position : Point
            get() = Platform.current.physicalMousePosition
    }
}