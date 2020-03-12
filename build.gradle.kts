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
