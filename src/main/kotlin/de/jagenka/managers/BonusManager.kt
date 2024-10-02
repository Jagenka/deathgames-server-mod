package de.jagenka.managers

import de.jagenka.BlockPos
import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.toCenter
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import kotlin.math.abs

object BonusManager
{
    val platforms
        get() = Config.bonus.platforms.plats
    val selectedPlatforms = mutableListOf<Platform>()

    val activePlatforms = mutableMapOf<Platform, Boolean>().withDefault { false }

    val inactiveBlock: Block = Blocks.RED_CONCRETE
    val activeBlock: Block = Blocks.LIME_CONCRETE

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
        activePlatforms.clear()
        colorPlatforms()
    }

    fun getActivePlatforms() = activePlatforms.keys.filter { it.isActive() }

    fun isOnActivePlatform(playerName: String) = getActivePlatforms().any {
        val player = PlayerManager.getOnlinePlayer(playerName) ?: return false
        val dx = abs(it.pos.x.toCenter() - player.pos.x)
        val dy = abs(it.pos.y.toDouble() - player.pos.y)
        val dz = abs(it.pos.z.toCenter() - player.pos.z)
        dy < 2 && dx <= Config.bonus.radius + 0.5 && dz <= Config.bonus.radius + 0.5
    }

    private fun colorPlatforms()
    {
        platforms.forEach { platform ->
            Util.getBlocksInSquareRadiusAtFixY(platform.pos, Config.bonus.radius).forEach { (block, coordinates) ->
                if (block isSame inactiveBlock || block isSame activeBlock)
                {
                    Util.setBlockAt(coordinates, if (platform.isActive()) activeBlock else inactiveBlock)
                }
            }
        }
    }

    fun Platform.isActive() = activePlatforms.getValue(this)
}

@Serializable
data class Platform(val name: String, val pos: BlockPos)
{
    override fun toString() = "$name,$pos"
}