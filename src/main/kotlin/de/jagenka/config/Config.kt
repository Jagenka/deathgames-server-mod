package de.jagenka.config

import de.jagenka.managers.BonusManager
import de.jagenka.managers.SpawnManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader

object Config
{
    private const val CONF_FILE = "deathgames"

    private lateinit var configEntry: ConfigEntry

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
        get() = configEntry.misc.tpOutOfShopAfter

    val shopBounds
        get() = configEntry.misc.shopBounds
    val arenaBounds
        get() = configEntry.misc.arenaBounds

    fun loadJSON()
    {
        configEntry = Json.decodeFromString(FabricLoader.getInstance().configDir.resolve("$CONF_FILE.json").toFile().readText())

        SpawnManager.setSpawns(configEntry.spawns.spawnPositions)
        BonusManager.setPlatforms(configEntry.bonus.platforms)
    }
}