rootProject.name = "asr-financial"

include(":shared")
include(":composeApp")
include(":androidApp")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
