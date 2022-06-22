package de.jagenka

import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TextColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.world.GameRules
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun log(message: String)
{
    println(message)
}

object Util
{
    var minecraftServer: MinecraftServer? = null
        private set

    val modUUID: UUID = UUID.randomUUID()

    @JvmStatic
    fun onServerLoaded(minecraftServer: MinecraftServer)
    {
        this.minecraftServer = minecraftServer

        ServerLifecycleEvents.SERVER_STARTED

        ifServerLoaded { server ->
            Config.lateLoadConfig()

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
            server.overworld.setWeather(Int.MAX_VALUE, 0, false, false)
            server.overworld.timeOfDay = 6000 // noon
        }

        PlayerManager.prepareTeams()

        DisplayManager.reset()
        DisplayManager.prepareTeams()
    }

    fun ifServerLoaded(lambda: (MinecraftServer) -> Unit)
    {
        minecraftServer?.let { lambda(it) }
            ?: log("Minecraft Server not yet initialized")
    }

    fun ServerPlayerEntity.teleport(coordinates: Coordinates)
    {
        val (x, y, z, yaw, pitch) = coordinates
        this.teleport(server.overworld, x.toCenter(), y.toDouble(), z.toCenter(), yaw, pitch)
    }

    fun ServerPlayerEntity.teleport(vec3d: Vec3d, yaw: Float, pitch: Float) = this.teleport(server.overworld, vec3d.x, vec3d.y, vec3d.z, yaw, pitch)

    fun setBlockAt(coordinates: Coordinates, block: Block)
    {
        val (x, y, z) = coordinates
        ifServerLoaded { it.overworld.setBlockState(BlockPos(x, y, z), block.defaultState) }
    }

    fun setBlockAt(x: Double, y: Double, z: Double, block: Block)
    {
        setBlockAt(Coordinates(x, y, z), block)
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block)
    {
        setBlockAt(x.toDouble(), y.toDouble(), z.toDouble(), block)
    }

    fun getBlockAt(coordinates: Coordinates): Block
    {
        val (x, y, z) = coordinates
        var block = Blocks.AIR // default
        ifServerLoaded { block = it.overworld.getBlockState(BlockPos(x, y, z)).block }
        return block
    }

    fun getBlockAt(x: Double, y: Double, z: Double) = getBlockAt(Coordinates(x, y, z))
    fun getBlockAt(x: Int, y: Int, z: Int) = getBlockAt(x.toDouble(), y.toDouble(), z.toDouble())

    fun getBlocksInCubeRadius(coordinates: Coordinates, radius: Int): List<BlockAtCoordinates>
    {
        val result = mutableListOf<BlockAtCoordinates>()

        for (dy in -radius..radius)
        {
            result.addAll(getBlocksInSquareRadiusAtFixY(coordinates.relative(0, dy, 0), radius))
        }


        return result.toList()
    }

    fun getBlocksInSquareRadiusAtFixY(coordinates: Coordinates, radius: Int): List<BlockAtCoordinates>
    {
        val result = mutableListOf<BlockAtCoordinates>()

        val (centerX, centerY, centerZ) = coordinates

        for (x in centerX - radius..centerX + radius)
        {
            for (z in centerZ - radius..centerZ + radius)
            {
                result.add(BlockAtCoordinates(getBlockAt(x, centerY, z), Coordinates(x, centerY, z)))
            }
        }

        return result.toList()
    }

    fun getIntTextColor(r: Int, g: Int, b: Int): Int = (r shl 16) or (g shl 8) or (b)
    fun getTextColor(r: Int, g: Int, b: Int) = TextColor.fromRgb(getIntTextColor(r, g, b))
}

data class BlockAtCoordinates(val block: Block, val coordinates: Coordinates)

@Serializable
class BlockCuboid
{
    val firstCorner: Coordinates
    val secondCorner: Coordinates

    constructor(firstCorner: Coordinates, secondCorner: Coordinates)
    {
        this.firstCorner = Coordinates(min(firstCorner.x, secondCorner.x), min(firstCorner.y, secondCorner.y), min(firstCorner.z, secondCorner.z))
        this.secondCorner = Coordinates(max(firstCorner.x, secondCorner.x), max(firstCorner.y, secondCorner.y), max(firstCorner.z, secondCorner.z))
    }

    fun contains(coordinates: Coordinates): Boolean
    {
        return (coordinates.x in firstCorner.x..secondCorner.x) && (coordinates.y in firstCorner.y..secondCorner.y) && (coordinates.z in firstCorner.z..secondCorner.z)
    }

    fun contains(pos: Vec3d): Boolean
    {
        return (pos.x in firstCorner.x.toFloat().rangeTo((secondCorner.x + 1).toFloat()))
                && (pos.y in firstCorner.y.toFloat().rangeTo((secondCorner.y + 1).toFloat()))
                && (pos.z in firstCorner.z.toFloat().rangeTo((secondCorner.z + 1).toFloat()))
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockCuboid

        if (firstCorner != other.firstCorner) return false
        if (secondCorner != other.secondCorner) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = firstCorner.hashCode()
        result = 31 * result + secondCorner.hashCode()
        return result
    }

    override fun toString() = "BlockCuboid(" + listOf(firstCorner, secondCorner).joinToString(", ") { it.toString() } + ")"

}

// this is needed so the config command transformer can correctly deduce the non generic type of the list
@Serializable
class CoordinateList(val coords: List<Coordinates>)
{
    override fun toString() = "[" + coords.joinToString(", ") { it.toString() } + "]"
}

fun Double.floor() = floor(this).toInt()

fun Double.toRadians(): Double = (this / 180.0) * Math.PI

fun Double.toDegree(): Double = (this * 180.0) / Math.PI

fun Float.toRadians(): Float = (this / 180f) * Math.PI.toFloat()

fun Float.toDegree(): Float = (this * 180f) / Math.PI.toFloat()

fun Vec3d.pureQuarternion(): Quaternion = Quaternion(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), 0f)

fun Vec3d.rotateAroundVector(axis: Vec3d, degrees: Float): Vec3d
{
    val rotationQuaternion = Quaternion(Vec3f(axis), degrees, true)
    val vectorQuaternion = this.pureQuarternion()
    val finalQuaternion = rotationQuaternion.copy()

    finalQuaternion.hamiltonProduct(vectorQuaternion)
    rotationQuaternion.conjugate()
    finalQuaternion.hamiltonProduct(rotationQuaternion)

    return Vec3d(finalQuaternion.x.toDouble(), finalQuaternion.y.toDouble(), finalQuaternion.z.toDouble())
}

infix fun Block.isSame(block: Block) = this.lootTableId == block.lootTableId