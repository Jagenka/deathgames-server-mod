package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.commands.DeathGamesCommand
import de.jagenka.config.Config
import de.jagenka.managers.*
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeInGame
import de.jagenka.managers.SpawnManager.getSpawnCoordinates
import de.jagenka.shop.Shop
import de.jagenka.stats.StatManager
import de.jagenka.stats.StatsIO
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Text.literal
import net.minecraft.text.Texts
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode

object DeathGames : DedicatedServerModInitializer
{
    var running = false

    var currentlyStarting = false
    var currentlyEnding = false

    var gameId: Long? = null
        private set

    override fun onInitializeServer()
    {
        registerCommands()

        StatsIO.loadStats()

        println("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            DeathGamesCommand.register(dispatcher)
        }
    }

    fun startGameWithCountdown()
    {
        if (currentlyStarting) return
        currentlyStarting = true

        PlayerManager.getOnlinePlayers().forEach { player ->
            player.closeHandledScreen()
            DisplayManager.sendTitleMessage(player, literal("3"), literal(""), 1.seconds())
            Timer.schedule({ DisplayManager.sendTitleMessage(player, literal("2"), literal(""), 1.seconds()) }, 1.seconds())
            Timer.schedule({ DisplayManager.sendTitleMessage(player, literal("1"), literal(""), 1.seconds()) }, 2.seconds())
        }

        Timer.schedule({ startGame() }, 3.seconds())
    }

    fun startGame()
    {
        gameId = System.currentTimeMillis()

        currentlyStarting = false

        val teamPlayers = PlayerManager.getTeamPlayers()

        StatManager.reset()
        KillManager.reset()
        MoneyManager.reset()
        Timer.reset()

        DisplayManager.reset()
        Shop.reset()
        BonusManager.init()

        KillManager.initLives()
        MoneyManager.initMoney()

        teamPlayers.forEach {
            it.clearStatusEffects()
            it.inventory.clear()
            it.health = 20f //set max hearts
            it.hungerManager.add(20, 1f) //set max food and saturation
            it.makeInGame()
            it.changeGameMode(GameMode.ADVENTURE)
        }

        PlayerManager.getOnlinePlayers().filter { it.getDGTeam() == null }.forEach { it.changeGameMode(GameMode.SPECTATOR) }

        SpawnManager.shuffleSpawns()

        PlayerManager.getOnlinePlayers().forEach {
            it.closeHandledScreen()
            val (x, y, z) = Config.lobbySpawn
            it.setSpawnPoint(it.server.overworld.registryKey, BlockPos(x, y, z), 0f, true, false)
            it.teleport(it.getSpawnCoordinates())
        }

        ifServerLoaded { server ->
            server.overworld.iterateEntities().toList().filter { it is ItemEntity || it is ProjectileEntity }.forEach { it.remove(Entity.RemovalReason.KILLED) }
        }

        PlayerManager.getOnlinePlayers().forEach { player ->
            DisplayManager.sendTitleMessage(player, Text.of("Good Luck"), Text.of("and have fun"), 5.seconds())
        }

        DisplayManager.showSidebar()

        Timer.start()
        running = true
    }

    fun stopGame()
    {
        StatManager.gameStats.gameEnd = System.currentTimeMillis()

        currentlyEnding = true

        val winners = mutableListOf<Text>()
        val onlineInGameTeams = PlayerManager.getOnlineInGameTeams()
        onlineInGameTeams.forEach { team ->
            winners.add(team.getFormattedText())
        }
        val winnerCount = onlineInGameTeams.count()
        val winnerPlayers = Texts.join(winners, Text.of(", "))
        winners.clear()
        if (winnerCount != 0)
        {
            winners.add(Text.of("Winner${if (winnerCount != 1) "s" else ""}"))
            winners.add(winnerPlayers)
        }
        PlayerManager.getOnlinePlayers().forEach {
            DisplayManager.sendTitleMessage(it, Text.of("Game Over"), Texts.join(winners, Text.of(": ")), 5.seconds())
        }

        if (winnerCount == 1)
        {
            StatManager.gameStats.winner = onlineInGameTeams.getOrNull(0)
        }

        DisplayManager.resetBossBars()

        MoneyManager.reset()
        DisplayManager.updateLevelDisplay()

        PlayerManager.getOnlinePlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }

        DisplayManager.sendChatMessage("")
        DisplayManager.sendChatMessage(literal("Player K/Ds:").getWithStyle(Style.EMPTY.withBold(true))[0])
        KillManager.getKDs().forEach { (playerName, kd) ->
            DisplayManager.sendChatMessage("$playerName: $kd")
        }
        DisplayManager.sendChatMessage("")

        StatManager.saveAllStatsAfterGame()

        Timer.schedule({
            PlayerManager.getOnlinePlayers().forEach {
                it.changeGameMode(GameMode.ADVENTURE)
                it.clearStatusEffects()
                it.inventory.clear()
                it.health = 20f //set max hearts
                it.hungerManager.add(20, 1f) //set max food and saturation
            }

            PlayerManager.clearInGameStatusForEveryone()
            running = false
            currentlyEnding = false
        }, 10.seconds())
    }
}
