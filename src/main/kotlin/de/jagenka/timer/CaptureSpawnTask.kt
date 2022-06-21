package de.jagenka.timer

import de.jagenka.managers.DGSpawn
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.SpawnManager
import de.jagenka.team.DGTeam

object CaptureSpawnTask : TimerTask
{
    val captureTimeNeeded = 20.seconds() //TODO: config

    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val captureProgress = mutableMapOf<Pair<DGSpawn, DGTeam>, Int>().withDefault { 0 }

    override fun run()
    {
        if (SpawnManager.captureEnabled)
        {
            SpawnManager.getSpawns().forEach forEachSpawn@{ spawn ->
                val playersOnSpawn = PlayerManager.getOnlineInGamePlayers().filter { player -> spawn.containsPlayer(player) }
                SpawnManager.getTeam(spawn)?.let { teamAssignedToSpawn ->
                    if (playersOnSpawn.find { player -> player.getDGTeam() == teamAssignedToSpawn } != null) return@forEachSpawn
                }

                PlayerManager.getInGameTeams().forEach forEachTeam@{ team ->
                    val playerAmountOnSpawn = team.getOnlineInGamePlayers().count { teamPlayer -> spawn.containsPlayer(teamPlayer) }
                    if (playerAmountOnSpawn > 0) captureProgress[spawn to team] = captureProgress.getValue(spawn to team) + playerAmountOnSpawn
                    else captureProgress.remove(spawn to team)
                }
            }

            captureProgress.toList().forEach { (pair, progress) ->
                val (spawn, team) = pair

                if (progress > captureTimeNeeded)
                {
                    println("$team has captured $spawn")
                    SpawnManager.reassignSpawn(spawn, team)
                    captureProgress.remove(pair)
                }
            }
        }
    }

    override fun reset()
    {
        captureProgress.clear()
    }
}