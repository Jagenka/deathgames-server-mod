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
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

object ShopTask : TimerTask
{
    private val currentlyInShop = mutableSetOf<String>()
    private val timeInShop = mutableMapOf<String, Int>().withDefault { 0 } // time in ticks

    private val lastPosOutOfShop = mutableMapOf<String, TPPos>()

    private const val countdownStartingWithSecondsLeft = 5

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
                            Component.literal(I18n.get("shopClosedTitle")),
                            Component.literal(I18n.get("shopClosedSubtitle")),
                            3.seconds()
                        )
                    }
                    return@forEach
                }

                serverPlayerEntity.addEffect(MobEffectInstance(MobEffects.RESISTANCE, 1.seconds(), 255))

                if (playerName !in currentlyInShop)
                {
                    currentlyInShop.add(playerName)
                    timeInShop[playerName] = 0

                    DisplayManager.sendTitleMessage(
                        serverPlayerEntity,
                        Component.literal(I18n.get("shopEnteredTitle")),
                        Component.literal(I18n.get("shopEnteredSubtitle")),
                        3.seconds()
                    )
                }

                if (tpOutActive)
                {
                    timeInShop[playerName] = timeInShop.getValue(playerName) + 1

                    val ticksToTpOut = Config.shopSettings.tpOutOfShopAfter - timeInShop.getValue(playerName)

                    if (ticksToTpOut == countdownStartingWithSecondsLeft.seconds())
                    {
                        sendTpOutMessage(serverPlayerEntity, countdownStartingWithSecondsLeft)
                    }

                    if (ticksToTpOut < 0)
                    {
                        exitShop(playerName)
                    }
                }
            } else currentlyInShop.remove(playerName)

            if (playerName !in currentlyInShop && serverPlayerEntity.onGround())
            {
                if (!Util.getBlockAt(BlockPos.from(serverPlayerEntity.position()).relative(0, -1, 0))
                        .isSame(Blocks.AIR)
                )
                {
                    lastPosOutOfShop[playerName] =
                        TPPos(serverPlayerEntity.position(), serverPlayerEntity.yRot, serverPlayerEntity.xRot)
                }
            }
        }
    }

    fun sendTpOutMessage(player: ServerPlayer, secondsLeft: Int)
    {
        if (secondsLeft > 0 && currentlyInShop.contains(player.name.string))
        {
            player.sendPrivateMessage(I18n.get("shopTpOut", mapOf("seconds" to secondsLeft)))
            Timer.schedule(1.seconds()) { sendTpOutMessage(player, secondsLeft - 1) }
        }
    }

    private fun clearIllegalItems(player: ServerPlayer)
    {
        val illegalItems = listOf(Items.GLASS_BOTTLE, Items.BUCKET)
        player.inventory.clearOrCountMatchingItems(
            { itemStack -> itemStack.item in illegalItems },
            -1,
            player.inventoryMenu.craftSlots
        )
    }

    /**
     * forces player to move to spawn with respawn effects, no respawn items, extinguished, shop parameters reset and menus closed
     */
    fun exitShop(player: ServerPlayer)
    {
        val playerName = player.name.string

        SpawnManager.spawnPlayer(player, giveItems = false)
        player.extinguishFire()
        timeInShop[playerName] = 0
        player.closeContainer()
        Shop.clearRecentlyBought(playerName)
    }

    fun exitShop(playerName: String)
    {
        val player = PlayerManager.getOnlinePlayer(playerName) ?: return
        exitShop(player)

    }

    override fun reset()
    {
        currentlyInShop.clear()
        timeInShop.clear()
    }
}

data class TPPos(val pos: Vec3, val yaw: Float, val pitch: Float)