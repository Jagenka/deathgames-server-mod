package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.commands.DeathGamesCommand
import de.jagenka.config.Config
import de.jagenka.managers.*
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeInGame
import de.jagenka.managers.SpawnManager.getSpawn
import de.jagenka.shop.Shop
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Texts
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode

object DeathGames : DedicatedServerModInitializer
{
    var running = false

    override fun onInitializeServer()
    {
        Config.loadJSON()

        registerCommands()

        println("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            DeathGamesCommand.register(dispatcher)
        }
    }

    fun startGame()
    {
        val teamPlayers = PlayerManager.getTeamPlayers()

        KillManager.reset()
        MoneyManager.reset()
        Timer.reset()
        PlayerManager.reset()

        DisplayManager.reset()
        Shop.reset()
        BonusManager.init()

        KillManager.initLives()
        KillManager.initMoney()

        DisplayManager.showSidebar()

        teamPlayers.forEach {
            it.clearStatusEffects()
            it.inventory.clear()
            it.health = 20f //set max hearts
            it.hungerManager.add(20, 1f) //set max food and saturation
            it.makeInGame()
            it.changeGameMode(GameMode.ADVENTURE)
        }

        PlayerManager.getOnlinePlayers().filter { it.getDGTeam() == null }.forEach { it.changeGameMode(GameMode.SPECTATOR) }

        ifServerLoaded { it.overworld.setWeather(Int.MAX_VALUE, 0, false, false) }

        SpawnManager.shuffleSpawns()

        PlayerManager.getOnlinePlayers().forEach {
            val (x, y, z) = Config.worldSpawn
            it.setSpawnPoint(it.server.overworld.registryKey, BlockPos(x, y, z), 0f, true, false)
            it.teleport(it.getSpawn())
        }

        //TODO: remove arrows / items from map

        PlayerManager.getOnlinePlayers().forEach { player ->
            DisplayManager.sendTitleMessage(player, Text.of("Whaddup fuckers"), Text.of("Ready to fuck?"), 5.seconds())
        }

        Timer.start()
        running = true
    }

    fun stopGame()
    {
        val winners = mutableListOf<Text>()
        PlayerManager.getOnlineInGameTeams().forEach { team ->
            winners.add(LiteralText("$team").getWithStyle(Style.EMPTY.withColor(Formatting.byName(team.name.lowercase())))[0])
        }
        val winnerCount = winners.count()
        val winnerPlayers = Texts.join(winners, Text.of(", "))
        winners.clear()
        if (winnerCount < 2) winners.add(Text.of("Winner")) else winners.add(Text.of("Winners"))
        winners.add(winnerPlayers)
        PlayerManager.getOnlinePlayers().forEach {
            DisplayManager.sendTitleMessage(it, Text.of("Game Over"), Texts.join(winners, Text.of(": ")), 5.seconds())
        }

        DisplayManager.resetBossBars()

        running = false
        PlayerManager.getOnlinePlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }
        //TODO: timer, back to lobby, etc
    }
}
