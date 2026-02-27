plugins {
    java
    application
    id("org.beryx.jlink") version "3.1.1"
}

val projectGroup: String by project
val projectVersion: String by project
val projectName: String by project
val flatlafVersion: String by project
val jsvgVersion: String by project
val junitBomVersion: String by project
val appVersionProperty: String by project
val vendorName: String by project

group = projectGroup
version = projectVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:$flatlafVersion")
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")
    implementation("com.github.weisj:jsvg:$jsvgVersion")
    
    // Add JNA for Native Access
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")

    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.parafield.storming.EditorApp")
    // Tell Gradle the module name explicitly
    mainModule.set("com.parafield.storming")
}

jlink {
    launcher {
        name = projectName
        noConsole = true // GUI-only
    }

    jpackage {
        val os = System.getProperty("os.name").lowercase()

        // Always set the installer type
        installerType = when {
            os.contains("win") && System.getenv("PATH")?.contains("WiX Toolset") == true -> "exe"
            os.contains("win") -> "msi" // fallback
            else -> "app-image"
        }

        installerName = projectName
        appVersion = appVersionProperty
        vendor = vendorName

        // Windows icon, Start Menu & Shortcut
        if (os.contains("win")) {
            installerOptions.addAll(
                listOf(
                    "--icon", "packaging/icon.ico", // <-- your real .ico outside resources
                    "--win-menu",
                    "--win-shortcut",
                    "--win-menu-group", projectName
                )
            )
        }

        // macOS icon
        if (os.contains("mac")) {
            installerOptions.addAll(listOf("--icon", "packaging/icon.icns"))
        }

        // Linux icon
        if (os.contains("nux")) {
            installerOptions.addAll(listOf("--icon", "packaging/icon.png"))
        }
    }
}