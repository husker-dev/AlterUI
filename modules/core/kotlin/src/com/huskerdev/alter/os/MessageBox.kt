package com.huskerdev.alter.os

import com.huskerdev.alter.Frame
import com.huskerdev.alter.internal.Platform

enum class MessageBoxIcon{
    None,
    Error,
    Warning,
    Question,
    Information
}

enum class MessageBoxButton{
    Abort,
    Cancel,
    Continue,
    Ignore,
    No,
    OK,
    Retry,
    TryAgain,
    Yes
}

enum class MessageBoxType{
    AbortRetryIgnore,
    CancelTryContinue,
    OK,
    CancelOK,
    RetryCancel,
    YesNo,
    YesNoCancel
}

class MessageBox(
    var title: String,
    var message: String,
    var icon: MessageBoxIcon = MessageBoxIcon.Information,
    var type: MessageBoxType = MessageBoxType.OK
) {
    companion object {
        fun show(
             title: String,
             message: String,
             icon: MessageBoxIcon = MessageBoxIcon.Information,
             type: MessageBoxType = MessageBoxType.OK) =
            MessageBox(title, message, icon, type).show()

        fun show(
            frame: Frame,
            title: String,
            message: String,
            icon: MessageBoxIcon = MessageBoxIcon.Information,
            type: MessageBoxType = MessageBoxType.OK) =
            MessageBox(title, message, icon, type).show(frame)
    }

    fun show(context: Frame? = null) = Platform.current.showMessage(context?.peer, this)
}