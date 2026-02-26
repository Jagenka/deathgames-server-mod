package de.jagenka

import com.mojang.brigadier.StringReader
import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.Platform
import de.jagenka.managers.PlayerManager
import kotlinx.serialization.Serializable
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.Vec3i
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Relative
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4d
import org.joml.Quaterniond
import org.joml.Vector3f
import java.util.regex.Pattern
import kotlin.math.floor

fun log(message: String)
{
    println(message)
}

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

        initOnServerStart()
    }

    fun initOnServerStart()
    {
        this.minecraftServer?.let { server ->
            server.scoreboard.playerTeams.toList().forEach { team -> server.scoreboard.removePlayerTeam(team) }

            server.worldData.gameRules.set(GameRules.SPECTATORS_GENERATE_CHUNKS, false, server)
            server.worldData.gameRules.set(GameRules.SPAWN_MOBS, false, server)
            server.worldData.gameRules.set(GameRules.MOB_GRIEFING, false, server)
            server.worldData.gameRules.set(GameRules.SPAWN_PATROLS, false, server)
            server.worldData.gameRules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server)
            server.worldData.gameRules.set(GameRules.SPAWN_WARDENS, false, server)
            server.worldData.gameRules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server)
            server.worldData.gameRules.set(GameRules.KEEP_INVENTORY, true, server)
            server.worldData.gameRules.set(GameRules.ADVANCE_TIME, false, server)
            server.worldData.gameRules.set(GameRules.ADVANCE_WEATHER, false, server)
            server.worldData.gameRules.set(GameRules.LOCATOR_BAR, false, server)

            server.overworld().setWeatherParameters(Int.MAX_VALUE, 0, false, false)
            server.overworld().dayTime = 6000 // noon
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
            ?: log("Minecraft Server not yet initialized")
    }

    fun ServerPlayer.teleport(coordinates: Coordinates?)
    {
        if (coordinates == null) return
        val (x, y, z, yaw, pitch) = coordinates

        this.teleportTo(level(), x.toCenter(), y.toDouble(), z.toCenter(), emptySet<Relative>(), yaw, pitch, true)
    }

    fun ServerPlayer.teleport(vec3: Vec3, yaw: Float, pitch: Float): Boolean
    {
        // new in 1.21.3: PositionFlags if relative tp and resetCamera (why not?)
        // new in 0.10.0-1.21.8: no longer teleporting to overworld, as map could be in another dimension
        return this.teleportTo(level(), vec3.x, vec3.y, vec3.z, emptySet<Relative>(), yaw, pitch, true)
    }

    fun setBlockAt(pos: BlockPos, block: Block)
    {
        ifServerLoaded { it.overworld().setBlock(pos.asMinecraftBlockPos(), block.defaultBlockState(), 0) }
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block)
    {
        setBlockAt(BlockPos(x, y, z), block)
    }

    fun getBlockAt(x: Int, y: Int, z: Int) = getBlockAt(BlockPos(x, y, z))
    fun getBlockAt(pos: BlockPos): Block
    {
        var block = Blocks.AIR // default
        ifServerLoaded { block = it.overworld().getBlockState(pos.asMinecraftBlockPos()).block }
        return block
    }

    fun getBlocksInCubeRadius(pos: BlockPos, radius: Int): List<BlockAtPos>
    {
        val result = mutableListOf<BlockAtPos>()

        for (dy in -radius..radius)
        {
            result.addAll(getBlocksInSquareRadiusAtFixY(pos.relative(0, dy, 0), radius))
        }


        return result.toList()
    }

    fun getBlocksInSquareRadiusAtFixY(pos: BlockPos, radius: Int): List<BlockAtPos>
    {
        val result = mutableListOf<BlockAtPos>()

        val (centerX, centerY, centerZ) = pos

        for (x in centerX - radius..centerX + radius)
        {
            for (z in centerZ - radius..centerZ + radius)
            {
                result.add(BlockAtPos(getBlockAt(x, centerY, z), BlockPos(x, centerY, z)))
            }
        }

        return result.toList()
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

    val coordinatePattern: Pattern =
        Pattern.compile("\\((x\\s*=\\s*)?(\\d*\\.?\\d+)\\s*,\\s*(y\\s*=\\s*)?(\\d*\\.?\\d+)\\s*,\\s*(z\\s*=\\s*)?(\\d*\\.?\\d+)\\s*,\\s*(y\\s*=\\s*)?(\\d*\\.?\\d+)\\s*,\\s*(p\\s*=\\s*)?(\\d*\\.?\\d+)\\)")

    fun getCoordinateFromString(str: String): Coordinates?
    {
        val matcher = coordinatePattern.matcher(str)
        if (!matcher.matches())
        {
            return null
        }

        try
        {
            val coordinate = Coordinates(
                matcher.group(2).toDouble(),
                matcher.group(4).toDouble(),
                matcher.group(6).toDouble(),
                matcher.group(8).toFloat(),
                matcher.group(10).toFloat()
            )
            return coordinate
        } catch (e: NumberFormatException)
        {
            return null
        }
    }

    fun getBlockPosFromString(str: String): BlockPos?
    {
        val matcher = coordinatePattern.matcher(str)
        if (!matcher.matches())
        {
            return null
        }

        try
        {
            return BlockPos(
                matcher.group(2).toInt(),
                matcher.group(4).toInt(),
                matcher.group(6).toInt()
            )
        } catch (e: NumberFormatException)
        {
            return null
        }
    }

    fun getCoordinateListFromString(str: String): List<Coordinates>?
    {
        val individualStrings = str.split(";")

        try
        {
            return individualStrings.map { getCoordinateFromString(it) }.requireNoNulls().toList()
        } catch (e: IllegalArgumentException)
        {
            return null
        }
    }

    fun getBlockPosListFromString(str: String): List<BlockPos>?
    {
        val individualStrings = str.split(";")

        try
        {
            return individualStrings.map { getBlockPosFromString(it) }.requireNoNulls().toList()
        } catch (e: IllegalArgumentException)
        {
            return null
        }
    }

    fun parseItemStack(id: String, nbt: String, amount: Int): ItemStack
    {
        val itemResult = itemArgument.parse(StringReader(id + nbt))
        val itemStack = itemResult.createItemStack(amount, false)
        return itemStack
    }
}

data class BlockAtPos(val block: Block, val pos: BlockPos)

// this is needed so the config command transformer can correctly deduce the non generic type of the list
@Serializable
class CoordinateList(val coords: List<Coordinates>)
{
    override fun toString() = "[" + coords.joinToString(", ") { it.toString() } + "]"
}

@Serializable
class PlatformList(val plats: List<Platform>)
{
    override fun toString() = "[" + plats.joinToString(", ") { it.toString() } + "]"
}

fun Double.floor() = floor(this).toInt()

fun Double.toRadians(): Double = (this / 180.0) * Math.PI

fun Double.toDegree(): Double = (this * 180.0) / Math.PI

fun Float.toRadians(): Float = (this / 180f) * Math.PI.toFloat()

fun Float.toDegree(): Float = (this * 180f) / Math.PI.toFloat()

operator fun Vec3.plus(other: Vec3): Vec3 = this.add(other)

operator fun Vec3.minus(other: Vec3): Vec3 = this.subtract(other)

operator fun Vec3.times(factor: Double): Vec3 = this.scale(factor)

fun Vec3i.toCenterPos(): Vec3 = Vec3(this.x + .5, this.y.toDouble(), this.z + .5)

fun Vec3.pureQuarternion(): Quaterniond = Quaterniond(this.x, this.y, this.z, 0.0)

fun Vec3.rotateAroundVector(axis: Vec3, degrees: Double): Vec3
{
    val rotationQuaternion = Quaterniond(AxisAngle4d(degrees.toRadians(), axis.x, axis.y, axis.z))
    val vectorQuaternion = this.pureQuarternion()
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
        ItemStack.isSameItemSameComponents(stackToRemove, itemStackInInventory) // TODO: components equality wanted?
    }, maxCount, player.inventoryMenu.craftSlots) // also look in craftSlots for removal
}