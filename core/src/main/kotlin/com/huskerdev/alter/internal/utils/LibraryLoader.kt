package com.huskerdev.alter.internal.utils

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.OS
import com.huskerdev.nioex.*
import java.io.*


class LibraryLoader {

    companion object {

        val alternativePaths = hashMapOf<String, String>()

        val tempFolder by lazy {
            val tmp = File("${System.getProperty("java.io.tmpdir")}Alter${File.separator}${AlterUI.version}")
            var folder: File? = null

            // Search for unused folder
            tmp.children.forEach {
                val lockFile = File(it, "lock")
                if (lockFile.exists() && !lockFile.isLocked()) {
                    folder = it
                    return@forEach
                }
            }
            // If bot found, create one
            if (folder == null)
                folder = File(tmp, System.nanoTime().toString())

            // Lock file
            val lockFile = File(folder, "lock")
            lockFile.parentFile.mkdirs()
            if (!lockFile.exists())
                lockFile.createNewFile()
            lockFile.lock()

            return@lazy folder
        }

        fun loadModuleLib(name: String) {
            val postfix = when(OS.current){
                OS.Windows -> ".dll"
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            load("com/huskerdev/alter/resources/${name}/${name}_${OS.arch.shortName}$postfix")
        }

        fun load(path: String) {
            val relativePath = if (path.startsWith("/")) path.substring(1) else path
            val dest = File(tempFolder, relativePath)
            dest.parentFile.mkdirs()

            if (path in alternativePaths)
                File(alternativePaths[path]!!).copyTo(dest)
            else if(!dest.exists())
                this::class.java.getResourceAsStream("/$relativePath")!!.copyTo(FileOutputStream(dest))
            System.load(dest.absolutePath)
        }

        private fun File.lock() = RandomAccessFile(this, "rw").channel.lock()

        private fun File.isLocked(): Boolean {
            try {
                FileInputStream(this).use {
                    it.read()
                    return false
                }
            } catch (e: FileNotFoundException) {
                return this.exists()
            } catch (ioe: IOException) {
                return true
            }
        }
    }
}