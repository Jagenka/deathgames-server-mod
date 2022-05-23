package de.jagenka

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.util.UUID
import kotlin.math.floor

fun log(message: String)
{
    println(message)
}

object Util
{
    private lateinit var minecraftServer: MinecraftServer

    val modUUID = UUID.randomUUID()

    @JvmStatic
    fun onServerLoaded(minecraftServer: MinecraftServer)
    {
        this.minecraftServer = minecraftServer

        DGPlayerManager.prepareTeams()
    }

    fun ifServerLoaded(lambda: (MinecraftServer) -> Unit)
    {
        if (Util::minecraftServer.isInitialized) lambda(minecraftServer)
        else log("Minecraft Server not yet initialized")
    }

    fun sendChatMessage(message: String, formatting: Formatting = Formatting.WHITE, sender: UUID = modUUID)
    {
        val text = LiteralText(message).formatted(formatting)
        ifServerLoaded { it.playerManager.broadcast(text, MessageType.CHAT, sender) }
    }

    fun ServerPlayerEntity.sendPrivateMessage(text: String)
    {
        this.sendMessage(Text.of(text), MessageType.CHAT, modUUID)
    }

    fun ServerPlayerEntity.teleport(coordinates: Coordinates)
    {
        val (x, y, z, yaw, pitch) = coordinates
        this.teleport(server.overworld, x, y, z, yaw, pitch)
    }

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

        for (x in centerX.floor() - radius..centerX.floor() + radius)
        {
            for (z in centerZ.floor() - radius..centerZ.floor() + radius)
            {
                result.add(BlockAtCoordinates(getBlockAt(x, centerY.floor(), z), Coordinates(x, centerY.floor(), z)))
            }
        }

        return result.toList()
    }
}

data class BlockAtCoordinates(val block: Block, val coordinates: Coordinates)

fun Double.floor() = floor(this).toInt()