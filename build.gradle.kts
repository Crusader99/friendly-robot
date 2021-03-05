import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    application
    kotlin("jvm") version "1.4.31"
}

group = "de.crusader"
version = "1.0.1"

application {
    applicationName = "FriendlyRobot"
    mainClassName = "de.crusader.friendlyrobot.FriendlyRobotKt"
}

repositories {
    maven("https://provider.ddnss.de/repository")
    jcenter()
    mavenCentral()
}

// Get version of kotlin plugin defined in plugins section
val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

dependencies {
    // Library for automated test units
    implementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    // Log management
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Multi-platform library for shorter access to some java api functionality
    implementation("de.crusader:kotlin-extensions-jvm:1.0.2")

    // Kotlin based multi-platform project provides Color, Rectangle, Point, etc
    implementation("de.crusader:library-objects-jvm:1.0.0")

    // Easy to use command line argument parser for kotlin
    implementation("de.crusader:library-args:1.0.1")

    // Language tool allows style and grammar checking
    implementation("org.languagetool:language-all:4.8")
}