package io.github.crow_misia.mediasoup

import android.util.Log

object DefaultLogHandler : LogHandler {
    override fun log(priority: Int, tag: String?, t: Throwable?, message: String?, vararg args: Any?) {
        var msg = message
        msg = if (msg.isNullOrEmpty()) {
            if (t == null) {
                return
            }
            Log.getStackTraceString(t)
        } else {
            val stackTrace = Log.getStackTraceString(t)
            if (stackTrace.isEmpty()) {
                msg.format(*args)
            } else {
                "${msg.format(*args)}\n$stackTrace"
            }
        }
        Log.println(priority, tag, msg)
    }
}
