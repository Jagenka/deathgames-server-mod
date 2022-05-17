package de.jagenka

import de.jagenka.DGPlayerManager.eliminate
import de.jagenka.DGPlayerManager.getDGTeam
import de.jagenka.Util.sendPrivateMessage
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.configurate.CommentedConfigurationNode

object DGKillManager
{
    private val playerMoney = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamMoney = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerLives = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamLives = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val playerKillStreak = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val teamKillStreak = mutableMapOf<DGTeam?, Int>().withDefault { 0 }

    private val totalKills = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }
    private val totalDeaths = mutableMapOf<ServerPlayerEntity, Int>().withDefault { 0 }

    var moneyMode = Mode.PLAYER
    var livesMode = Mode.TEAM
    var killStreakMode = Mode.PLAYER

    var moneyPerKill = 20
    var startMoneyPerPlayer = 100
    var livesPerPlayer = 5
    var livesPerTeam = 10
    var killStreakBonus = 10

    @JvmStatic
    fun registerKill(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity) //TODO: handle deceased on every death
    {
        totalKills[attacker] = totalKills.getValue(attacker) + 1
        totalDeaths[deceased] = totalDeaths.getValue(deceased) + 1

        when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak[attacker] = playerKillStreak.getValue(attacker) + 1
            Mode.TEAM -> teamKillStreak[attacker.getDGTeam()] = teamKillStreak.getValue(attacker.getDGTeam()) + 1
        }

        handleMoney(attacker, deceased)

        // TODO?: reset shop teleport after kill
        // TODO: reset time since last kill

        handleLives(attacker, deceased)

        DGDisplayManager.updateSidebar()
    }

    private fun handleMoney(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
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
    }

    private fun getKillStreak(deceased: ServerPlayerEntity): Int
    {
        return when (killStreakMode)
        {
            Mode.PLAYER -> playerKillStreak.getValue(deceased).also { playerKillStreak[deceased] = 0 }
            Mode.TEAM -> teamKillStreak.getValue(deceased.getDGTeam()).also { teamKillStreak[deceased.getDGTeam()] = 0 }

        }
    }

    private fun handleLives(attacker: ServerPlayerEntity, deceased: ServerPlayerEntity)
    {
        when (livesMode)
        {
            Mode.PLAYER ->
            {
                val livesAmount = playerLives.getValue(deceased)
                if (livesAmount > 0) playerLives[deceased] = livesAmount - 1
                else deceased.eliminate()
            }
            Mode.TEAM ->
            {
                val livesAmount = teamLives.getValue(deceased.getDGTeam())
                if (livesAmount > 0) teamLives[deceased.getDGTeam()] = livesAmount - 1
                else deceased.eliminate()
            }
        }
    }

    fun loadConfig(root: CommentedConfigurationNode)
    {
        moneyPerKill = root.node("moneyPerKill").int
        livesPerPlayer = root.node("livesPerPlayer").int
        livesPerTeam = root.node("livesPerTeam").int
        killStreakBonus = root.node("killStreakBonus").int
        startMoneyPerPlayer = root.node("startMoneyPerPlayer").int
    }

    fun initLives(players: Collection<ServerPlayerEntity>)
    {
        when (livesMode)
        {
            Mode.PLAYER -> players.forEach { playerLives[it] = livesPerPlayer }
            Mode.TEAM -> players.forEach { teamLives[it.getDGTeam()] = livesPerTeam }
        }
    }

    fun initMoney(players: Collection<ServerPlayerEntity>)
    {
        when (moneyMode)
        {
            Mode.PLAYER -> players.forEach { playerMoney[it] = startMoneyPerPlayer }
            Mode.TEAM -> players.forEach { teamMoney[it.getDGTeam()] = teamMoney.getValue(it.getDGTeam()) + startMoneyPerPlayer }
        }
    }

    fun getPlayerLives() = playerLives
    fun getTeamLives() = teamLives

    fun getLives(player: ServerPlayerEntity) = playerLives[player]
    fun getLives(team: DGTeam) = teamLives[team]

    fun getNonZeroLifePlayers() = playerLives.filter { it.value > 0 }

    fun getNonZeroLifeTeams() = teamLives.filter { it.value > 0 }

    fun reset()
    {
        playerMoney.clear()
        teamMoney.clear()
        playerLives.clear()
        teamLives.clear()
        playerKillStreak.clear()
        teamKillStreak.clear()
        totalKills.clear()
        totalDeaths.clear()
    }
}

enum class Mode
{
    PLAYER, TEAM
}