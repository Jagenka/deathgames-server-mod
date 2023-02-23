package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import de.jagenka.util.I18n
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object BonusDisplayTask : TimerTask
{
    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 10.ticks()

    override fun run()
    {
        if (currentlyEnding) return

        val timeToSpawn = BonusSpawnTask.getTimeToSpawn()
        val timeToDespawn = BonusSpawnTask.getTimeToDespawn()

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
        val replaceMap = mapOf<String, Any>("name" to name, "time" to timeToSpawn / DGUnit.SECONDS.factor)
        return if (name.isEmpty()) I18n.get("bonusInactive", replaceMap) else I18n.get("bonusInactiveWithName", replaceMap)
    }

    fun getActiveString(name: String, timeToDespawn: Int): String
    {
        val replaceMap = mapOf<String, Any>("name" to name, "time" to timeToDespawn / DGUnit.SECONDS.factor)
        return if (name.isEmpty()) I18n.get("bonusActive", replaceMap) else I18n.get("bonusActiveWithName", replaceMap)
    }

    override fun reset()
    {

    }
}