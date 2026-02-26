plugins {
    java
    application
}

group = "com.parafield.storming"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.5.4")
    implementation("com.formdev:flatlaf-extras:3.5.4")
    // jsvg dependency - usually com.github.weisj:jsvg
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

// Standard source sets are used by default
