package de.jagenka

import de.jagenka.DGPlayerManager.getDGTeam
import de.jagenka.DGPlayerManager.makeInGame
import de.jagenka.DGSpawnManager.getSpawn
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import de.jagenka.commands.JayCommand
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
            JayCommand.register(dispatcher)
        }
    }

    fun startGame()
    {
        val teamPlayers = DGPlayerManager.getTeamPlayers()

        DGKillManager.reset()
        Timer.reset()
        DGPlayerManager.reset()

        DGDisplayManager.reset()
        //TODO: reset shop
        DGBonusManager.init()

        DGKillManager.initLives(teamPlayers)
        DGKillManager.initMoney(teamPlayers)

        DGDisplayManager.showSidebar()

        teamPlayers.forEach {
            it.health = 20f //set max hearts
            it.hungerManager.add(20, 1f) //set max food and saturation
            it.makeInGame()
            it.changeGameMode(GameMode.ADVENTURE)
        }

        DGPlayerManager.getPlayers().filter { it.getDGTeam() == null }.forEach { it.changeGameMode(GameMode.SPECTATOR) }
        //TODO: clear inventory

        ifServerLoaded { it.overworld.setWeather(Int.MAX_VALUE, 0, false, false) }

        DGSpawnManager.shuffleSpawns()

        DGPlayerManager.getPlayers().forEach {
            it.setSpawnPoint(it.server.overworld.registryKey, BlockPos(0, 51, 0), 0f, true, false) //TODO: read from config -> blackbox
            it.teleport(it.getSpawn())
        }

        //TODO: remove arrows / items from map / gino's traps

        //TODO: show begin message

        Timer.start()
        running = true
    }
}
