package de.jagenka.config

import de.jagenka.managers.BonusManager
import de.jagenka.managers.SpawnManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

object Config
{
    private const val CONF_FILE = "deathgames"

    private val serializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true // WHO EVEN THOUGHT THIS WOULD BE A GOOD IDEA AS FALSE BY DEFAULT? WTF? WHAT IF I SEND A MESSAGE OVER THE NETWORK?
    }

    lateinit var configEntry: ConfigEntry

    val worldSpawn
        get() = configEntry.spawns.worldSpawn

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

    val livesPerPlayer
        get() = configEntry.lives.perPlayer
    val livesPerTeam
        get() = configEntry.lives.perTeam

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
        get() = configEntry.misc.arenaBounds
    val spectatorRadiusPadding
        get() = configEntry.misc.spectatorRadiusPadding

    val refundPercent
        get() = configEntry.shopSettings.refundPercent

    val captureTimeNeeded
        get() = configEntry.spawns.captureTimeNeeded
    val captureEnabled
        get() = configEntry.spawns.captureEnabled

    val trapConfig
        get() = configEntry.traps

    val shopCloseTimeAfterReveal
        get() = configEntry.misc.shopCloseTimeAfterReveal
    val killStreakPenaltyCap
        get() = configEntry.misc.killStreakPenaltyCap

    fun loadJSON()
    {
        configEntry = serializer.decodeFromString(FabricLoader.getInstance().configDir.resolve("$CONF_FILE.json").toFile().readText())

        SpawnManager.setSpawns(configEntry.spawns.spawnPositions.coords) // TODO: these won't be change by the config command
        BonusManager.setPlatforms(configEntry.bonus.platforms)
    }

    fun store()
    {
        val json = serializer.encodeToString(configEntry)
        val path = FabricLoader.getInstance().configDir.resolve("$CONF_FILE.json")

        Files.writeString(path, json)
    }
}