# Proguard rules for ASR Financial Android App

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.asr.financial.**$$serializer { *; }
-keepclassmembers class com.asr.financial.** {
    *** Companion;
}
-keepclasseswithmembers class com.asr.financial.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep SQLDelight generated classes
-keep class com.asr.financial.db.** { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep data classes used for serialization
-keepclassmembers class com.asr.financial.domain.models.** {
    <fields>;
    <init>(...);
}

# Suppress warnings for missing error-prone annotations (compile-time only)
-dontwarn com.google.errorprone.annotations.**

# =====================================================
# Firebase Cloud Messaging (FCM) Rules
# =====================================================

# Keep Firebase Messaging classes
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.messaging.** { *; }

# Keep our Firebase Messaging Service (critical - must match AndroidManifest)
-keep class com.asr.financial.android.AsrFirebaseMessagingService { *; }

# Keep the FirebaseMessagingServiceBridge with all its volatile fields and callbacks
# CRITICAL: This bridge is used by FirebaseMessagingService (separate process) to communicate with app
-keep class com.asr.financial.platform.FirebaseMessagingServiceBridge { *; }
-keepclassmembers class com.asr.financial.platform.FirebaseMessagingServiceBridge {
    <fields>;  # Keep all fields including volatile ones
    <methods>; # Keep all methods including callbacks
}

# Keep PushNotification model used by FCM bridge
-keep class com.asr.financial.platform.PushNotification { *; }
-keepclassmembers class com.asr.financial.platform.PushNotification {
    <fields>;
    <init>(...);
}

# Keep FirebaseMessaging platform implementation
-keep class com.asr.financial.platform.FirebaseMessaging { *; }
-keepclassmembers class com.asr.financial.platform.FirebaseMessaging {
    <fields>;
    <init>(...);
}

# Keep notification-related use cases (used via Koin injection)
-keep class com.asr.financial.domain.usecase.ConvertIntentDataAndSavePendingNotificationUseCase { *; }
-keep class com.asr.financial.domain.usecase.ConvertAndSavePendingNotificationUseCase { *; }
-keep class com.asr.financial.domain.usecase.SavePendingNotificationUseCase { *; }
-keep class com.asr.financial.domain.usecase.ObservePendingNotificationUseCase { *; }
-keep class com.asr.financial.domain.usecase.GetPendingNotificationUseCase { *; }
-keep class com.asr.financial.domain.usecase.ClearPendingNotificationUseCase { *; }

# Keep PendingNotification domain model
-keep class com.asr.financial.domain.models.PendingNotification { *; }
-keepclassmembers class com.asr.financial.domain.models.PendingNotification {
    <fields>;
    <init>(...);
}

# Keep MainActivity (handles notification intents)
-keep class com.asr.financial.android.MainActivity { *; }
-keepclassmembers class com.asr.financial.android.MainActivity {
    <methods>; # Keep all methods including onNewIntent, onCreate, etc.
}

# Keep DataStore classes for notification persistence
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class com.asr.financial.data.datasource.DataStorePendingNotificationDataSource { *; }
-keep class com.asr.financial.data.datasource.PendingNotificationDataSource { *; }

# Keep notification repository implementation
-keep class com.asr.financial.data.repository.NotificationRepositoryImpl { *; }
-keep class com.asr.financial.domain.repository.NotificationRepository { *; }

# =====================================================
# Platform Abstraction Classes (Clock, etc.)
# =====================================================

# Keep Clock class (expect/actual) - CRITICAL for timestamp generation
# Used in FirebaseMessagingService and throughout the app
-keep class com.asr.financial.platform.Clock { *; }
-keepclassmembers class com.asr.financial.platform.Clock {
    <init>();  # Keep constructor
    <methods>; # Keep all methods (now(), formatDate(), etc.)
}

# Keep kotlinx.datetime.Instant (used by Clock)
-keep class kotlinx.datetime.Instant { *; }
-keepclassmembers class kotlinx.datetime.Instant {
    <methods>;
}

# Keep kotlin.time.Clock (used by Clock.android.kt)
-keep class kotlin.time.Clock { *; }
-keepclassmembers class kotlin.time.Clock {
    <fields>;
    <methods>;
}
