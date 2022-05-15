package de.jagenka

import de.jagenka.commands.JayCommand
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

private const val CONF_FILE = "deathgames_conf.yaml"

object DeathGames : DedicatedServerModInitializer
{
    val spawnPoints = ArrayList<Coords>()
    val kills = ArrayList<Kill>()

    override fun onInitializeServer()
    {
        loadConfig()

        registerCommands()

        println("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            JayCommand.register(dispatcher)
        }
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

    @JvmStatic
    fun registerKill(kill: Kill)
    {
        kills.add(kill)
        println("${kill.attacker.name.asString()} killed ${kill.deceased.name.asString()}")
    }
}
