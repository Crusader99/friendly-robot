plugins {
    kotlin("jvm") version "1.3.70"
}

group = "de.crusader"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://provider.ddnss.de/repository")
    }
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.70")

    // Log management
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Multi-platform library for shorter access to some java api functionality
    implementation("de.crusader:kotlin-extensions-jvm:1.0.0")

    // Kotlin based multi-plattform projekt provides Color, Rectangle, Point, etc
    implementation("de.crusader:library-objects-jvm:1.0.0")

    // Easy to use command line argument parser for kotlin
    implementation("de.crusader:library-args:1.0.0")

    // Language tool allows style and grammar checking
    implementation("org.languagetool:language-all:4.8")
}