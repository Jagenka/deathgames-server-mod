package de.jagenka

import com.mojang.brigadier.StringReader
import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.util.component1
import de.jagenka.util.component2
import de.jagenka.util.component3
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4d
import org.joml.Quaterniond
import org.joml.Vector3f
import java.util.*
import kotlin.math.floor

object Util
{
    var minecraftServer: MinecraftServer? = null
        private set

    private val itemArgument = ItemArgument(DeathGames.commandBuildContext)

    @JvmStatic
    fun onServerLoaded(minecraftServer: MinecraftServer)
    {
        this.minecraftServer = minecraftServer

        ifServerLoaded {
            Config.lateLoadConfig()
        }

        if (!isEnabled) return

        this.minecraftServer?.let { server ->
            server.scoreboard.playerTeams.toList().forEach { team -> server.scoreboard.removePlayerTeam(team) }

            server.gameRules.set(GameRules.SPECTATORS_GENERATE_CHUNKS, true, server)
            server.gameRules.set(GameRules.SPAWN_MOBS, false, server)
            server.gameRules.set(GameRules.MOB_GRIEFING, false, server)
            server.gameRules.set(GameRules.SPAWN_PATROLS, false, server)
            server.gameRules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server)
            server.gameRules.set(GameRules.SPAWN_WARDENS, false, server)
            server.gameRules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server)
            server.gameRules.set(GameRules.KEEP_INVENTORY, true, server)
            server.gameRules.set(GameRules.ADVANCE_TIME, false, server)
            server.gameRules.set(GameRules.ADVANCE_WEATHER, false, server)
            server.gameRules.set(GameRules.LOCATOR_BAR, false, server)

            server.setWeatherParameters(Int.MAX_VALUE, 0, false, false)
            server.setDifficulty(Difficulty.NORMAL, false)

            PlayerManager.getOnlinePlayers().forEach { player ->
                player.resetRecipes(server.recipeManager.recipes)
            }
        }

        PlayerManager.prepareTeams()

        DisplayManager.prepareTeams()
    }

    fun ifServerLoaded(lambda: (MinecraftServer) -> Unit)
    {
        minecraftServer?.let { lambda(it) }
            ?: DeathGames.logger.error("Minecraft Server not yet initialized")
    }

    fun setBlockAt(level: Level, pos: BlockPos, block: Block)
    {
        level.setBlockAndUpdate(pos, block.defaultBlockState())
    }

    fun getBlockAt(x: Int, y: Int, z: Int) = getBlockAt(BlockPos(x, y, z))
    fun getBlockAt(pos: BlockPos): Block
    {
        var block = Blocks.AIR // default
        ifServerLoaded { block = it.overworld().getBlockState(pos).block }
        return block
    }

    fun getBlocksInSquareRadiusAtFixY(pos: BlockPos, radius: Int): Iterable<BlockAtPos>
    {
        return Iterable {
            iterator {
                val (centerX, centerY, centerZ) = pos
                for (x in centerX - radius..centerX + radius)
                {
                    for (z in centerZ - radius..centerZ + radius)
                    {
                        yield(BlockAtPos(getBlockAt(x, centerY, z), BlockPos(x, centerY, z)))
                    }
                }
            }
        }
    }

    fun getRGBTripleForInt(rgb: Int): Triple<Int, Int, Int> = Triple((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
    fun getRGBVector3fForInt(rgb: Int): Vector3f
    {
        val (r, g, b) = getRGBTripleForInt(rgb)
        return Vector3f(r.toFloat() / 0xFF.toFloat(), g.toFloat() / 0xFF.toFloat(), b.toFloat() / 0xFF.toFloat())
    }

    /**
     * converts individual RGB values to one int
     * @param r red value with 0 <= r < 255/FF
     * @param g green value with 0 <= g < 255/FF
     * @param b blue value with 0 <= b < 255/FF
     */
    fun getRGBInt(r: Int, g: Int, b: Int): Int = (r shl 16) or (g shl 8) or (b)
    fun getTextColor(r: Int, g: Int, b: Int): TextColor = TextColor.fromRgb(getRGBInt(r, g, b))


    fun parseItemStack(id: String, nbt: String, amount: Int): ItemStack
    {
        val itemResult = itemArgument.parse(StringReader(id + nbt))
        val itemStack = itemResult.createItemStack(amount)
        return itemStack
    }
}

data class BlockAtPos(val block: Block, val pos: BlockPos)

fun Double.floor() = floor(this).toInt()

fun Double.toRadians(): Double = (this / 180.0) * Math.PI

fun Double.toDegree(): Double = (this * 180.0) / Math.PI

fun Float.toRadians(): Float = (this / 180f) * Math.PI.toFloat()

fun Float.toDegree(): Float = (this * 180f) / Math.PI.toFloat()

operator fun Vec3.plus(other: Vec3): Vec3 = this.add(other)

operator fun Vec3.minus(other: Vec3): Vec3 = this.subtract(other)

operator fun Vec3.times(factor: Double): Vec3 = this.scale(factor)

fun Vec3i.toCenterPos(): Vec3 = Vec3(this.x + .5, this.y.toDouble(), this.z + .5)

fun Vec3.pureQuaternion(): Quaterniond = Quaterniond(this.x, this.y, this.z, 0.0)

fun Vec3.rotateAroundVector(axis: Vec3, degrees: Double): Vec3
{
    val rotationQuaternion = Quaterniond(AxisAngle4d(degrees.toRadians(), axis.x, axis.y, axis.z))
    val vectorQuaternion = this.pureQuaternion()
    val finalQuaternion = Quaterniond(rotationQuaternion)

    finalQuaternion.mul(vectorQuaternion)
    rotationQuaternion.conjugate()
    finalQuaternion.mul(rotationQuaternion)

    return Vec3(finalQuaternion.x, finalQuaternion.y, finalQuaternion.z)
}

infix fun Block.isSame(block: Block) = this.descriptionId == block.descriptionId

fun Inventory.combinedInventory() =
    items +
            equipment.get(EquipmentSlot.OFFHAND) +
            equipment.get(EquipmentSlot.HEAD) +
            equipment.get(EquipmentSlot.BODY) +
            equipment.get(EquipmentSlot.LEGS) +
            equipment.get(EquipmentSlot.FEET)

/**
 * custom port of previously existing function
 * sets custom name of given ItemStack and returns itself (redundant but like original implementation)
 */
fun ItemStack.setCustomName(textComponent: Component): ItemStack
{
    this.set(DataComponents.CUSTOM_NAME, textComponent)
    return this
}

fun itemAndNbtEqual(itemStack1: ItemStack, itemStack2: ItemStack): Boolean
{
    return itemStack1.item == itemStack2.item &&
            itemStack1.components == itemStack2.components
}

fun ItemStack.withDamage(damage: Int): ItemStack
{
    this.damageValue = damage
    return this
}

val Item.maxDamage
    get() = (this.components().get(DataComponents.MAX_DAMAGE) ?: 0)

fun Item.isArmor(): Boolean
{
    return (this.components().get(DataComponents.EQUIPPABLE)?.slot ?: return false) in
            listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
}

val Item.equipmentSlot: EquipmentSlot?
    get() = this.components().get(DataComponents.EQUIPPABLE)?.slot

fun Inventory.removeItemStack(stackToRemove: ItemStack, maxCount: Int = -1): Int
{
    return this.clearOrCountMatchingItems({ itemStackInInventory ->
        ItemStack.isSameItemSameComponents(stackToRemove, itemStackInInventory)
    }, false, maxCount, player.inventoryMenu.craftSlots) // also look in craftSlots for removal
}

fun <T : Any> T?.asOptional(): Optional<T> = Optional.ofNullable(this)