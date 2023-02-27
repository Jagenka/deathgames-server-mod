package de.jagenka.timer

import de.jagenka.BlockPos
import de.jagenka.Util
import de.jagenka.Util.teleport
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.SpawnManager
import de.jagenka.shop.Shop
import de.jagenka.util.I18n
import net.minecraft.block.Blocks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

object ShopTask : TimerTask
{
    private val currentlyInShop = mutableSetOf<String>()
    private val timeInShop = mutableMapOf<String, Int>().withDefault { 0 } // time in ticks

    private val lastPosOutOfShop = mutableMapOf<String, TPPos>()

    private const val countdownStartingWithSecondsLeft = 5 // configurable

    var tpOutActive: Boolean = true

    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach { serverPlayerEntity ->
            val playerName = serverPlayerEntity.name.string

            clearIllegalItems(serverPlayerEntity)

            if (PlayerManager.isCurrentlyDead(playerName)) return@forEach

            if (PlayerManager.isParticipating(playerName) && Shop.isInShopBounds(serverPlayerEntity))
            {
                if (InactivePlayersTask.hasShopClosed(playerName))
                {
                    lastPosOutOfShop[playerName]?.let {
                        serverPlayerEntity.teleport(it.pos, it.yaw, it.pitch)
                        DisplayManager.sendTitleMessage(
                            serverPlayerEntity,
                            Text.literal(I18n.get("shopClosedTitle")),
                            Text.literal(I18n.get("shopClosedSubtitle")),
                            3.seconds()
                        )
                    }
                    return@forEach
                }

                serverPlayerEntity.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 1.seconds(), 255))

                if (playerName !in currentlyInShop)
                {
                    currentlyInShop.add(playerName)
                    timeInShop[playerName] = 0

                    DisplayManager.sendTitleMessage(
                        serverPlayerEntity,
                        Text.of(I18n.get("shopEnteredTitle")),
                        Text.of(I18n.get("shopEnteredSubtitle")),
                        3.seconds()
                    )
                }

                if (tpOutActive)
                {
                    timeInShop[playerName] = timeInShop.getValue(playerName) + 1

                    val ticksToTpOut = Config.tpOutOfShopAfter - timeInShop.getValue(playerName)

                    if (ticksToTpOut == countdownStartingWithSecondsLeft.seconds())
                    {
                        sendTpOutMessage(serverPlayerEntity, countdownStartingWithSecondsLeft)
                    }

                    if (ticksToTpOut < 0)
                    {
                        exitShop(serverPlayerEntity)
                    }
                }
            } else currentlyInShop.remove(playerName)

            if (playerName !in currentlyInShop && serverPlayerEntity.isOnGround)
            {
                if (!Util.getBlockAt(BlockPos.from(serverPlayerEntity.pos).relative(0, -1, 0)).isSame(Blocks.AIR))
                {
                    lastPosOutOfShop[playerName] = TPPos(serverPlayerEntity.pos, serverPlayerEntity.yaw, serverPlayerEntity.pitch)
                }
            }
        }
    }

    fun sendTpOutMessage(player: ServerPlayerEntity, secondsLeft: Int)
    {
        if (secondsLeft > 0 && currentlyInShop.contains(player.name.string))
        {
            player.sendPrivateMessage(I18n.get("shopTpOut", mapOf("seconds" to secondsLeft)))
            Timer.schedule({ sendTpOutMessage(player, secondsLeft - 1) }, 1.seconds())
        }
    }

    private fun clearIllegalItems(player: ServerPlayerEntity)
    {
        val illegalItems = listOf(Items.GLASS_BOTTLE, Items.BUCKET)
        player.inventory.remove({ itemStack -> itemStack.item in illegalItems }, -1, player.playerScreenHandler.craftingInput)
    }

    fun exitShop(player: ServerPlayerEntity)
    {
        val playerName = player.name.string
        SpawnManager.teleportPlayerToSpawn(player)
        player.extinguish()
        timeInShop[playerName] = 0
        player.closeHandledScreen()
        Shop.clearRecentlyBought(playerName)
    }

    override fun reset()
    {
        currentlyInShop.clear()
        timeInShop.clear()
    }
}

data class TPPos(val pos: Vec3d, val yaw: Float, val pitch: Float)