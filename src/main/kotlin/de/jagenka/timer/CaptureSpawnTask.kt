package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config.captureEnabled
import de.jagenka.config.Config.captureTimeNeeded
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.SpawnManager
import net.minecraft.entity.boss.BossBar
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text.literal

object CaptureSpawnTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    private val captureProgress = mutableMapOf<DGSpawn, Int>().withDefault { 0 }

    override fun run()
    {
        if (currentlyEnding) return

        if (!captureEnabled) return

        val playersOnAnySpawn = mutableListOf<Pair<ServerPlayerEntity, DGSpawn>>()

        SpawnManager.getSpawns().forEach forEachSpawn@{ spawn ->
            val playersOnSpawn = PlayerManager.getOnlineInGamePlayers().filter { spawn.containsPlayer(it) }
            val teamsOnSpawn = playersOnSpawn.map { it.getDGTeam() }.toSet()
            if (teamsOnSpawn.count { it != null } == 1)
            {
                if(SpawnManager.getTeam(spawn) !in teamsOnSpawn)
                {
                    if (captureProgress.getValue(spawn) > captureTimeNeeded)
                    {
                        val team = teamsOnSpawn.find { it != null } ?: return@forEachSpawn // das sollte nie passieren
                        captureProgress[spawn] = 0
                        val teamPreviouslyAssigned = SpawnManager.reassignSpawn(spawn, team)
                        val captureMessage = literal("")
                        captureMessage.append(team.getFormattedText())
                        captureMessage.append(literal(" captured "))

                        if (teamPreviouslyAssigned != null)
                        {
                            captureMessage.append(teamPreviouslyAssigned.getFormattedText())
                            captureMessage.append(literal("'s spawn!"))
                        } else
                        {
                            captureMessage.append(literal("a spawn!"))
                        }

                        DisplayManager.sendChatMessage(captureMessage)
                    } else
                    {
                        captureProgress[spawn] = captureProgress.getValue(spawn) + 1
                    }
                }
                else
                {
                    captureProgress[spawn] = 0
                    return@forEachSpawn
                }

            } else
            {
                captureProgress[spawn] = 0
            }

            playersOnSpawn.forEach { player -> playersOnAnySpawn.add(player to spawn) }
        }

        playersOnAnySpawn.forEach forEachPlayer@{ (playerOnSpawn, spawn) ->
            val progress = captureProgress.getValue(spawn)
            val fillAmount = progress.toFloat() / captureTimeNeeded.toFloat()
            DisplayManager.setBossBarForPlayer(playerOnSpawn, fillAmount, text = literal("capture progress..."), color = BossBar.Color.BLUE, idSuffix = "capture")
        }

        PlayerManager.getOnlinePlayers()
            .filter { player -> player !in playersOnAnySpawn.map { pair -> pair.first } }
            .forEach { player -> DisplayManager.removeBossBarForPlayer(player, idSuffix = "capture") }
    }

    override fun reset()
    {
        captureProgress.clear()
    }
}