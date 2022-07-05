package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import de.jagenka.config.Config.configEntry as config

object BonusDisplayTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 10.ticks()

    override fun run()
    {
        if (currentlyEnding) return

        val timeToSpawn = BonusManager.getTimeToSpawn()
        val timeToDespawn = BonusManager.getTimeToDespawn()

        if (timeToSpawn != null)
        {
            val selectedPlatforms = BonusManager.selectedPlatforms
            if (selectedPlatforms.isNotEmpty())
            {
                val (name) = selectedPlatforms[0]
                DisplayManager.showTimeToBonusMessage(
                    Text.of(getInactiveString(name, timeToSpawn))
                        .getWithStyle(Style.EMPTY.withColor(Formatting.DARK_RED).withBold(true))[0]
                )
            }
        } else if (timeToDespawn != null)
        {
            val activePlatforms = BonusManager.getActivePlatforms()
            if (activePlatforms.isNotEmpty())
            {
                val (name) = activePlatforms[0]
                DisplayManager.showTimeToBonusMessage(
                    Text.of(getActiveString(name, timeToDespawn))
                        .getWithStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN).withBold(true))[0]
                )
            }
        }
    }

    fun getInactiveString(name: String, timeToSpawn: Int): String
    {
        val baseString = if (name.isEmpty()) config.displayedText.bonusInactive else config.displayedText.bonusInactiveWithName
        return baseString.replace("%name", name).replace("%time", (timeToSpawn / DGUnit.SECONDS.factor).toString())
    }

    fun getActiveString(name: String, timeToDespawn: Int): String
    {
        val baseString = if (name.isEmpty()) config.displayedText.bonusActive else config.displayedText.bonusActiveWithName
        return baseString.replace("%name", name).replace("%time", (timeToDespawn / DGUnit.SECONDS.factor).toString())
    }

    override fun reset()
    {

    }
}