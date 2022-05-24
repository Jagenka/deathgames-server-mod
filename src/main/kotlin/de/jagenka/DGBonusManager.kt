package de.jagenka

import de.jagenka.Config.bonusPlatformRadius
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.abs

object DGBonusManager
{
    private val platforms = mutableListOf<Platform>()

    fun setPlatforms(platforms: List<Coordinates>)
    {
        this.platforms.clear()
        platforms.forEach { this.platforms.add(Platform(it)) }
    }

    fun getActivePlatforms() = platforms.filter { it.active }

    fun isOnActivePlatform(player: ServerPlayerEntity) = getActivePlatforms().any { (coordinates) ->
        val (x, y, z) = coordinates - player.pos.toDGCoordinates()
        y < 2 && abs(x) < bonusPlatformRadius && abs(z) < bonusPlatformRadius
    }
}

data class Platform(val coordinates: Coordinates, var active: Boolean = true)