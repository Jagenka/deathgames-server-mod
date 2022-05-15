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
        }

        ifServerLoaded {
//            it.scoreboard.teams.clear()
        }
    }
}