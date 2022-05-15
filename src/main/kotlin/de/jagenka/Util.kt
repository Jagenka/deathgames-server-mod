package de.jagenka

import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Coords(val x: Int, val y: Int, val z: Int, val yaw: Float, val pitch: Float)

data class Kill(val attacker: ServerPlayerEntity, val deceased: ServerPlayerEntity)

fun log(message: String)
{
    println(message)
}

object Util
{
    private lateinit var minecraftServer: MinecraftServer

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
}
