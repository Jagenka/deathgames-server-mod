package de.jagenka.config

import com.charleskorn.kaml.SequenceStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.shop.ShopEntries
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Files
import java.nio.file.Path

object Config
{
    private lateinit var pathToConfFile: Path
    private lateinit var pathToShopConfFile: Path

    private val serializer = Yaml(
        configuration = YamlConfiguration(
            sequenceStyle = SequenceStyle.Block,
            encodeDefaults = true,
        )
    )

    var internalConfigEntry = MainConfig()
    var shopConfig = ShopConfig()

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
        get() = shopConfig


    fun lateLoadConfig()
    {
        Util.minecraftServer?.let { server ->
            val configFolder = server.getWorldPath(LevelResource.ROOT).resolve("deathgames")
            if (!Files.exists(configFolder))
            {
                Files.createDirectories(configFolder)
            }

            pathToConfFile = configFolder.resolve("config.yaml")
            if (!Files.exists(pathToConfFile))
            {
                Files.createFile(pathToConfFile)
                writeGeneralConfigIfMissing()
            }

            pathToShopConfFile = configFolder.resolve("shop.yaml")
            if (!Files.exists(pathToShopConfFile))
            {
                Files.createFile(pathToShopConfFile)
                writeShopConfigIfMissing()
            }

            load()
        } ?: error("Failed loading DeathGames config - Server not loaded yet.")

        DeathGames.logger.info("Successfully loaded DeathGames config!")
    }

    fun load()
    {
        loadGeneralConfig()
        loadShopConfig()
    }

    fun loadGeneralConfig()
    {
        try
        {
            internalConfigEntry = serializer.decodeFromString(pathToConfFile.toFile().readText())
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while reading general config!", e)
        }
    }

    fun loadShopConfig()
    {
        try
        {
            shopConfig = serializer.decodeFromString(pathToShopConfFile.toFile().readText())
            ShopEntries.reloadShop()
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while reading shop config!", e)
        }
    }

    private fun writeGeneralConfigIfMissing()
    {
        val stringFromDefault = Config::class.java.getResourceAsStream("/defaultConfig/config.yaml").use { stream ->
            stream?.bufferedReader()?.readText()
        }

        var stringToWrite = ""

        if (stringFromDefault != null)
        {
            try
            {
                // if default is decodable, copy default later
                serializer.decodeFromString<MainConfig>(stringFromDefault)
                stringToWrite = stringFromDefault
            } catch (_: Exception)
            {
            }
        }

        if (stringToWrite.isEmpty())
        {
            // if default wasn't decodable, use Config defaults
            stringToWrite = serializer.encodeToString(MainConfig())
        }

        try
        {
            // write working defaults to file
            Files.writeString(pathToConfFile, stringToWrite)
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while storing general config!", e)
        }
    }

    fun writeShopConfigIfMissing()
    {
        val stringFromDefault = Config::class.java.getResourceAsStream("/defaultConfig/shop.yaml").use { stream ->
            stream?.bufferedReader()?.readText()
        }

        var stringToWrite = ""

        if (stringFromDefault != null)
        {
            try
            {
                // if default is decodable, copy default later
                serializer.decodeFromString<ShopConfig>(stringFromDefault)
                stringToWrite = stringFromDefault
            } catch (_: Exception)
            {
            }
        }

        if (stringToWrite.isEmpty())
        {
            // if default wasn't decodable, use Config defaults
            stringToWrite = serializer.encodeToString(ShopConfig())
        }

        try
        {
            // write working defaults to file
            Files.writeString(pathToShopConfFile, stringToWrite)
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while storing general config!", e)
        }
    }

    fun debugSerializeDefaultConfig(): String
    {
        return serializer.encodeToString(internalConfigEntry)
    }

    fun debugSerializeShopConfig(): String
    {
        return serializer.encodeToString(shopConfig)
    }
}