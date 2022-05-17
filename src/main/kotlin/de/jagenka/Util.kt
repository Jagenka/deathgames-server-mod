package de.jagenka

import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

@ConfigSerializable
data class Coords(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f)
{
    operator fun Coords.plus(other: Coords) = Coords(this.x + other.x, this.y + other.y, this.z + other.z, this.yaw, this.pitch)
    operator fun Coords.minus(other: Coords) = Coords(this.x - other.x, this.y - other.y, this.z - other.z, this.yaw, this.pitch)
    infix fun distanceTo(other: Coords) = (other - this).length()
    private fun length() = sqrt(this.x.pow(2) + this.y.pow(2) + this.z.pow(2))
}

data class Kill(val attacker: ServerPlayerEntity, val deceased: ServerPlayerEntity)

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

        DGPlayerManager.reset()
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

    fun ServerPlayerEntity.teleport(coords: Coords)
    {
        val (x, y, z, yaw, pitch) = coords
        this.teleport(server.overworld, x, y, z, yaw, pitch)
    }
}
