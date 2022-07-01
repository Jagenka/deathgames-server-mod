package de.jagenka

import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.Platform
import de.jagenka.managers.PlayerManager
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TextColor
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import java.util.*
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
            server.overworld.setWeather(Int.MAX_VALUE, 0, false, false)
            server.overworld.timeOfDay = 6000 // noon
            server.setDifficulty(Difficulty.NORMAL, false)
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

    fun getIntTextColor(r: Int, g: Int, b: Int): Int = (r shl 16) or (g shl 8) or (b)
    fun getTextColor(r: Int, g: Int, b: Int) = TextColor.fromRgb(getIntTextColor(r, g, b))

    val coordinatePattern =
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