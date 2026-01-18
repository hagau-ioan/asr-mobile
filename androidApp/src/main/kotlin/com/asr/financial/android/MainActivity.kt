package com.asr.financial.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.asr.financial.App
import com.asr.financial.di.initKoin
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        initKoin {
            androidContext(this@MainActivity)
        }
        
        setContent {
            App()
        }
    }
}
