package de.jagenka

import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.UUID

@ConfigSerializable
data class Coords(val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float)

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
}
