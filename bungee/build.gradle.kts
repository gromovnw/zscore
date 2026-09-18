import org.gradle.api.file.DuplicatesStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
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
    implementation(project(":common"))
    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
}

tasks.processResources {
    expand("version" to project.version)
}

tasks.shadowJar {
    archiveBaseName.set("zScore-Bungee")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    relocate("eu.okaeri", "it.gromov.zscore.shaded.eu.okaeri")
    relocate("org.yaml.snakeyaml", "it.gromov.zscore.shaded.org.yaml.snakeyaml")
    relocate("com.google.gson", "it.gromov.zscore.shaded.com.google.gson")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
