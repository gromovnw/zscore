import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("eu.okaeri:okaeri-configs-core:6.1.0-beta.4")
    api("eu.okaeri:okaeri-configs-yaml-snakeyaml:6.1.0-beta.4")
}
