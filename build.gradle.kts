import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    kotlin("jvm") version "1.4.32"
    id("se.patrikerdes.use-latest-versions") version "0.2.16"
    id("com.github.ben-manes.versions") version "0.38.0"
    application
    `maven-publish`
    idea
    eclipse
}

group = "de.crusader"
version = "1.0.4"

application {
    applicationName = "FriendlyRobot"
    mainClass.set("de.crusader.friendlyrobot.FriendlyRobotKt")
}

repositories {
    maven("https://provider.ddnss.de/repository")
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
    implementation("de.crusader:kotlin-extensions-jvm:1.0.17")

    // Kotlin based multi-platform project provides Color, Rectangle, Point, etc
    implementation("de.crusader:library-objects-jvm:1.0.16")

    // Easy to use command line argument parser for kotlin
    implementation("de.crusader:library-args-jvm:2.1.4")

    // Language tool allows style and grammar checking
    implementation("org.languagetool:language-all:5.3")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
    repositories {
        maven("$buildDir/repo")
    }
}
