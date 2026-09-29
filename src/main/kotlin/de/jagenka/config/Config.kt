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
                writeGeneralConfig()
            }

            pathToShopConfFile = configFolder.resolve("shop.yaml")
            if (!Files.exists(pathToShopConfFile))
            {
                Files.createFile(pathToShopConfFile)
                writeShopConfig()
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

    fun writeGeneralConfig()
    {
        try
        {
            Files.writeString(pathToConfFile, serializer.encodeToString(internalConfigEntry))
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while storing general config!", e)
        }
    }

    fun writeShopConfig()
    {
        try
        {
            Files.writeString(pathToShopConfFile, serializer.encodeToString(shopConfig))
        } catch (e: Exception)
        {
            DeathGames.logger.error("Error while storing shop config!", e)
        }
    }
}