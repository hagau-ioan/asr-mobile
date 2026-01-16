package com.asr.financial

import androidx.compose.ui.window.ComposeUIViewController
import com.asr.financial.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
