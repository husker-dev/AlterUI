package com.huskerdev.alter.internal.utils

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.OS
import java.io.*
import java.lang.NullPointerException


class LibraryLoader {
    companion object {
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

            if(AlterUIProperties.forceLibraryCaching || !dest.exists()) {
                try {
                    this::class.java.getResourceAsStream("/$relativePath").use {
                        if (it == null)
                            throw NullPointerException("Can't find library in resources: /$relativePath")

                        val output = FileOutputStream(dest)
                        it.copyTo(output)
                        it.close()
                        output.close()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            System.load(dest.absolutePath)
        }
    }
}