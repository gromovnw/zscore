import org.gradle.api.file.DuplicatesStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0")
}

tasks.shadowJar {
    archiveBaseName.set("zScore-Velocity")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    relocate("eu.okaeri", "it.gromov.zscore.shaded.eu.okaeri")
    relocate("org.yaml.snakeyaml", "it.gromov.zscore.shaded.org.yaml.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
