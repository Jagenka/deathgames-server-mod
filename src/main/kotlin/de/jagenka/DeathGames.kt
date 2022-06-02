package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.commands.DeathGamesCommand
import de.jagenka.commands.JayCommand
import de.jagenka.managers.*
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeInGame
import de.jagenka.managers.SpawnManager.getSpawn
import de.jagenka.shop.Shop
import de.jagenka.timer.Timer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode

object DeathGames : DedicatedServerModInitializer
{
    var running = false

    const val CONF_FILE = "deathgames_conf.yaml"

    override fun onInitializeServer()
    {
        Config.load()

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

        //TODO: show begin message

        Timer.start()
        running = true
    }

    fun stopGame()
    {
        Util.sendChatMessage("GAME OVER") //TODO: change this
        running = false
        PlayerManager.getOnlinePlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }
        //TODO: timer, back to lobby, etc
    }
}
