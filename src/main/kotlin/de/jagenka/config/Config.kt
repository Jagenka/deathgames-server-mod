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

    lateinit var configEntry: ConfigEntry

    // TODO: clean this up

    val isEnabled
        get() = configEntry.general.enabled


    val spawnPlatformRadius
        get() = configEntry.spawns.platformRadius

    val defaultSpawn
        get() = configEntry.spawns.spectatorSpawn
    val lobbySpawn
        get() = configEntry.spawns.lobbySpawn

    val bonusPlatformRadius
        get() = configEntry.bonus.radius
    val bonusPlatformSpawnInterval
        get() = configEntry.bonus.spawnInterval
    val bonusPlatformStayTime
        get() = configEntry.bonus.stayTime
    val bonusPlatformInitialSpawn
        get() = configEntry.bonus.initialSpawn
    val bonusMoneyAmount
        get() = configEntry.bonus.moneyAmount
    val bonusMoneyInterval
        get() = configEntry.bonus.moneyInterval
    val respawnsPerTeam
        get() = configEntry.respawns.perTeam

    val shuffleSpawnsInterval
        get() = configEntry.spawns.shuffleInterval
    val shuffleDelayAfterKill
        get() = configEntry.spawns.shuffleDelayAfterKill

    val moneyInterval
        get() = configEntry.money.interval
    val moneyPerInterval
        get() = configEntry.money.amountPerInterval
    val moneyPerKill
        get() = configEntry.money.perKill
    val moneyBonusPerKillStreakKill
        get() = configEntry.money.perKillStreakKill
    val startMoneyPerPlayer
        get() = configEntry.money.start

    val revealTimePerPlayer
        get() = configEntry.misc.revealTimePerPlayer

    val tpOutOfShopAfter
        get() = configEntry.shopSettings.tpOutOfShopAfter

    val shopBounds
        get() = configEntry.shopSettings.shopBounds
    val arenaBounds
        get() = configEntry.general.arenaBounds
    val spectatorRadiusPadding
        get() = configEntry.general.spectatorRadiusPadding

    val refundPercent
        get() = configEntry.shopSettings.refundPercent

    val captureTimeNeeded
        get() = configEntry.spawns.captureTimeNeeded
    val captureEnabled
        get() = configEntry.spawns.enableCapture

    val shopCloseTimeAfterReveal
        get() = configEntry.misc.shopCloseTimeAfterReveal
    val killStreakPenaltyCap
        get() = configEntry.misc.killStreakPenaltyCap
    val startInShop
        get() = configEntry.misc.startInShop
    val startInShopTpAfterSeconds
        get() = configEntry.misc.startInShopTpAfterSeconds

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
                configEntry = ConfigEntry()
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
        configEntry = serializer.decodeFromString(jsonConfFile.readText())
    }

    fun store()
    {
        val json = serializer.encodeToString(configEntry)

        Files.writeString(pathToConfFile, json)
    }
}