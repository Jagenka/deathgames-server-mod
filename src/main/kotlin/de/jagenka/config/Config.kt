package de.jagenka.config

import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.shop.ShopEntries
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.util.WorldSavePath
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object Config
{
    private lateinit var pathToConfFile: Path

    private val serializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true // WHO EVEN THOUGHT THIS WOULD BE A GOOD IDEA AS FALSE BY DEFAULT? WTF? WHAT IF I SEND A MESSAGE OVER THE NETWORK?
    }

    /**
     * do not use this if not absolutely necessary
     */
    lateinit var internalConfigEntry: ConfigEntry

    val isEnabled
        get() = internalConfigEntry.general.enabled

    val general
        get() = internalConfigEntry.general

    val spawns
        get() = internalConfigEntry.spawns

    val bonus
        get() = internalConfigEntry.bonus

    val respawns
        get() = internalConfigEntry.respawns

    val money
        get() = internalConfigEntry.money

    val shopSettings
        get() = internalConfigEntry.shopSettings

    val misc
        get() = internalConfigEntry.misc

    val displayedText
        get() = internalConfigEntry.displayedText

    val shop
        get() = internalConfigEntry.shop


    fun lateLoadConfig()
    {
        Util.minecraftServer?.let { server ->
            val configFolder = server.getSavePath(WorldSavePath.ROOT).resolve("deathgames")
            if (!Files.exists(configFolder))
            {
                Files.createDirectories(configFolder)
            }
            pathToConfFile = configFolder.resolve("config.json")
            if (!Files.exists(pathToConfFile))
            {
                Files.createFile(pathToConfFile)
                internalConfigEntry = ConfigEntry()
                store()
            }
            load()

        } ?: error("Failed loading DeathGames config - Server not loaded yet.")

        DeathGames.logger.info("Successfully loaded DeathGames config!")
    }

    fun load()
    {
        loadJSON(pathToConfFile.toFile())
        ShopEntries.loadShop()
    }

    fun loadJSON(jsonConfFile: File)
    {
        internalConfigEntry = serializer.decodeFromString(jsonConfFile.readText())
    }

    fun store()
    {
        val json = serializer.encodeToString(internalConfigEntry)

        Files.writeString(pathToConfFile, json)
    }
}