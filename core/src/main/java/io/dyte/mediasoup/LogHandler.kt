package io.dyte.mediasoup

interface LogHandler {
    fun log(priority: Int, tag: String?, t: Throwable?, message: String?, vararg args: Any?) { }
}
