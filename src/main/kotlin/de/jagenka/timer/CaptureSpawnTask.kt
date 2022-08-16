package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.config.Config.captureEnabled
import de.jagenka.config.Config.captureTimeNeeded
import de.jagenka.gameplay.rendering.CaptureAnimation
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.SpawnManager
import de.jagenka.stats.StatManager
import de.jagenka.stats.gib
import de.jagenka.team.DGTeam
import de.jagenka.util.I18n
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

        SpawnManager.spawns.forEach forEachSpawn@{ spawn ->
            val playersOnSpawn = PlayerManager.getOnlineParticipatingPlayers().filter { spawn.containsPlayer(it) }
            val teamsOnSpawn = playersOnSpawn.map { it.getDGTeam() }.toSet()
            if (teamsOnSpawn.count { it != null } == 1)
            {
                if (SpawnManager.getTeam(spawn) !in teamsOnSpawn)
                {
                    if (captureProgress.getValue(spawn) > captureTimeNeeded)
                    {
                        val newTeam = teamsOnSpawn.find { it != null } ?: return@forEachSpawn // das sollte nie passieren
                        captureProgress[spawn] = 0
                        val oldTeam = SpawnManager.reassignSpawn(spawn, newTeam)

                        sendCaptureMessage(oldTeam, newTeam)

                        playersOnSpawn.filter { it.getDGTeam() == newTeam }.forEach { StatManager.personalStats.gib(it.name.string).spawnsCaptured++ }
                    } else
                    {
                        captureProgress[spawn] = captureProgress.getValue(spawn) + 1
                    }
                } else
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
            DisplayManager.setBossBarForPlayer(playerOnSpawn, fillAmount, text = literal(I18n.get("captureProgress")), color = BossBar.Color.BLUE, idSuffix = "capture")
        }

        PlayerManager.getOnlinePlayers()
            .filter { player -> player !in playersOnAnySpawn.map { pair -> pair.first } }
            .forEach { player -> DisplayManager.removeBossBarForPlayer(player, idSuffix = "capture") }

        val underAttackSpawnProgress = captureProgress.filterKeys { it in playersOnAnySpawn.map { (_, spawn) -> spawn }.toSet() }
        CaptureAnimation.renderOrb(underAttackSpawnProgress)
    }

    fun sendCaptureMessage(oldTeam: DGTeam?, newTeam: DGTeam)
    {
        val replaceMap = mapOf("newTeam" to "%newTeam", "oldTeam" to "%oldTeam")

        val baseString = if (oldTeam != null)
        {
            I18n.get("captureTeamSpawn", replaceMap)
        } else
        {
            I18n.get("captureASpawn", replaceMap)
        }
        val mapThingie = mutableMapOf("%newTeam" to newTeam)
        if(oldTeam != null) mapThingie["%oldTeam"] = oldTeam
        DisplayManager.sendChatMessage(DisplayManager.getTextWithPlayersAndTeamsColored(baseString, idToTeam = mapThingie))
    }

    override fun reset()
    {
        captureProgress.clear()
    }
}