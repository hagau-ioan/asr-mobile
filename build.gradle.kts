import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Node

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

/**
 * Task to sync version from gradle.properties (or libs.versions.toml) to iOS Info.plist
 * This ensures iOS version matches the single source of truth.
 * 
 * Priority:
 * 1. First tries to read from libs.versions.toml (appVersion, appVersionCode)
 * 2. Falls back to gradle.properties (app.version.name, app.version.code)
 * 
 * The version is written to Info.plist (CFBundleShortVersionString and CFBundleVersion).
 */
tasks.register("syncIosVersion") {
    group = "versioning"
    description = "Syncs version from gradle.properties/libs.versions.toml to iOS Info.plist"
    
    doLast {
        // Try to read from libs.versions.toml first, then fall back to gradle.properties
        val versionName = try {
            libs.versions.appVersion.get()
        } catch (e: Exception) {
            project.findProperty("app.version.name") as? String ?: "1.0.0"
        }
        
        val versionCode = try {
            libs.versions.appVersionCode.get().toString()
        } catch (e: Exception) {
            (project.findProperty("app.version.code") as? String ?: "1")
        }
        
        val infoPlistFile = file("iosApp/iosApp/Info.plist")
        if (!infoPlistFile.exists()) {
            throw GradleException("Info.plist not found at ${infoPlistFile.absolutePath}")
        }
        
        // Parse XML
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val doc: Document = docBuilder.parse(infoPlistFile)
        
        // Find and update CFBundleShortVersionString
        val shortVersionKey = findKeyNode(doc, "CFBundleShortVersionString")
        if (shortVersionKey != null) {
            val stringNode = shortVersionKey.nextSibling
            if (stringNode != null && stringNode.nodeName == "string") {
                stringNode.textContent = versionName
            }
        }
        
        // Find and update CFBundleVersion
        val bundleVersionKey = findKeyNode(doc, "CFBundleVersion")
        if (bundleVersionKey != null) {
            val stringNode = bundleVersionKey.nextSibling
            if (stringNode != null && stringNode.nodeName == "string") {
                stringNode.textContent = versionCode
            }
        }
        
        // Write back to file
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty("indent", "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1")
        val source = DOMSource(doc)
        val result = StreamResult(infoPlistFile)
        transformer.transform(source, result)
        
        println("✅ Synced iOS version: $versionName ($versionCode) to Info.plist")
    }
}

/**
 * Helper function to find a key node in the plist dict structure
 */
fun findKeyNode(doc: Document, keyName: String): Node? {
    val dict = doc.documentElement.getElementsByTagName("dict").item(0) ?: return null
    val children = dict.childNodes
    
    for (i in 0 until children.length) {
        val node = children.item(i)
        if (node.nodeName == "key" && node.textContent == keyName) {
            return node
        }
    }
    return null
}

// Make syncIosVersion run before iOS framework builds
tasks.configureEach {
    if (name.startsWith("embedAndSignAppleFrameworkFor") || 
        name.startsWith("linkDebugFrameworkIos") ||
        name.startsWith("linkReleaseFrameworkIos")) {
        dependsOn("syncIosVersion")
    }
}
