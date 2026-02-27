package de.jagenka.team

import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.isParticipating
import de.jagenka.util.I18n
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import org.joml.Vector3f

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

    fun getColorInt(): Int = ChatFormatting.getByName(this.name.lowercase())?.color ?: 0xFFFFFF

    fun getColorVector(): Vector3f = Util.getRGBVector3fForInt(getColorInt())

    fun getPlayers() = PlayerManager.getPlayersInTeam(this)

    fun getOnlinePlayers() = PlayerManager.getOnlinePlayersInTeam(this)

    fun getOnlineParticipatingPlayers() = getOnlinePlayers().filter { it.isParticipating() }

    fun getPrettyName(): String
    {
        return I18n.get("team${this.name.replace('_', ' ').lowercase().capitalizeWords().replace(" ", "")}")
    }

    fun getFormattedText(): Component =
        Component.literal(this.getPrettyName()).withColor(ChatFormatting.getByName(this.name.lowercase())?.color ?: 0)

    companion object
    {
        val defaultColorBlock: Block = Blocks.WHITE_CONCRETE

        fun random() = entries.random()

        fun isColorBlock(block: Block) = block isSame defaultColorBlock || entries.any { block isSame it.getColorBlock() }

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

fun String.capitalizeWords() = split(" ").joinToString(separator = " ", transform = { it.replaceFirstChar { char -> char.uppercaseChar() } })