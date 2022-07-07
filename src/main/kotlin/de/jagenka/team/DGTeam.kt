package de.jagenka.team

import de.jagenka.isSame
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isParticipating
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3f

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

    fun getColorVector(): Vec3f
    {
        return when (this)
        {
            BLACK -> Vec3f(1f, 1f, 1f)
            DARK_GREEN -> Vec3f(0.12f, 0.3f, 0.17f)
            DARK_AQUA -> Vec3f(0f, 0.42f, 0.5f)
            DARK_RED -> Vec3f(0.4f, 0f, 0f)
            DARK_PURPLE -> Vec3f(0.5f, 0f, 0.5f)
            GOLD -> Vec3f(0.85f, 0.57f, 0f)
            GRAY -> Vec3f(0.66f, 0.66f, 0.66f)
            DARK_GRAY -> Vec3f(0.33f, 0.33f, 0.33f)
            BLUE -> Vec3f(0.2f, 0.2f, 0.6f)
            GREEN -> Vec3f(0.12f, 0.3f, 0.17f)
            AQUA -> Vec3f(0f, 0.67f, 1f)
            RED -> Vec3f(0.67f, 0.29f, 0.32f)
            LIGHT_PURPLE -> Vec3f(0.69f,0.61f,0.85f)
            YELLOW -> Vec3f(1f, 0.83f, 0f)
        }
    }

    fun getPlayers() = PlayerManager.getPlayersInTeam(this)

    fun getOnlinePlayers() = PlayerManager.getOnlinePlayersInTeam(this)

    fun getOnlineParticipatingPlayers() = getOnlinePlayers().filter { it.isParticipating() }

    fun getPrettyName(): String
    {
        return this.name.replace('_', ' ').lowercase().capitalizeWords()
    }

    fun getFormattedText() = Text.literal(this.getPrettyName()).getWithStyle(Style.EMPTY.withColor(Formatting.byName(this.name.lowercase())))[0]

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

fun String.capitalizeWords() = split(" ").joinToString(separator = " ", transform = { it.replaceFirstChar { char -> char.uppercaseChar() } })