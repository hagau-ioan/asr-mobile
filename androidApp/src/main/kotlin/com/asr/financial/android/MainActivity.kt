package com.asr.financial.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.asr.financial.App
import com.asr.financial.domain.usecase.ConvertIntentDataAndSavePendingNotificationUseCase
import com.asr.financial.di.initKoin
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result - notifications will work if granted
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        initKoin {
            androidContext(this@MainActivity)
        }
        
        // Extract notification data from intent and store in DataStore if present
        val notificationData = extractNotificationDataFromIntent(intent)
        if (notificationData.isNotEmpty()) {
            storeNotificationInDataStore(notificationData)
        }
        
        setContent {
            App() // Notification data is stored in DataStore via UseCase
        }
        
        // Navigation will be handled by NavGraph with startDestination = Routes.HOME
        // launchMode="singleTop" ensures we use existing instance when app is already open
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent so getIntent() returns the latest one
        
        // Handle notification tap when app is already running
        val notificationData = extractNotificationDataFromIntent(intent)
        if (notificationData.isNotEmpty()) {
            storeNotificationInDataStore(notificationData)
        }
    }
    
    private fun extractNotificationDataFromIntent(intent: Intent?): Map<String, String> {
        if (intent == null) return emptyMap()

        val data = mutableMapOf<String, String>()
        intent.extras?.keySet()?.forEach { key ->
            if (key != null) {
                // Only extract String values, skip Firebase internal keys (google.*, gcm.*)
                // and non-String values to avoid ClassCastException
                if (!key.startsWith("google.") && !key.startsWith("gcm.")) {
                    try {
                        val value = intent.extras?.getString(key)
                        if (value != null) {
                            data[key] = value
                        }
                    } catch (e: ClassCastException) {
                        // Skip non-String values (Long, Integer, Bundle, etc.)
                        // These are Firebase internal metadata that we don't need
                    }
                }
            }
        }
        return data
    }
    
    private fun storeNotificationInDataStore(notificationData: Map<String, String>) {
        scope.launch {
            try {
                val convertAndSaveUseCase: ConvertIntentDataAndSavePendingNotificationUseCase by inject()
                val intentExtras = getIntent().extras
                
                // Use UseCase to convert and save notification data
                convertAndSaveUseCase(
                    notificationData = notificationData,
                    getStringExtra = { key ->
                        notificationData[key] ?: intentExtras?.getString(key)
                    },
                    getLongExtra = { key, default ->
                        notificationData[key]?.toLongOrNull() 
                            ?: (intentExtras?.getLong(key, default) ?: default)
                    }
                )
            } catch (e: Exception) {
                // Silently handle errors - notification storage is not critical
                e.printStackTrace()
            }
        }
    }

    private fun requestNotificationPermission() {
        // Only needed for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
