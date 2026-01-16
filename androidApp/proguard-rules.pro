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
