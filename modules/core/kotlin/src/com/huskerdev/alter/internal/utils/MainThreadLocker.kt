package com.huskerdev.alter.internal.utils

import com.huskerdev.alter.internal.Pipeline
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class MainThreadLocker {

    companion object {

        var queueNotifiers = arrayListOf<() -> Unit>()
        var tasksQueue = LinkedBlockingQueue<() -> Unit>()
        var disposed = false
        var mainThread: Thread? = null

        fun lock(){
            try {
                mainThread = Thread.currentThread()
                createDaemon()
                while (!disposed)
                    (tasksQueue.poll(150, TimeUnit.MILLISECONDS) ?: continue)()
            }catch (e: Exception){
                e.printStackTrace()
                exitProcess(1)
            }
        }

        fun invokeAsync(toInvoke: () -> Unit){
            if(mainThread == null)
                throw UnsupportedOperationException("Main thread is not locked")
            if(Thread.currentThread() == mainThread)
                toInvoke()
            else {
                tasksQueue.offer(toInvoke)
                queueNotifiers.forEach { it() }
            }
        }

        inline fun invoke(crossinline toInvoke: () -> Unit){
            if(mainThread == null)
                throw UnsupportedOperationException("Main thread is not locked")
            if(Thread.currentThread() == mainThread)
                toInvoke()
            else {
                val trigger = Trigger()
                tasksQueue.offer {
                    toInvoke()
                    trigger.ready()
                }
                queueNotifiers.forEach { it() }
                trigger.waitForReady()
            }
        }

        private fun createDaemon(){
            thread(name = "AlterUI Daemon Checker", isDaemon = true){
                while(!disposed) {
                    val allThreads = getAllThreads()
                        .filter { it != mainThread && !it.isDaemon }
                    if (allThreads.isEmpty() && Pipeline.windows.size == 0) {
                        disposed = true
                        queueNotifiers.forEach { it() }
                        break
                    }
                    Thread.sleep(200)
                }
            }
        }

        private fun getAllThreads(): Array<Thread>{
            // Get root thread
            var rootGroup = Thread.currentThread().threadGroup
            var parentGroup: ThreadGroup?
            while (rootGroup.parent.also { parentGroup = it } != null)
                rootGroup = parentGroup

            // Fill thread array
            var threads = arrayOfNulls<Thread>(rootGroup.activeCount())
            while (rootGroup.enumerate(threads, true) == threads.size)
                threads = arrayOfNulls(threads.size * 2)

            // Slice until first null
            return threads.sliceArray(0 until threads.indexOfFirst { it == null }).requireNoNulls()
        }
    }
}