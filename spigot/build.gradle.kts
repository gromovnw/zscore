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
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
}

tasks.processResources {
    expand("version" to project.version)
}

tasks.shadowJar {
    archiveBaseName.set("zScore-Spigot")
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
