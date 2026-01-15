package com.asr.financial

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.asr.financial.presentation.theme.AsrTheme

@Composable
fun App() {
    AsrTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Text("ASR Financial Management")
        }
    }
}
