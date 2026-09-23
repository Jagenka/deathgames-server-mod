package de.jagenka.team

import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isParticipating
import de.jagenka.util.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.scores.TeamColor
import org.joml.Vector3f

enum class DGTeam
{
    BLACK, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW;

    fun getColorBlock(): Block
    {
        return when (this)
        {
            BLACK -> Blocks.CONCRETE.black
            DARK_GREEN -> Blocks.DYED_TERRACOTTA.green
            DARK_AQUA -> Blocks.CONCRETE.cyan
            DARK_RED -> Blocks.CONCRETE.red
            DARK_PURPLE -> Blocks.CONCRETE.purple
            GOLD -> Blocks.CONCRETE.orange
            GRAY -> Blocks.CONCRETE.lightGray
            DARK_GRAY -> Blocks.CONCRETE.gray
            BLUE -> Blocks.CONCRETE.blue
            GREEN -> Blocks.CONCRETE.lime
            AQUA -> Blocks.CONCRETE.lightBlue
            RED -> Blocks.DYED_TERRACOTTA.red
            LIGHT_PURPLE -> Blocks.CONCRETE.magenta
            YELLOW -> Blocks.CONCRETE.yellow
        }
    }

    fun getTeamColor(): TeamColor
    {
        return when (this)
        {
            BLACK -> TeamColor.BLACK
            DARK_GREEN -> TeamColor.DARK_GREEN
            DARK_AQUA -> TeamColor.DARK_AQUA
            DARK_RED -> TeamColor.DARK_RED
            DARK_PURPLE -> TeamColor.DARK_PURPLE
            GOLD -> TeamColor.GOLD
            GRAY -> TeamColor.GRAY
            DARK_GRAY -> TeamColor.DARK_GRAY
            BLUE -> TeamColor.BLUE
            GREEN -> TeamColor.GREEN
            AQUA -> TeamColor.AQUA
            RED -> TeamColor.RED
            LIGHT_PURPLE -> TeamColor.LIGHT_PURPLE
            YELLOW -> TeamColor.YELLOW
        }
    }

    fun getTextColor(): TextColor
    {
        return getTeamColor().textColor()
    }

    fun getColorInt(): Int
    {
        return getTeamColor().rgb()
    }

    fun getColorVector(): Vector3f = Util.getRGBVector3fForInt(getColorInt())

    fun getPlayers() = PlayerManager.getPlayersInTeam(this)

    fun getOnlinePlayers() = PlayerManager.getOnlinePlayersInTeam(this)

    fun getOnlineParticipatingPlayers() = getOnlinePlayers().filter { it.isParticipating() }

    fun getPrettyName(): String
    {
        return I18n.get("team${this.name.replace('_', ' ').lowercase().capitalizeWords().replace(" ", "")}")
    }

    fun getFormattedText(): Component
    {
        return Component.literal(this.getPrettyName()).withColor(getTextColor())
    }

    companion object
    {
        val defaultColorBlock: Block = Blocks.CONCRETE.white

        fun random() = entries.random()

        fun isColorBlock(block: Block) =
            block isSame defaultColorBlock || entries.any { block isSame it.getColorBlock() }

        fun getValuesAsStringList(): List<String> = Config.general.enabledTeams.asStringList()
    }

}

fun List<DGTeam>.asStringList(): List<String>
{
    val result = mutableListOf<String>()
    this.forEach { result.add(it.name) }
    return result.toList()
}

fun Block.isDGColorBlock() = DGTeam.isColorBlock(this)

fun String.capitalizeWords() =
    split(" ").joinToString(separator = " ", transform = { it.replaceFirstChar { char -> char.uppercaseChar() } })