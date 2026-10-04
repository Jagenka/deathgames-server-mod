package de.jagenka.managers

import de.jagenka.Util
import de.jagenka.combinedInventory
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.setCustomName
import de.jagenka.util.BlockPosSerializer
import de.jagenka.util.surroundingBlockPos
import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.component.DataComponents.CUSTOM_DATA
import net.minecraft.core.component.DataComponents.LODESTONE_TRACKER
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.LodestoneTracker
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import java.util.*
import kotlin.jvm.optionals.getOrNull

object BonusManager
{
    private val platforms
        get() = Config.bonus.platforms
    private val selectedPlatforms = mutableListOf<Platform>()

    private val activePlatforms = mutableMapOf<Platform, Boolean>().withDefault { false }

    val inactiveBlock: Block = Blocks.CONCRETE.red
    val activeBlock: Block = Blocks.CONCRETE.lime

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

    fun getSelectedPlatforms() = selectedPlatforms.toList()

    fun isOnActivePlatform(playerName: String): Boolean = getActivePlatforms().any {
        val player = PlayerManager.getOnlinePlayer(playerName) ?: return false
        it.containsPlayer(player)
    }

    private fun colorPlatforms()
    {
        platforms.forEach { platform ->
            Util.getBlocksInSquareRadiusAtFixY(platform.pos, Config.bonus.radius).forEach { (block, coordinates) ->
                if (block isSame inactiveBlock || block isSame activeBlock)
                {
                    Util.setBlockAt(
                        PlayerManager.getMapLevel(),
                        coordinates,
                        if (platform.isActive()) activeBlock else inactiveBlock
                    )
                }
            }
        }
    }

    fun Platform.isActive() = activePlatforms.getValue(this)

    val bonusCompass: ItemStack
        get()
        {
            val compass = Items.COMPASS.defaultInstance

            val customCompassNbt = CompoundTag()
            customCompassNbt.putBoolean("isDGBonusTracker", true)
            compass.set(CUSTOM_DATA, CustomData.of(customCompassNbt))

            return compass
        }

    fun updateAllCompasses()
    {
        PlayerManager.getOnlineParticipatingPlayers().forEach { player ->
            val compassesToGive = (selectedPlatforms.size -
                    player.inventory.combinedInventory()
                        .count {
                            it.item == Items.COMPASS &&
                                    it.get(CUSTOM_DATA)?.tag?.getBoolean("isDGBonusTracker")?.getOrNull() == true
                        }).coerceAtLeast(0)

            val emptyHotbarSlots = player.inventory.items.subList(0, 9)
                .mapIndexed { index, itemStack -> if (itemStack.isEmpty) index else -1 }.filter { it >= 0 }
            val fitInHotbar = emptyHotbarSlots.size
            repeat(fitInHotbar.coerceAtMost(compassesToGive)) {
                player.inventory.setItem(
                    emptyHotbarSlots.reversed()[it],
                    bonusCompass
                )
            }
            repeat((compassesToGive - fitInHotbar).coerceAtLeast(0)) { player.addItem(bonusCompass) }

            player.inventory.combinedInventory()
                .filter {
                    it.item == Items.COMPASS &&
                            it.get(CUSTOM_DATA)?.tag?.getBoolean("isDGBonusTracker")?.getOrNull() == true
                }
                .forEachIndexed { index, stackInInventory ->
                    if (index !in selectedPlatforms.indices) return@forEachIndexed

                    val platform = selectedPlatforms[index]

                    stackInInventory.setCustomName(Component.literal(platform.toString().trim()))

                    val lodestoneTrackerComponent =
                        LodestoneTracker(
                            Optional.of(
                                GlobalPos.of(
                                    player.level().dimension(),
                                    platform.pos
                                )
                            ), true
                        )
                    stackInInventory.set(LODESTONE_TRACKER, lodestoneTrackerComponent)
                }
        }
    }
}

@Serializable
data class Platform(val name: String, @Serializable(with = BlockPosSerializer::class) val pos: BlockPos)
{
    fun getBoundingBox(): BoundingBox = BoundingBox.fromCorners(
        pos.offset(-Config.bonus.radius, 0, -Config.bonus.radius),
        pos.offset(Config.bonus.radius, 2, Config.bonus.radius)
    )

    fun containsPlayer(player: ServerPlayer) = getBoundingBox().isInside(player.position().surroundingBlockPos()) // TODO: does this work?

    override fun toString() = "$name $pos"
}