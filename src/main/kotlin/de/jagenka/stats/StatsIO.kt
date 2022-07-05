package de.jagenka.stats

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

object StatsIO
{
    private lateinit var pathToStatsFile: Path

    private val serializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true // WHO EVEN THOUGHT THIS WOULD BE A GOOD IDEA AS FALSE BY DEFAULT? WTF? WHAT IF I SEND A MESSAGE OVER THE NETWORK?
    }

    lateinit var stats: StatsBaseEntry

    @OptIn(ExperimentalSerializationApi::class)
    fun loadStats()
    {
        pathToStatsFile = FabricLoader.getInstance().configDir.resolve("deathgames_stats.json")
        if (!Files.exists(pathToStatsFile))
        {
            Files.createFile(pathToStatsFile)
            Files.writeString(pathToStatsFile, serializer.encodeToString(StatsBaseEntry()))
        }

        stats = serializer.decodeFromStream(pathToStatsFile.toFile().inputStream())
    }

    fun store()
    {
        val json = serializer.encodeToString(stats)
        Files.writeString(pathToStatsFile, json)
    }

    fun resetAllStats()
    {
        stats = StatsBaseEntry()
        store()
    }

    fun resetStatsForPlayer(playerName: String)
    {
        stats.playerEntries.remove(playerName)
        store()
    }
}
