package de.jagenka

import de.jagenka.commands.configPropertyTransformers
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
import java.util.regex.Pattern
import kotlin.math.floor

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

    // TODO: extract this to different file

    val complexConfigOptionPatterns = listOf<ConfigOptionPattern<*>>(
        ConfigOptionPattern(
            Coordinates::class.java,
            Pattern.compile("(Coord)?\\((x\\s*=\\s*)?(\\d+)\\s*,\\s*(y\\s*=\\s*)?(\\d+)\\s*,\\s*(z\\s*=\\s*)?(\\d+)\\s*,\\s*(y\\s*=\\s*)?(\\d*\\.?\\d+)\\s*,\\s*(p\\s*=\\s*)?(\\d*\\.?\\d+)\\)"),
            listOf<Pair<Int, Class<*>>>(
                3 to Int::class.java,
                5 to Int::class.java,
                7 to Int::class.java,
                9 to Float::class.java,
                11 to Float::class.java
            )
        ),
        ConfigOptionPattern(
            Coordinates::class.java,
            Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d*\\.?\\d+)\\s+(\\d*\\.?\\d+)\\s*"),
            listOf<Pair<Int, Class<*>>>(
                3 to Int::class.java,
                1 to Int::class.java,
                2 to Int::class.java,
                3 to Float::class.java,
                4 to Float::class.java
            )
        ),
        ConfigOptionPattern(
            BlockPos::class.java,
            Pattern.compile("(Coord)?\\((x\\s*=\\s*)?(\\d+)\\s*,\\s*(y\\s*=\\s*)?(\\d+)\\s*,\\s*(z\\s*=\\s*)?(\\d+)\\s*\\)"),
            listOf<Pair<Int, Class<*>>>(
                3 to Int::class.java,
                5 to Int::class.java,
                7 to Int::class.java,
            )
        ),
        ConfigOptionPattern(
            BlockPos::class.java,
            Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d*\\.?\\d+)\\s+(\\d*\\.?\\d+)\\s*"),
            listOf<Pair<Int, Class<*>>>(
                1 to Int::class.java,
                2 to Int::class.java,
                3 to Int::class.java,
            )
        )
    )

    data class ConfigOptionPattern<T>(val returnType: Class<T>, val pattern: Pattern, val parameters: List<Pair<Int, Class<*>>>)

    fun <T> getComplexConfigOptionFromString(str: String, desiredType: Class<T>): T?
    {
        complexConfigOptionPatterns.forEach { configOptionPattern ->
            val matcher = configOptionPattern.pattern.matcher(str)
            if (!matcher.matches())
            {
                return@forEach
            }

            val stringParametersToType = configOptionPattern.parameters.map { matcher.group(it.first) to it.second }

            val parameters: List<*> = stringParametersToType.map { (str, type) ->
                val simpleTransformer = configPropertyTransformers.entries.find { it.key == type }?.value ?: return@getComplexConfigOptionFromString null
                return@map simpleTransformer.fromString(str, null, null)
            }

            if(parameters.any { it == null }) {
                return@getComplexConfigOptionFromString null
            }

            val constructor = configOptionPattern.returnType::class.java.getDeclaredConstructor(*(configOptionPattern.parameters.map { it.second }.toTypedArray())) ?: return@getComplexConfigOptionFromString null

            val newObject = constructor.newInstance(*parameters.toTypedArray()) as T

            return@getComplexConfigOptionFromString newObject
        }

        return null
    }

    fun <T> getConfigOptionListFromString(str: String, type: Class<T>): List<T>?
    {
        val individualStrings = str.split(if(str.contains(";")) ";" else " ")

        try
        {
            return individualStrings.map { getComplexConfigOptionFromString(it, type) }.requireNoNulls().toList()
        }
        catch (e: IllegalArgumentException)
        {
            return null
        }
    }
}

fun log(message: String)
{
    println(message)
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