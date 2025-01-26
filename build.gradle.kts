plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}
val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup
repositories {
    mavenCentral()
}
dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val yarn_mappings: String by project
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    val loader_version: String by project
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    val fabric_version: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    implementation("io.netty:netty-all:4.1.24.Final") // this specific version is here for a reason, I just don't know what reason

    modImplementation("org.jetbrains.exposed:exposed-core:0.58.0")
    shadow("org.jetbrains.exposed:exposed-core:0.58.0")
    modImplementation("org.jetbrains.exposed:exposed-jdbc:0.58.0")
    shadow("org.jetbrains.exposed:exposed-jdbc:0.58.0")
    modImplementation("org.xerial:sqlite-jdbc:3.48.0.0")
    shadow("org.xerial:sqlite-jdbc:3.48.0.0")
}

loom {
    accessWidenerPath.set(file("src/main/resources/deathgames-server-mod.accesswidener"))
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

tasks.shadowJar {
    configurations = listOf(project.configurations.shadow.get())
}

tasks {
    withType<JavaCompile> {
        options.release.set(21)
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }

    java {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
