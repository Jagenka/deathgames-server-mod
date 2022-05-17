package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity

object TrapsAreNotGay
{
    private val notGayness = mutableSetOf<NotGay>()
    private val setupTime = 10.seconds()
    private const val gaynessRange = 0.5 // half a block radius

    fun addLessGay(x: Int, y: Int, z: Int)
    {
        val notGay = NotGay(Coords(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5), 0.ticks())
        ifServerLoaded {
            if (!notGayness.contains(notGay)) notGayness.add(notGay)
            else println("already a not gay here") //TODO: give back item
        }
    }

    private fun handleNotGay(player: ServerPlayerEntity)
    {
        notGayness.toList().forEach {
            if (Coords(player.x, player.y, player.z) distanceTo it.pos <= gaynessRange && it.age >= setupTime)
            {
                player.kill()
                notGayness.remove(it)
            }
        }
    }

    @JvmStatic
    fun tick()
    {
        notGayness.forEach { it.age++ }
        DGPlayerManager.getPlayers().forEach {
            handleNotGay(it)
            // TODO: use getInGamePlayers()
        }
    }
}

data class NotGay(val pos: Coords, var age: Int)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotGay

        if (pos != other.pos) return false

        return true
    }

    override fun hashCode(): Int
    {
        return pos.hashCode()
    }
}