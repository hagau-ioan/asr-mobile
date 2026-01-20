plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":shared"))
            binaryOption("bundleId", "com.asr.financial.ComposeApp")
        }
        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        commonMain.dependencies {
            // Shared module
            api(project(":shared"))

            // Compose Multiplatform
            // Note: These aliases are deprecated in CMP 1.10 but still work.
            // They handle version compatibility automatically across ~15 artifacts.
            // Migrate to direct coordinates when JetBrains releases a BOM.
            // See: https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html
            @Suppress("DEPRECATION")
            implementation(compose.runtime)
            @Suppress("DEPRECATION")
            implementation(compose.foundation)
            @Suppress("DEPRECATION")
            implementation(compose.material3)
            @Suppress("DEPRECATION")
            implementation(compose.materialIconsExtended)
            @Suppress("DEPRECATION")
            implementation(compose.ui)
            @Suppress("DEPRECATION")
            implementation(compose.components.resources)
            @Suppress("DEPRECATION")
            implementation(compose.components.uiToolingPreview)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // DateTime (for formatting timestamps)
            implementation(libs.kotlinx.datetime)

            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation 3 (Multiplatform)
            implementation(libs.navigation.compose)

            // Lifecycle/ViewModel (Multiplatform)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // Coil Image Loading (Multiplatform)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Ktor Client (for Coil networking)
            implementation(libs.ktor.client.core)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity.compose)

            // Ktor Android engine
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            // Ktor iOS engine
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.asr.financial"
    compileSdk = 36

    buildFeatures {
        buildConfig = true  // Enable BuildConfig generation for version info
    }

    defaultConfig {
        minSdk = 26
        
        // Read version from libs.versions.toml (or gradle.properties as fallback) and expose to BuildConfig
        // This is only needed in the presentation layer (composeApp)
        val versionName = try {
            libs.versions.appVersion.get()
        } catch (e: Exception) {
            project.findProperty("app.version.name") as? String ?: "1.0.0"
        }
        
        val versionCode = try {
            libs.versions.appVersionCode.get().toInt()
        } catch (e: Exception) {
            ((project.findProperty("app.version.code") as? String) ?: "1").toInt()
        }
        
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("int", "VERSION_CODE", "$versionCode")  // Use "int" (Java) not "Int" (Kotlin)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
