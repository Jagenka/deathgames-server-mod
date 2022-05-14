package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

private const val CONF_FILE = "deathgames_conf.yaml"

object DeathGames : DedicatedServerModInitializer
{
    val spawnPoints = ArrayList<Coords>()
    val kills = ArrayList<Kill>()
    val players = HashMap<String, ServerPlayerEntity>()

    override fun onInitializeServer()
    {
        loadConfig()

        println("DeathGames Mod initialized!")
    }

    private fun loadConfig()
    {
        val path = FabricLoader.getInstance().configDir.resolve(CONF_FILE)
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

        spawnPoints.clear()
        spawnPoints.addAll(root.node("spawns").getList(Coords::class.java) ?: error("Error loading DeathGames config for spawns"))
    }

    fun getPlayer(name: String): ServerPlayerEntity?
    {
        if (players.containsKey(name)) return players[name]
        ifServerLoaded { minecraftServer ->
            minecraftServer.playerManager.playerList.forEach { player ->
                players[player.name.asString()] = player
            }
        }
        if (players.containsKey(name)) return players[name]
        return null
    }

    @JvmStatic
    fun registerKill(kill: Kill)
    {
        kills.add(kill)
        println("${kill.attacker.name.asString()} killed ${kill.deceased.name.asString()}")
    }
}
