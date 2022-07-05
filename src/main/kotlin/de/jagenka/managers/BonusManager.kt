package de.jagenka.managers

import de.jagenka.BlockPos
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
import kotlin.math.abs

object BonusManager
{
    val platforms
        get() = Config.configEntry.bonus.platforms.plats
    val selectedPlatforms = mutableListOf<Platform>()

    val activePlatforms = mutableMapOf<Platform, Boolean>().withDefault { false }

    val inactiveBlock = Blocks.RED_CONCRETE
    val activeBlock = Blocks.LIME_CONCRETE

    private var currentSpawnTask: ScheduledTask? = null
    private var currentDespawnTask: ScheduledTask? = null

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
        selectedPlatforms.forEach { activePlatforms[it] = true }
        colorPlatforms()
    }

    @Deprecated("use queueRandomPlatforms and activateSelectedPlatforms instead", ReplaceWith("", ""), DeprecationLevel.WARNING)
    fun activateRandomPlatforms(howMany: Int)
    {
        queueRandomPlatforms(howMany)
        activateSelectedPlatforms()
    }

    fun disableAllPlatforms()
    {
        platforms.forEach { activePlatforms.clear() }
        colorPlatforms()
    }

    fun getActivePlatforms() = activePlatforms.keys.filter { activePlatforms.getValue(it) == true }

    fun isOnActivePlatform(playerName: String) = getActivePlatforms().any {
        val player = PlayerManager.getOnlinePlayer(playerName) ?: return false
        val dx = abs(it.pos.x.toCenter() - player.pos.x)
        val dy = abs(it.pos.y.toDouble() - player.pos.y)
        val dz = abs(it.pos.z.toCenter() - player.pos.z)
        dy < 2 && dx <= bonusPlatformRadius + 0.5 && dz <= bonusPlatformRadius + 0.5
    }

    private fun colorPlatforms()
    {
        platforms.forEach { platform ->
            Util.getBlocksInSquareRadiusAtFixY(platform.pos, bonusPlatformRadius).forEach { (block, coordinates) ->
                if (block isSame inactiveBlock || block isSame activeBlock)
                {
                    Util.setBlockAt(coordinates, if (platform.isActive()) activeBlock else inactiveBlock)
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

    fun Platform.isActive() = activePlatforms.getValue(this)
}

@Serializable
data class Platform(val name: String, val pos: BlockPos)
{
    override fun toString() = "$name,$pos"
}