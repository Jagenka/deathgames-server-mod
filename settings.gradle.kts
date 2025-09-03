
rootProject.name = "deathgames-server-mod"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom").version(loom_version)
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm").version(kotlinVersion)
    }
}
