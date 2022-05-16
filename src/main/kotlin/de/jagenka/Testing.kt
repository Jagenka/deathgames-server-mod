package de.jagenka

import de.jagenka.DGPlayerManager.registerToTeam
import de.jagenka.Util.ifServerLoaded
import net.minecraft.text.Text

object Testing
{
    @JvmStatic
    fun dropTest()
    {
        println("drop it like it's hot")

        DGPlayerManager.getPlayer("HideoTurismo")?.let {
            it.registerToTeam(DGTeam.values().random())
            DGSpawnManager.shuffleSpawns()
            val spawn = DGSpawnManager.getSpawn(it)
            Util.sendChatMessage(spawn?.toString() ?: "no spawn assigned")
            ifServerLoaded { server ->
                if (spawn != null)
                {
                    val (x, y, z, yaw, pitch) = spawn
                    it.teleport(server.overworld, x, y, z, yaw, pitch)
                }
            }
        }

        ifServerLoaded {
//            it.scoreboard.teams.clear()
        }
    }
}