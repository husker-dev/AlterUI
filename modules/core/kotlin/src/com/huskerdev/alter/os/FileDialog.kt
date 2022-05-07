package com.huskerdev.alter.os

import com.huskerdev.alter.Frame
import com.huskerdev.alter.internal.Platform
import java.io.File


enum class FileDialogType {
    Open,
    Save
}

class FileDialog(
    var type: FileDialogType = FileDialogType.Open,
    var directory: File = File("."),
    var title: String = "",
    var onlyDirectories: Boolean = false,
    var multipleSelect: Boolean = false
) {

    companion object {
        @JvmStatic val ImageFilter = "Image Files" to "*.jpeg;*.jpg;*.png;*.tga;*.bmp;*.psd;*.gif;*.hdr;*.pic"
        @JvmStatic val AllFiles = "All Files" to "*.*"
    }

    val filters = arrayListOf<Pair<String, String>>()

    fun addFilters(vararg filters: Pair<String, String>){
        this.filters.addAll(filters)
    }

    fun show(frame: Frame? = null) = Platform.current.showFileDialog(frame?.peer, this)

}