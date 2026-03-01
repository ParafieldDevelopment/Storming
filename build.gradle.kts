plugins {
    java
    application
    idea
    id("org.beryx.runtime") version "2.0.1"
}

val projectGroup: String by project
val projectVersion: String by project
val projectName: String by project
val flatlafVersion: String by project
val jsvgVersion: String by project
val jeditermVersion: String by project
val pty4jVersion: String by project
val junitBomVersion: String by project
val appVersionProperty: String by project
val vendorName: String by project

group = projectGroup
version = projectVersion

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

dependencies {
    implementation("com.formdev:flatlaf:$flatlafVersion")
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")
    implementation("com.github.weisj:jsvg:$jsvgVersion")

    // Terminal Emulator Dependencies
    implementation("org.jetbrains.jediterm:jediterm-ui:$jeditermVersion")
    implementation("org.jetbrains.jediterm:jediterm-core:$jeditermVersion")
    implementation("org.jetbrains.pty4j:pty4j:$pty4jVersion")

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

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

application {
    mainClass.set("com.parafield.storming.EditorApp")
}

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.base", "java.desktop", "java.logging", "java.naming", "java.xml"))

    jpackage {
        val osName = System.getProperty("os.name").lowercase()
        val isWindows = osName.contains("windows")
        val isMac = osName.contains("mac")
        val isLinux = osName.contains("linux")

        installerOptions.addAll(
            listOf(
                "--name", projectName,
                "--vendor", vendorName,
                "--app-version", appVersionProperty,
                "--copyright", "© $vendorName"
            )
        )

        if (isWindows) {
            installerOptions.addAll(listOf("--icon", "packaging/icon.ico", "--type", "msi"))
        } else if (isMac) {
            val icnsIcon = file("packaging/icon.icns")
            val iconPath = if (icnsIcon.exists()) "packaging/icon.icns" else "src/main/resources/com/parafield/storming/icons/png/icon.png"
            installerOptions.addAll(listOf("--icon", iconPath, "--type", "dmg"))
        } else if (isLinux) {
            installerOptions.addAll(listOf("--icon", "src/main/resources/com/parafield/storming/icons/png/icon.png", "--type", "deb"))
        }

        installerOptions.addAll(
            listOf(
                "--vendor", vendorName,
                "--copyright", "© $vendorName"
            )
        )

        if (isWindows) {
            installerOptions.addAll(
                listOf(
                    "--win-menu",
                    "--win-shortcut",
                    "--win-menu-group", projectName,
                    "--win-per-user-install",
                    "--win-dir-chooser",
                    "--win-upgrade-uuid", "4ad9b696-55b9-40b1-8864-edc18a657a6e"
                )
            )
        }

        if (isMac) {
            installerOptions.addAll(
                listOf(
                    "--mac-package-name", projectName,
                    "--mac-package-identifier", projectGroup
                )
            )
        }

        if (isLinux) {
            installerOptions.addAll(
                listOf(
                    "--linux-menu-group", "Development",
                    "--linux-shortcut"
                )
            )
        }
    }
}