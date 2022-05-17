package de.jagenka

import de.jagenka.DGPlayerManager.clearTeam
import de.jagenka.DGPlayerManager.registerToTeam
import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.teleport
import net.minecraft.text.Text

object Testing
{
    @JvmStatic
    fun dropTest()
    {
        println("drop it like it's hot")

//        DGPlayerManager.getPlayer("HideoTurismo")?.let {
//            it.registerToTeam(DGTeam.values().random())
//            DGSpawnManager.shuffleSpawns()
//            val spawn = DGSpawnManager.getSpawn(it)
//            Util.sendChatMessage(spawn?.toString() ?: "no spawn assigned")
//            ifServerLoaded { server ->
//                if (spawn != null)
//                {
//                    val (x, y, z, yaw, pitch) = spawn
//                    it.teleport(server.overworld, x, y, z, yaw, pitch)
//                }
//            }
//        }
//        Timer.toggle()

//        DGPlayerManager.getPlayers().forEach { it.clearTeam() }
//        DGPlayerManager.getPlayers().random().registerToTeam(DGTeam.random())
//        DGSpawnManager.shuffleSpawns(DGPlayerManager.getNonEmptyTeams())
//        DGPlayerManager.getPlayers().forEach { it.teleport(DGSpawnManager.getSpawn(it)) }

        DGPlayerManager.getPlayers().forEach { it.registerToTeam(DGTeam.random()) }
        DGPlayerManager.getPlayers().random().clearTeam()
        DeathGames.startGame()
    }
}