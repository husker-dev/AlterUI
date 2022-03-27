package com.huskerdev.alter.internal.utils

import java.util.*

class Trigger {

    companion object {

    }

    private var ready = false
    private val notifier = Object()

    fun ready(){
        ready = true
        synchronized(notifier) { notifier.notifyAll() }
    }

    fun waitForReady(){
        if(!ready)
            synchronized(notifier) { notifier.wait() }
    }

    fun reset(): Trigger {
        ready = false
        return this
    }
}