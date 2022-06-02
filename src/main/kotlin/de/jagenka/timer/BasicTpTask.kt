package de.jagenka.timer

import de.jagenka.BlockCuboid
import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.managers.PlayerManager
import de.jagenka.toDGCoordinates
import net.minecraft.world.GameMode

object BasicTpTask : TimerTask
{
    private val arena = BlockCuboid(Coordinates(-128, 48, -128), Coordinates(129, 152, 129)) //TODO: config

    private val lobby = Coordinates(0.5, 20.0, 0.5) //TODO: Config

    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (!DeathGames.running)
        {
            PlayerManager.getOnlinePlayers().forEach {
                if (it.interactionManager.gameMode == GameMode.SPECTATOR) return@forEach
                if (arena.contains(it.pos.toDGCoordinates())) it.teleport(lobby)
            }
        }
    }

    override fun reset()
    {

    }
}