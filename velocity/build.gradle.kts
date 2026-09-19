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
    relocate("com.google.gson", "it.gromov.zscore.shaded.com.google.gson")
    relocate("com.mysql", "it.gromov.zscore.shaded.com.mysql")
    exclude("org/sqlite/native/FreeBSD/**", "org/sqlite/native/Linux-Android/**", "org/sqlite/native/Linux/riscv64/**", "org/sqlite/native/Linux/ppc64/**", "org/sqlite/native/Linux/armv6/**", "org/sqlite/native/Linux/armv7/**", "org/sqlite/native/Linux/x86/**", "org/sqlite/native/Windows/armv7/**", "org/sqlite/native/Windows/x86/**")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
