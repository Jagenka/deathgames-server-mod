package de.jagenka

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapper
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

private const val CONF_FILE = "deathgames_conf.yaml"

object DeathGames : DedicatedServerModInitializer
{
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

        val list = listOf(Coords(1, 2, 3), Coords(1, 2, 3), Coords(1, 2, 3), Coords(1, 2, 3))

        root.node("coords").setList(Coords::class.java, list)
        confLoader.save(root)

//        println(objectMapper<List<Coords>>().load(root.node("coords")))

        println(root.node("coords").getList(Coords::class.java))

//        objectMapper<Coords>().save(list, root.node("coords"))
//        objectMapper<Coords>().load(root.node("coords"))
    }
}

@ConfigSerializable
data class Coords(val x: Int, val y: Int, val z: Int)