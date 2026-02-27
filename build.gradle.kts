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

    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.parafield.storming.EditorApp")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

jlink {
    launcher {
        name = projectName
        noConsole = true // GUI-only
    }

    jpackage {
        val os = System.getProperty("os.name").lowercase()

        when {
            os.contains("win") -> {
                // Windows: produce exe if WiX is installed
                if (System.getenv("PATH")?.contains("WiX Toolset") == true) {
                    installerType = "exe"
                } else {
                    installerType = null // portable folder
                }
            }
            os.contains("mac") || os.contains("nux") -> {
                // macOS/Linux: just app-image
                installerType = "app-image"
            }
        }

        // Set general options
        installerName = projectName
        appVersion = appVersionProperty
        vendor = vendorName

        // ⚠ Do NOT manually add --app-image anywhere
        // Optional icons can still be added
        installerOptions.add("--icon")
        installerOptions.add("src/main/resources/icon.ico")
    }
}