package de.jagenka

import de.jagenka.Config.bonusPlatformRadius
import de.jagenka.timer.ScheduledTask
import de.jagenka.timer.Timer
import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.math.abs

object DGBonusManager
{
    private val platforms = mutableListOf<Platform>()

    private val inactiveBlock = Blocks.RED_CONCRETE
    private val activeBlock = Blocks.LIME_CONCRETE

    private var currentSpawnTask: ScheduledTask? = null
    private var currentDespawnTask: ScheduledTask? = null

    fun setPlatforms(platforms: List<Platform>)
    {
        this.platforms.clear()
        this.platforms.addAll(platforms)
    }

    fun activateRandomPlatforms(howMany: Int)
    {
        platforms.forEach { it.active = false }
        platforms.toList().shuffled().forEachIndexed { index, platform ->
            if (index >= howMany) return@forEachIndexed
            platform.active = true
        }

        colorPlatforms()
    }

    fun disableAllPlatforms()
    {
        platforms.forEach { it.active = false }
        colorPlatforms()
    }

    fun getActivePlatforms() = platforms.filter { it.active }

    fun isOnActivePlatform(player: ServerPlayerEntity) = getActivePlatforms().any {
        val (x, y, z) = it.coordinates - player.pos.toDGCoordinates()
        y < 2 && abs(x) < bonusPlatformRadius && abs(z) < bonusPlatformRadius
    }

    fun colorPlatforms()
    {
        platforms.forEach { platform ->
            Util.getBlocksInSquareRadiusAtFixY(platform.coordinates, bonusPlatformRadius).forEach { (block, coordinates) ->
                if (block isSame inactiveBlock || block isSame activeBlock)
                {
                    Util.setBlockAt(coordinates, if (platform.active) activeBlock else inactiveBlock)
                }
            }
        }
    }

    fun spawnNow()
    {
        currentSpawnTask?.let {
            Timer.unscheduleTask(it)
            it.task()
        }
    }

    fun init()
    {
        disableAllPlatforms()
        currentSpawnTask = Timer.schedule({
            spawnBonusPlatformTask()
        }, Config.bonusPlatformInitialSpawn)
        currentDespawnTask = null
    }

    private fun spawnBonusPlatformTask()
    {
        activateRandomPlatforms(1)
        currentDespawnTask = Timer.schedule({
            disableBonusPlatformTask()
        }, Config.bonusPlatformStayTime)
        currentSpawnTask = null

        getActivePlatforms().forEach { platform -> Util.sendChatMessage("Bonus \"${platform.name}\" now active.") }
    }

    private fun disableBonusPlatformTask()
    {
        disableAllPlatforms()
        currentSpawnTask = Timer.schedule({
            spawnBonusPlatformTask()
        }, Config.bonusPlatformSpawnInterval - Config.bonusPlatformStayTime)
        currentDespawnTask = null

        Util.sendChatMessage("All bonuses now inactive.")
    }
}

@ConfigSerializable
data class Platform(val name: String, val coordinates: Coordinates, var active: Boolean = false)