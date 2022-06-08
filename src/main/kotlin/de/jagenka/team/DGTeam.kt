package de.jagenka.team

import de.jagenka.isSame
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isInGame
import net.minecraft.block.Block
import net.minecraft.block.Blocks

enum class DGTeam
{
    BLACK, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getColorBlock(): Block
    {
        return when (this)
        {
            BLACK -> Blocks.BLACK_CONCRETE
            DARK_GREEN -> Blocks.GREEN_TERRACOTTA
            DARK_AQUA -> Blocks.CYAN_CONCRETE
            DARK_RED -> Blocks.RED_CONCRETE
            DARK_PURPLE -> Blocks.PURPLE_CONCRETE
            GOLD -> Blocks.ORANGE_CONCRETE
            GRAY -> Blocks.LIGHT_GRAY_CONCRETE
            DARK_GRAY -> Blocks.GRAY_CONCRETE
            BLUE -> Blocks.BLUE_CONCRETE
            GREEN -> Blocks.LIME_CONCRETE
            AQUA -> Blocks.LIGHT_BLUE_CONCRETE
            RED -> Blocks.RED_TERRACOTTA
            LIGHT_PURPLE -> Blocks.MAGENTA_CONCRETE
            YELLOW -> Blocks.YELLOW_CONCRETE
        }
    }

    fun getOnlinePlayers() = PlayerManager.getOnlinePlayersInTeam(this)

    fun getOnlineInGamePlayers() = getOnlinePlayers().filter { it.isInGame() }

    companion object
    {
        val defaultColorBlock: Block = Blocks.WHITE_CONCRETE

        fun random() = values().random()

        fun isColorBlock(block: Block) = block isSame defaultColorBlock || values().any { block isSame it.getColorBlock() }

        fun getValuesAsStringList(): List<String>
        {
            val result = mutableListOf<String>()
            values().forEach { result.add(it.name) }
            return result.toList()
        }
    }

}

fun Block.isDGColorBlock() = DGTeam.isColorBlock(this)