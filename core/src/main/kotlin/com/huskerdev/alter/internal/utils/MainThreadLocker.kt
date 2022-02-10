package com.huskerdev.alter.internal.utils

import com.huskerdev.alter.internal.Pipeline
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainThreadLocker {

    companion object {

        var queueNotifiers = arrayListOf<() -> Unit>()
        var tasksQueue = LinkedBlockingQueue<Runnable>()
        var disposed = false
        private var mainThread: Thread? = null

        fun lock(){
            mainThread = Thread.currentThread()
            createDaemon()
            while(!disposed)
                (tasksQueue.poll(150, TimeUnit.MILLISECONDS) ?: continue).run()
        }

        fun invokeAsync(toInvoke: Runnable){
            if(mainThread == null)
                throw UnsupportedOperationException("Main thread is not locked")
            if(Thread.currentThread() == mainThread)
                toInvoke.run()
            else {
                tasksQueue.offer(toInvoke)
                queueNotifiers.forEach { it() }
            }
        }

        fun invoke(toInvoke: Runnable){
            if(mainThread == null)
                throw UnsupportedOperationException("Main thread is not locked")
            if(Thread.currentThread() == mainThread)
                toInvoke.run()
            else {
                val trigger = Trigger()
                tasksQueue.offer {
                    toInvoke.run()
                    trigger.ready()
                }
                queueNotifiers.forEach { it() }
                trigger.waitForReady()
            }
        }

        private fun createDaemon(){
            thread(name = "MinUI Daemon Checker", isDaemon = true){
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