package de.jagenka

import de.jagenka.DGPlayerManager.getDGTeam
import de.jagenka.Util.sendPrivateMessage
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.CommentedConfigurationNode

object DGKillManager
{
    val playerMoney = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    val playerLives = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    val teamLives = mutableMapOf<DGTeam, Int>().withDefault { 0 }

    val playerKillStreak = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    var moneyMode = Mode.PLAYER
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER

    var moneyPerKill = 20
    var livesPerPlayer = 5
    var livesPerTeam = 10
    var killStreakBonus = 10

    @JvmStatic
    fun registerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        when (moneyMode)
        {
            Mode.PLAYER ->
            {
                val killStreakAmount = getKillStreak(deceased)
                playerMoney[attacker] = playerMoney.getValue(attacker) + moneyPerKill + killStreakBonus * killStreakAmount
                Util.sendChatMessage("They made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.sendPrivateMessage("You received ${moneyPerKill + killStreakBonus * killStreakAmount}")
            }
            Mode.TEAM ->
            {
                val killStreakAmount = getKillStreak(deceased)
                teamMoney[attacker.getDGTeam()] = teamMoney.getValue(attacker.getDGTeam()) + moneyPerKill + killStreakAmount
                Util.sendChatMessage("${attacker.getDGTeam()?.name ?: "They"} made $killStreakAmount kill${if (killStreakAmount != 1) "s" else ""} since their previous death.")
                attacker.getDGTeam()?.getPlayers()?.forEach { it.sendPrivateMessage("Your team received ${moneyPerKill + killStreakBonus * killStreakAmount}") }
            }
        }

        // TODO?: reset shop teleport after kill
        // TODO: reset time since last kill
    }

    private fun getKillStreak(deceased: ServerPlayerEntity): Int
    {
        return when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak.getValue(deceased).also { playerKillStreak[deceased] = 0 }
            Mode.TEAM -> teamKillStreak.getValue(deceased.getDGTeam()).also { teamKillStreak[deceased.getDGTeam()] = 0 }

        }
    }

    fun loadConfig(root: CommentedConfigurationNode)
    {
        moneyPerKill = root.node("moneyPerKill").int
        livesPerPlayer = root.node("livesPerPlayer").int
        livesPerTeam = root.node("livesPerTeam").int
        killStreakBonus = root.node("killStreakBonus").int
    }
}

enum class Mode
{
    PLAYER, TEAM
}