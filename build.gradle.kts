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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.70")
    implementation("de.crusader:kotlin-extensions-jvm:1.0.0")
    implementation("de.crusader:library-objects-jvm:1.0.0")
    implementation("org.languagetool:language-de:4.8")
}