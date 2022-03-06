package io.github.crow_misia.mediasoup

interface LogHandler {
    fun log(priority: Int, tag: String?, t: Throwable?, message: String?, vararg args: Any?) { }
}
