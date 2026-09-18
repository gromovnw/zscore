plugins {
    kotlin("jvm") version "2.4.20" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
}

allprojects {
    group = "it.gromov"
    version = "1.1.0"

    repositories {
        mavenCentral()
        maven("https://storehouse.okaeri.eu/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net/")
    }
}
