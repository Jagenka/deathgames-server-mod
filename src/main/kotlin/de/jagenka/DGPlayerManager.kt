package de.jagenka

import de.jagenka.Util.ifServerLoaded
import net.minecraft.server.network.ServerPlayerEntity

object DGPlayerManager
{
    private val players = HashSet<ServerPlayerEntity>()
    private val score = HashMap<ServerPlayerEntity, Int>()

    fun getPlayer(name: String): ServerPlayerEntity?
    {
        players.forEach { player -> if (player.name.asString() == name) return player }

        var result: ServerPlayerEntity? = null
        ifServerLoaded { server -> server.playerManager.playerList.forEach { player -> if (player.name.asString() == name) result = player } }
        if (result != null) players.add(result!!)
        return result
    }

    fun getScore(name: String): Int?
    {
        return getPlayer(name)?.let { getScore(it) }
    }

    fun getScore(player: ServerPlayerEntity): Int?
    {
        return score[player]
    }

    fun reset()
    {
        ifServerLoaded { server -> players.forEach { player -> server.scoreboard.clearPlayerTeam(player.name.asString()) } }
        players.clear()
        score.clear()
    }
}

enum class MCColor
{
    black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white
}
