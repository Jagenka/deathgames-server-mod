package de.jagenka

import com.mojang.brigadier.StringReader
import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.Platform
import de.jagenka.managers.PlayerManager
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.command.argument.ItemStringReader
import net.minecraft.component.ComponentMap
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import org.joml.AxisAngle4f
import org.joml.Quaternionf
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

    private val itemStringReader = ItemStringReader(DeathGames.commandRegistryAccess)

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
            server.scoreboard.teams.toList().forEach { team -> server.scoreboard.removeTeam(team) }

            server.gameRules[GameRules.SPECTATORS_GENERATE_CHUNKS].set(false, server)
            server.gameRules[GameRules.DO_MOB_SPAWNING].set(false, server)
            server.gameRules[GameRules.DO_MOB_GRIEFING].set(false, server)
            server.gameRules[GameRules.DO_PATROL_SPAWNING].set(false, server)
            server.gameRules[GameRules.DO_TRADER_SPAWNING].set(false, server)
            server.gameRules[GameRules.DO_WARDEN_SPAWNING].set(false, server)
            server.gameRules[GameRules.ANNOUNCE_ADVANCEMENTS].set(false, server)
            server.gameRules[GameRules.KEEP_INVENTORY].set(true, server)
            server.gameRules[GameRules.DO_DAYLIGHT_CYCLE].set(false, server)
            server.gameRules[GameRules.DO_WEATHER_CYCLE].set(false, server)
            server.gameRules[GameRules.LOCATOR_BAR].set(false, server)
            server.overworld.setWeather(Int.MAX_VALUE, 0, false, false)
            server.overworld.timeOfDay = 6000 // noon
            server.setDifficulty(Difficulty.NORMAL, false)

            PlayerManager.getOnlinePlayers().forEach { player ->
                player.lockRecipes(server.recipeManager.values())
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

    fun ServerPlayerEntity.teleport(coordinates: Coordinates?)
    {
        if (coordinates == null) return
        val (x, y, z, yaw, pitch) = coordinates
        // new in 1.21.3: PositionFlags if relative tp and resetCamera (why not?)
        // new in 0.10.0-1.21.8: no longer teleporting to overworld, as map could be in another dimension
        this.teleport(world, x.toCenter(), y.toDouble(), z.toCenter(), emptySet<PositionFlag>(), yaw, pitch, true)
    }

    fun ServerPlayerEntity.teleport(vec3d: Vec3d, yaw: Float, pitch: Float): Boolean
    {
        // new in 1.21.3: PositionFlags if relative tp and resetCamera (why not?)
        // new in 0.10.0-1.21.8: no longer teleporting to overworld, as map could be in another dimension
        return this.teleport(world, vec3d.x, vec3d.y, vec3d.z, emptySet<PositionFlag>(), yaw, pitch, true)
    }

    fun setBlockAt(pos: BlockPos, block: Block)
    {
        ifServerLoaded { it.overworld.setBlockState(pos.asMinecraftBlockPos(), block.defaultState) }
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block)
    {
        setBlockAt(BlockPos(x, y, z), block)
    }

    fun getBlockAt(x: Int, y: Int, z: Int) = getBlockAt(BlockPos(x, y, z))
    fun getBlockAt(pos: BlockPos): Block
    {
        var block = Blocks.AIR // default
        ifServerLoaded { block = it.overworld.getBlockState(pos.asMinecraftBlockPos()).block }
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
        val itemResult = itemStringReader.consume(StringReader(id + nbt))
        val itemStack = ItemStack(itemResult.item, amount)
        itemStack.applyUnvalidatedChanges(itemResult.components)
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

operator fun Vec3d.plus(other: Vec3d): Vec3d = this.add(other)

operator fun Vec3d.minus(other: Vec3d): Vec3d = this.subtract(other)

operator fun Vec3d.times(factor: Double): Vec3d = this.multiply(factor)

fun Vec3d.pureQuarternion(): Quaternionf = Quaternionf(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), 0f)

fun Vec3d.rotateAroundVector(axis: Vector3f, degrees: Float): Vec3d
{
    val rotationQuaternion = Quaternionf(AxisAngle4f(degrees.toRadians(), axis.x, axis.y, axis.z))
    val vectorQuaternion = this.pureQuarternion()
    val finalQuaternion = Quaternionf(rotationQuaternion)

    finalQuaternion.mul(vectorQuaternion)
    rotationQuaternion.conjugate()
    finalQuaternion.mul(rotationQuaternion)

    return Vec3d(finalQuaternion.x.toDouble(), finalQuaternion.y.toDouble(), finalQuaternion.z.toDouble())
}

infix fun Block.isSame(block: Block) = this.translationKey == block.translationKey

fun PlayerInventory.combinedInventory() =
    main +
            equipment.get(EquipmentSlot.OFFHAND) +
            equipment.get(EquipmentSlot.HEAD) +
            equipment.get(EquipmentSlot.BODY) +
            equipment.get(EquipmentSlot.LEGS) +
            equipment.get(EquipmentSlot.FEET)

/**
 * custom port of previously existing function
 * sets custom name of given ItemStack and returns itself (redundant but like original implementation)
 */
fun ItemStack.setCustomName(text: Text): ItemStack
{
    this.set(DataComponentTypes.CUSTOM_NAME, text)
    return this
}

fun itemAndNbtEqual(itemStack1: ItemStack, itemStack2: ItemStack): Boolean
{
    return itemStack1.item == itemStack2.item &&
            itemStack1.components == itemStack2.components
}

fun ItemStack.withDamage(damage: Int): ItemStack
{
    this.damage = damage
    return this
}

val Item.maxDamage
    get() = (this.components.get(DataComponentTypes.MAX_DAMAGE) ?: 0)

fun Item.isArmor(): Boolean
{
    return (this.components.get(DataComponentTypes.EQUIPPABLE)?.slot ?: return false) in
            listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
}

val Item.equipmentSlot: EquipmentSlot?
    get() = this.components.get(DataComponentTypes.EQUIPPABLE)?.slot

fun PlayerInventory.removeItemStack(stackToRemove: ItemStack, maxCount: Int = -1): Int
{
    return this.remove({ itemStackInInventory ->
        ItemStack.areEqual(stackToRemove, itemStackInInventory)
    }, maxCount, player!!.playerScreenHandler.craftingInput) // should be null-safe, because a player inventory without a player should be an illegal state
}

/**
 * I will leave this here, because it took me 30 minutes to figure this out haha
 */
private fun ItemStack.makeUnbreakable(): ItemStack
{
    val componentType = DataComponentTypes.UNBREAKABLE
    this.applyComponentsFrom(ComponentMap.builder().add(componentType, net.minecraft.util.Unit.INSTANCE).build())  // show in tooltip
    return this
}