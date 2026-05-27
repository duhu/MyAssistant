package com.myassistant.app.data.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val time: String,
    val level: String,  // INFO, SUCCESS, ERROR
    val message: String
)

object DiagnosticLog {
    private val _events = MutableSharedFlow<LogEntry>(replay = 50, extraBufferCapacity = 10)
    val events: SharedFlow<LogEntry> = _events.asSharedFlow()

    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun i(msg: String) = emit("INFO", msg)
    fun ok(msg: String) = emit("SUCCESS", msg)
    fun e(msg: String) = emit("ERROR", msg)

    private fun emit(level: String, msg: String) {
        _events.tryEmit(LogEntry(fmt.format(Date()), level, msg))
    }
}
