package com.atriidev.warp_runtime.log

import com.diamondedge.logging.KmLog
import com.diamondedge.logging.KmLogging
import com.diamondedge.logging.LogLevel
import kotlin.jvm.JvmName

enum class WarpLoggerLevel {
    Verbose,
    Debug,
    Info,
    Warn,
    Error,
    Off,
}

object WarpLogger {
    private var currentLevel: WarpLoggerLevel = WarpLoggerLevel.Info

    /**
     * Expose log level configuration to developers.
     * Example: `WarpLogger.Level = WarpLoggerLevel.Info`
     */
    var Level: WarpLoggerLevel
        get() = currentLevel
        set(value) {
            currentLevel = value
            applyLevel(value)
        }

    /** Alias property for `level` lower-case access. */
    var level: WarpLoggerLevel
        @JvmName("getLowerLevel")
        get() = currentLevel
        @JvmName("setLowerLevel")
        set(value) {
            Level = value
        }

    init {
        applyLevel(WarpLoggerLevel.Info)
    }

    private fun applyLevel(warpLevel: WarpLoggerLevel) {
        val kmLevel = when (warpLevel) {
            WarpLoggerLevel.Verbose -> LogLevel.Verbose
            WarpLoggerLevel.Debug -> LogLevel.Debug
            WarpLoggerLevel.Info -> LogLevel.Info
            WarpLoggerLevel.Warn -> LogLevel.Warn
            WarpLoggerLevel.Error -> LogLevel.Error
            WarpLoggerLevel.Off -> LogLevel.Off
        }
        KmLogging.setLogLevel(kmLevel)
    }

    fun v(tag: String, message: String) {
        if (currentLevel <= WarpLoggerLevel.Verbose) {
            KmLog(tag).v { message }
        }
    }

    fun d(tag: String, message: String) {
        if (currentLevel <= WarpLoggerLevel.Debug) {
            KmLog(tag).d { message }
        }
    }

    fun i(tag: String, message: String) {
        if (currentLevel <= WarpLoggerLevel.Info) {
            KmLog(tag).i { message }
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLevel <= WarpLoggerLevel.Warn) {
            KmLog(tag).w(err = throwable) { message }
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLevel <= WarpLoggerLevel.Error) {
            KmLog(tag).e(err = throwable) { message }
        }
    }
}
