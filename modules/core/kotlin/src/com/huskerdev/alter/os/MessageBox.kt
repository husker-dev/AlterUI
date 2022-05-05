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

class MessageBoxButtonsBuilder(private val messageBox: MessageBox){

    fun onAbort(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Abort, runnable)
    fun onCancel(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Cancel, runnable)
    fun onContinue(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Continue, runnable)
    fun onIgnore(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Ignore, runnable)
    fun onNo(runnable: Runnable) = messageBox.onClick(MessageBoxButton.No, runnable)
    fun onOk(runnable: Runnable) = messageBox.onClick(MessageBoxButton.OK, runnable)
    fun onRetry(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Retry, runnable)
    fun onTryAgain(runnable: Runnable) = messageBox.onClick(MessageBoxButton.TryAgain, runnable)
    fun onYes(runnable: Runnable) = messageBox.onClick(MessageBoxButton.Yes, runnable)
}

class MessageBox(
    var title: String,
    var message: String,
    var icon: MessageBoxIcon = MessageBoxIcon.Information,
    var type: MessageBoxType = MessageBoxType.OK
) {

    val listeners = hashMapOf<MessageBoxButton, Runnable>()

    constructor(
        title: String,
        message: String,
        icon: MessageBoxIcon = MessageBoxIcon.Information,
        type: MessageBoxType = MessageBoxType.OK,
        buttonInit: MessageBoxButtonsBuilder.() -> Unit
    ) : this(title, message, icon, type) {
        buttonInit(MessageBoxButtonsBuilder(this))
    }

    fun onClick(button: MessageBoxButton, runnable: Runnable){
        listeners[button] = runnable
    }

    fun show(context: Frame? = null){
        val result = Platform.current.showMessage(context?.peer, this)
        listeners[result]?.run()
    }

}