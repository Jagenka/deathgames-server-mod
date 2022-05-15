package de.jagenka

import de.jagenka.Util.ifServerLoaded

object Testing
{
    @JvmStatic
    fun dropTest()
    {
        println("drop it like it's hot")

        DGPlayerManager.getPlayer("HideoTurismo")?.let {
            ifServerLoaded { server -> server.scoreboard.teams.first() }
        }
    }
}