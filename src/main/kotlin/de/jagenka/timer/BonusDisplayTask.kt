package de.jagenka.timer

import de.jagenka.DeathGames.currentlyEnding
import de.jagenka.managers.BonusManager
import de.jagenka.managers.DisplayManager
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

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
            val selectedPlatforms = BonusManager.getSelectedPlatforms()
            if (selectedPlatforms.isNotEmpty())
            {
                val (name) = selectedPlatforms[0]
                DisplayManager.showTimeToBonusMessage(
                    Text.of("Bonus Money Platform: $name in ${timeToSpawn / DGUnit.SECONDS.factor}sec").getWithStyle(Style.EMPTY.withColor(Formatting.DARK_RED).withBold(true))[0]
                )
            }
        } else if (timeToDespawn != null)
        {
            val activePlatforms = BonusManager.getActivePlatforms()
            if (activePlatforms.isNotEmpty())
            {
                val (name) = activePlatforms[0]
                DisplayManager.showTimeToBonusMessage(
                    Text.of("Bonus Money Platform: $name for another ${timeToDespawn / DGUnit.SECONDS.factor}sec")
                        .getWithStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN).withBold(true))[0]
                )
            }
        }
    }

    override fun reset()
    {

    }
}