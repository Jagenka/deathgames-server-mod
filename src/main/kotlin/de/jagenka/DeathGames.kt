package de.jagenka

import de.jagenka.commands.JayCommand
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

object DeathGames : DedicatedServerModInitializer
{
    const val CONF_FILE = "deathgames_conf.yaml"

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

        DGSpawnManager.loadConfig(root)
        DGKillManager.loadConfig(root)
    }
}
