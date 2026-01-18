import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}

// Load keystore properties
val keystorePropertiesFile = rootProject.file("androidApp/keys/keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.asr.financial.android"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.asr.financial"
        minSdk = 26
        targetSdk = 36
        // Read version from libs.versions.toml (or gradle.properties as fallback)
        val appVersionName = try {
            libs.versions.appVersion.get()
        } catch (e: Exception) {
            project.findProperty("app.version.name") as? String ?: "1.0.0"
        }
        
        val appVersionCode = try {
            libs.versions.appVersionCode.get().toInt()
        } catch (e: Exception) {
            (project.findProperty("app.version.code") as? String ?: "1").toInt()
        }
        
        versionCode = appVersionCode
        versionName = appVersionName
        
        // Expose version to BuildConfig so it's accessible in the app
        buildConfigField("String", "VERSION_NAME", "\"$appVersionName\"")
        buildConfigField("int", "VERSION_CODE", "$appVersionCode")  // Use "int" (Java) not "Int" (Kotlin)
    }
    
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":shared"))
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    
    // Firebase (for MainActivity initialization)
    // Note: firebase-auth-ktx includes Firebase Core automatically
    implementation(libs.firebase.auth)
}
