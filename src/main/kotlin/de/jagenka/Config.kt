package de.jagenka

import de.jagenka.managers.BonusManager
import de.jagenka.managers.Platform
import de.jagenka.managers.SpawnManager
import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

object Config
{
    private const val CONF_FILE = "deathgames"


    var worldSpawn = Coordinates(0.5, 51.0, 0.5, 0f, 0f)
        private set

    var spawnPlatformRadius = 1
        private set

    var defaultSpawn = Coordinates(0.5, 80.0, 0.5, 0f, 0f)
        private set

    var bonusPlatformRadius = 1
        private set
    var bonusPlatformSpawnInterval = 1
        private set
    var bonusPlatformStayTime = 1
        private set
    var bonusPlatformInitialSpawn = 1
        private set
    var bonusMoneyAmount = 1
        private set
    var bonusMoneyInterval = 1
        private set

    var livesPerPlayer = 1
        private set
    var livesPerTeam = 1
        private set

    var shuffleSpawnsInterval = 1
        private set
    var shuffleDelayAfterKill = 1
        private set

    var moneyInterval = 1
        private set
    var moneyPerInterval = 1
        private set
    var moneyPerKill = 1
        private set
    var moneyBonusPerKillStreakKill = 1
        private set
    var startMoneyPerPlayer = 1
        private set

    fun loadYAML()
    {
        val path = FabricLoader.getInstance().configDir.resolve("$CONF_FILE.yaml")
        val confLoader = YamlConfigurationLoader.builder().path(path).build()
        val root = confLoader.load(
            ConfigurationOptions.defaults()
//                .implicitInitialization(true)
                .serializers {
                    it.registerAnnotatedObjects(
                        objectMapperFactory()
                    )
                }
        )

        worldSpawn = root.node("worldSpawn").get(Coordinates::class.java) ?: configError("worldSpawn")

        SpawnManager.setSpawns(root.node("spawns").getList(Coordinates::class.java) ?: configError("spawns"))
        spawnPlatformRadius = root.node("spawnPlatformRadius").int
        bonusPlatformSpawnInterval = root.node("bonusPlatformSpawnInterval").int
        bonusPlatformStayTime = root.node("bonusPlatformStayTime").int
        bonusPlatformInitialSpawn = root.node("bonusPlatformInitialSpawn").int
        bonusMoneyAmount = root.node("bonusMoneyAmount").int
        bonusMoneyInterval = root.node("bonusMoneyInterval").int

        defaultSpawn = root.node("defaultSpawn").get(Coordinates::class.java) ?: configError("defaultSpawn")

        BonusManager.setPlatforms(root.node("bonusPlatforms").getList(Platform::class.java) ?: configError("bonusPlatforms"))
        bonusPlatformRadius = root.node("bonusPlatformRadius").int

        moneyPerKill = root.node("moneyPerKill").int
        livesPerPlayer = root.node("livesPerPlayer").int
        livesPerTeam = root.node("livesPerTeam").int
        moneyBonusPerKillStreakKill = root.node("moneyBonusPerKillStreakKill").int
        startMoneyPerPlayer = root.node("startMoneyPerPlayer").int

        shuffleSpawnsInterval = root.node("shuffleSpawnsInterval").int
        shuffleDelayAfterKill = root.node("shuffleDelayAfterKill").int

        moneyInterval = root.node("moneyInterval").int
        moneyPerInterval = root.node("moneyPerInterval").int
    }

    private fun configError(whatFailed: String): Nothing = error("Error loading DeathGames $whatFailed from config")
}