package com.asr.financial.platform

import platform.Foundation.NSLog

actual class Logger {
    
    actual fun debug(tag: String, message: String) {
        NSLog("DEBUG [$tag]: $message")
    }
    
    actual fun info(tag: String, message: String) {
        NSLog("INFO [$tag]: $message")
    }
    
    actual fun warning(tag: String, message: String) {
        NSLog("WARNING [$tag]: $message")
    }
    
    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("ERROR [$tag]: $message - ${throwable.message}")
        } else {
            NSLog("ERROR [$tag]: $message")
        }
    }
}
