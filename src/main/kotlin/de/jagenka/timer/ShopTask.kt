package de.jagenka.timer

import de.jagenka.Util.sendPrivateMessage
import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.SpawnManager
import de.jagenka.shop.Shop
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object ShopTask : TimerTask
{
    private val currentlyInShop = mutableSetOf<String>()
    private val timeInShop = mutableMapOf<String, Int>().withDefault { 0 } // time in ticks

    private const val countdownStartingWithSecondsLeft = 5 // configurable

    override val onlyInGame: Boolean
        get() = true
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach { serverPlayerEntity ->
            val playerName = serverPlayerEntity.name.asString()

            if (Shop.isInShopBounds(serverPlayerEntity))
            {
                serverPlayerEntity.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 1.seconds(), 255))

                if (playerName !in currentlyInShop)
                {
                    currentlyInShop.add(playerName)
                    timeInShop[playerName] = 0

                    DisplayManager.sendTitleMessage(serverPlayerEntity, Text.of("Welcome to the shop!"), Text.of("Press F to pay money."), 3.seconds())
                }

                timeInShop[playerName] = timeInShop.getValue(playerName) + 1

                val ticksToTpOut = Config.tpOutOfShopAfter - timeInShop.getValue(playerName)

                if (ticksToTpOut == countdownStartingWithSecondsLeft.seconds())
                {
                    serverPlayerEntity.sendPrivateMessage("You will be teleported out in $countdownStartingWithSecondsLeft seconds.")

                    for (i in countdownStartingWithSecondsLeft - 1 downTo 1)
                    {
                        Timer.schedule({
                            serverPlayerEntity.sendPrivateMessage("You will be teleported out in $i seconds.")
                        }, (countdownStartingWithSecondsLeft - i).seconds())
                    }
                }

                if (ticksToTpOut < 0)
                {
                    exitShop(serverPlayerEntity)
                }

            } else currentlyInShop.remove(playerName)
        }
    }

    fun exitShop(player: ServerPlayerEntity)
    {
        SpawnManager.teleportPlayerToSpawn(player)
        timeInShop[player.name.asString()] = 0
        player.closeHandledScreen()
    }

    override fun reset()
    {
        currentlyInShop.clear()
        timeInShop.clear()
    }
}