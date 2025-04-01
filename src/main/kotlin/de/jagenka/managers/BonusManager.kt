package de.jagenka.managers

import de.jagenka.*
import de.jagenka.config.Config
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.math.GlobalPos
import java.util.*
import kotlin.jvm.optionals.getOrNull
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

    val bonusCompass: ItemStack
        get()
        {
            val compass = Items.COMPASS.defaultStack

            val customCompassNbt = NbtCompound()
            customCompassNbt.putBoolean("isDGBonusTracker", true)
            compass.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customCompassNbt))

            return compass
        }

    fun updateAllCompasses()
    {
        PlayerManager.getOnlineParticipatingPlayers().forEach { player ->
            val compassesToGive = (selectedPlatforms.size -
                    player.inventory.combinedInventory()
                        .count {
                            it.item == Items.COMPASS &&
                                    it.get(DataComponentTypes.CUSTOM_DATA)?.nbt?.getBoolean("isDGBonusTracker")?.getOrNull() == true
                        }).coerceAtLeast(0)

            val emptyHotbarSlots = player.inventory.main.subList(0, 9).mapIndexed { index, itemStack -> if (itemStack.isEmpty) index else -1 }.filter { it >= 0 }
            val fitInHotbar = emptyHotbarSlots.size
            repeat(fitInHotbar.coerceAtMost(compassesToGive)) { player.inventory.setStack(emptyHotbarSlots.reversed()[it], bonusCompass) }
            repeat((compassesToGive - fitInHotbar).coerceAtLeast(0)) { player.giveItemStack(bonusCompass) }

            player.inventory.combinedInventory()
                .filter {
                    it.item == Items.COMPASS &&
                            it.get(DataComponentTypes.CUSTOM_DATA)?.nbt?.getBoolean("isDGBonusTracker")?.getOrNull() == true
                }
                .forEachIndexed { index, stackInInventory ->
                    if (index !in selectedPlatforms.indices) return@forEachIndexed

                    val platform = selectedPlatforms[index]

                    stackInInventory.setCustomName(Text.of(platform.toString().trim()))

                    val lodestoneTrackerComponent =
                        LodestoneTrackerComponent(Optional.of(GlobalPos.create(player.world.registryKey, platform.pos.asMinecraftBlockPos())), true)
                    stackInInventory.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent)
                }
        }
    }
}

@Serializable
data class Platform(val name: String, val pos: BlockPos)
{
    override fun toString() = "$name $pos"
}