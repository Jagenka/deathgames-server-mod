package de.jagenka

import net.minecraft.block.Block
import net.minecraft.block.Blocks

val defaultColorBlock = Blocks.WHITE_CONCRETE

enum class DGTeam
{
    BLACK, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getColoredBlock(): Block
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

    fun getPlayers() = DGPlayerManager.getPlayersInTeam(this)

    fun getInGamePlayers() = DGPlayerManager.getInGamePlayersInTeam(this)

    companion object
    {
        fun random() = values().random()

        fun isColorBlock(block: Block) = block == defaultColorBlock || values().any { block == it.getColoredBlock() }
    }

}

fun Block.isDGColorBlock() = DGTeam.isColorBlock(this)