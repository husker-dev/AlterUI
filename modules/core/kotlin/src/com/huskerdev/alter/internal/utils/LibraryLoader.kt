package com.huskerdev.alter.internal.utils

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.OS
import java.io.*
import java.lang.NullPointerException


class LibraryLoader {
    companion object {
        var forceWrite = false

        val tempFolder = File("${System.getProperty("java.io.tmpdir")}Alter${File.separator}${AlterUI.version}")

        fun loadModuleLib(name: String) {
            val postfix = when(OS.current){
                OS.Windows -> ".dll"
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            load("com/huskerdev/alter/resources/${name}/${name}_${OS.arch.shortName}$postfix")
        }

        fun load(path: String) {
            val relativePath = if (path.startsWith("/")) path.substring(1) else path
            val dest = File(tempFolder, relativePath.substring(relativePath.lastIndexOf("/")))
            dest.parentFile.mkdirs()

            if(forceWrite || !dest.exists()) {
                val input = this::class.java.getResourceAsStream("/$relativePath")
                    ?: throw NullPointerException("Can't find library in resources: /$relativePath")
                val output = FileOutputStream(dest)
                input.copyTo(output)
                input.close()
                output.close()
            }
            System.load(dest.absolutePath)
        }
    }
}