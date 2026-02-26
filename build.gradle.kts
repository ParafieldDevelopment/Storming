plugins {
    java
    application
    id("org.beryx.jlink") version "3.1.1"
}

group = "com.parafield.storming"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.5.4")
    implementation("com.formdev:flatlaf-extras:3.5.4")
    implementation("com.github.weisj:jsvg:1.6.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
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
        name = "Storming"
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
        installerName = "Storming"
        appVersion = "1.0"
        vendor = "Parafield"

        // ⚠ Do NOT manually add --app-image anywhere
        // Optional icons can still be added
        // installerOptions.add("--icon")
        // installerOptions.add("src/main/resources/Storming.ico")
    }
}