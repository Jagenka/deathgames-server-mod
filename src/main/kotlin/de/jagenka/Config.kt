package de.jagenka

import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

object Config
{
    var defaultSpawn = Coordinates(0.5, 80.0, 0.5, 0f, 0f)
        private set

    var moneyPerKill = 1
        private set
    var startMoneyPerPlayer = 1
        private set
    var livesPerPlayer = 1
        private set
    var livesPerTeam = 1
        private set
    var killStreakBonus = 1
        private set

    init
    {
        val path = FabricLoader.getInstance().configDir.resolve(DeathGames.CONF_FILE)
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

        DGSpawnManager.setSpawns(root.node("spawns").getList(Coordinates::class.java) ?: error("Error loading DeathGames spawns from config"))
        defaultSpawn = root.node("defaultSpawn").get(Coordinates::class.java) ?: error("Error loading DeathGames defaultSpawn from config")

        moneyPerKill = root.node("moneyPerKill").int
        livesPerPlayer = root.node("livesPerPlayer").int
        livesPerTeam = root.node("livesPerTeam").int
        killStreakBonus = root.node("killStreakBonus").int
        startMoneyPerPlayer = root.node("startMoneyPerPlayer").int
    }
}