package de.jagenka.managers

import de.jagenka.Coordinates
import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.config.Config.bonusPlatformInitialSpawn
import de.jagenka.config.Config.bonusPlatformRadius
import de.jagenka.isSame
import de.jagenka.timer.ScheduledTask
import de.jagenka.timer.Timer
import de.jagenka.toCenter
import kotlinx.serialization.Serializable
import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.abs

object BonusManager
{
    private val platforms = mutableListOf<Platform>()
    private val selectedPlatforms = mutableListOf<Platform>()

    private val inactiveBlock = Blocks.RED_CONCRETE
    private val activeBlock = Blocks.LIME_CONCRETE

    private var currentSpawnTask: ScheduledTask? = null
    private var currentDespawnTask: ScheduledTask? = null

    fun setPlatforms(platforms: List<Platform>)
    {
        BonusManager.platforms.clear()
        BonusManager.platforms.addAll(platforms)
    }

    fun queueRandomPlatforms(howMany: Int)
    {
        selectedPlatforms.clear()
        platforms.toList().shuffled().forEachIndexed { index, platform ->
            if (index >= howMany) return@forEachIndexed
            selectedPlatforms.add(platform)
        }
    }

    fun activateSelectedPlatforms()
    {
        selectedPlatforms.forEach { it.active = true }
        colorPlatforms()
    }

    fun getSelectedPlatforms() = selectedPlatforms.toList()

    @Deprecated("use queueRandomPlatforms and activateSelectedPlatforms instead", ReplaceWith("", ""), DeprecationLevel.WARNING)
    fun activateRandomPlatforms(howMany: Int)
    {
        queueRandomPlatforms(howMany)
        activateSelectedPlatforms()
    }

    fun disableAllPlatforms()
    {
        platforms.forEach { it.active = false }
        colorPlatforms()
    }

    fun getActivePlatforms() = platforms.filter { it.active }

    fun isOnActivePlatform(player: ServerPlayerEntity) = getActivePlatforms().any {
        val dx = abs(it.coordinates.x.toCenter() - player.pos.x)
        val dy = abs(it.coordinates.y.toDouble() - player.pos.y)
        val dz = abs(it.coordinates.z.toCenter() - player.pos.z)
        dy < 2 && dx <= bonusPlatformRadius + 0.5 && dz <= bonusPlatformRadius + 0.5
    }

    private fun colorPlatforms()
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
        queueRandomPlatforms(1)
        currentSpawnTask = Timer.schedule({
            spawnBonusPlatformTask()
        }, bonusPlatformInitialSpawn)
        currentDespawnTask = null
    }

    fun getTimeToSpawn(): Int?
    {
        currentSpawnTask?.let { return it.time - Timer.now() }
        return null
    }

    fun getTimeToDespawn(): Int?
    {
        currentDespawnTask?.let { return it.time - Timer.now() }
        return null
    }

    private fun spawnBonusPlatformTask()
    {
        activateSelectedPlatforms()
        currentDespawnTask = Timer.schedule({
            disableBonusPlatformTask()
        }, Config.bonusPlatformStayTime)
        currentSpawnTask = null
    }

    private fun disableBonusPlatformTask()
    {
        disableAllPlatforms()
        queueRandomPlatforms(1)
        currentSpawnTask = Timer.schedule({
            spawnBonusPlatformTask()
        }, Config.bonusPlatformSpawnInterval)
        currentDespawnTask = null
    }
}

@Serializable
data class Platform(val name: String, val coordinates: Coordinates, var active: Boolean = false)