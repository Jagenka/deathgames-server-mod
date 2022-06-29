package de.jagenka.timer

import de.jagenka.Coordinates
import de.jagenka.DeathGames
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.team.TeamSelectorUI
import de.jagenka.toDGCoordinates
import net.minecraft.world.GameMode

object BasicTpTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        if (!DeathGames.running)
        {
            PlayerManager.getOnlinePlayers().forEach {
                if (!it.interactionManager.isSurvivalLike)
                {
                    if (!it.hasPermissionLevel(2)) it.changeGameMode(GameMode.ADVENTURE)

                    /* BY RADIUS, OLD CODE
                    val middle = (Config.arenaBounds.firstCorner + Config.arenaBounds.secondCorner) / 2.0
                    val maxRadius = (Config.arenaBounds.firstCorner - middle).toVec3d().length()
                    val allowedRadius = maxRadius + Config.spectatorRadiusPadding
                    val playerCoords3D = Coordinates(it.pos)
                    var playerCoords2D = Coordinates(it.pos.x, middle.y.toDouble(), it.pos.z)
                    // 0.9 safety boundary to prevent getting a seizure when flying outwards
                    if ((playerCoords2D - middle).toVec3d().length() > allowedRadius) {
                        playerCoords2D *= (allowedRadius * 0.9) / playerCoords2D.toVec3d().length()
                        it.teleport(it.getWorld(), playerCoords2D.x.toDouble(), playerCoords3D.y.toDouble(), playerCoords2D.z.toDouble(), it.yaw, it.pitch)
                    }
                     */
                    val (posX, negX) = listOf<Int>(Config.arenaBounds.secondCorner.x, Config.arenaBounds.firstCorner.x).sortedDescending()
                    val (posZ, negZ) = listOf<Int>(Config.arenaBounds.secondCorner.z, Config.arenaBounds.firstCorner.z).sortedDescending()
                    if (it.pos.x > posX + Config.spectatorRadiusPadding) it.teleport(Coordinates(posX.toDouble(), it.pos.y, it.pos.z))
                    if (it.pos.x < negX - Config.spectatorRadiusPadding) it.teleport(Coordinates(negX.toDouble(), it.pos.y, it.pos.z))
                    if (it.pos.z > posZ + Config.spectatorRadiusPadding) it.teleport(Coordinates(it.pos.x, it.pos.y, posZ.toDouble()))
                    if (it.pos.z < negZ - Config.spectatorRadiusPadding) it.teleport(Coordinates(it.pos.x, it.pos.y, negZ.toDouble()))
                    return@forEach
                }
                if (!TeamSelectorUI.lobbyBounds.contains(it.pos.toDGCoordinates())) it.teleport(Config.lobbySpawn)
            }
        }
    }

    override fun reset()
    {

    }
}