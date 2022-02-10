package com.huskerdev.alter.internal.utils

import java.util.*

class Trigger {

    companion object {
        fun waitForInvoke(queue: AbstractQueue<() -> Unit>, action: () -> Unit){
            val trigger = Trigger()
            queue.offer {
                action()
                trigger.ready()
            }
            trigger.waitForReady()
        }

        fun waitForInvoke(queue: AbstractQueue<Runnable>, action: Runnable){
            val trigger = Trigger()
            queue.offer {
                action.run()
                trigger.ready()
            }
            trigger.waitForReady()
        }
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