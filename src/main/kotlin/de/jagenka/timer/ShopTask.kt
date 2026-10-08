package de.jagenka.timer

import de.jagenka.Util
import de.jagenka.config.Config
import de.jagenka.isSame
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.ShopManager
import de.jagenka.managers.ShopManager.isOfficiallyInAShop
import de.jagenka.util.I18n
import de.jagenka.util.surroundingBlockPos
import de.jagenka.util.teleportTo
import de.jagenka.util.withRotation
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

object ShopTask : TimerTask
{
    private val lastPosOutOfShop = mutableMapOf<String, TPPos>()

    override val onlyInGame: Boolean
        get() = true
    override val isGameMechanic: Boolean
        get() = false
    override val runEvery: Int
        get() = 1.ticks()

    override fun run()
    {
        PlayerManager.getOnlinePlayers().forEach { player ->
            val playerName = player.name.string

            clearIllegalItems(player)

            if (PlayerManager.isCurrentlyDead(playerName)) return@forEach

            val shopBoundsIndex = ShopManager.getShopBoundsIndexContaining(player)

            if (player.isOfficiallyInAShop) // a player has legally entered a shop, an is currently in
            {
                // player left shop bounds
                if (shopBoundsIndex == null)
                {
                    // ignore shortly after respawning, as shop entering/exiting triggers more than once
                    if (!PlayerManager.hasRecentlyRespawned(playerName))
                    {
                        ShopManager.clear(player)
                    }
                } else // player is still in shop bounds
                {
                    // if in shop, add resistance
                    player.addEffect(MobEffectInstance(MobEffects.RESISTANCE, 1.seconds(), 255))
                }
            } else // player is not officially in a shop
            {
                var foundInShopBounds = false
                // but entered shop bounds
                if (shopBoundsIndex != null && shopBoundsIndex in Config.shopSettings.shopBounds.indices)
                {
                    foundInShopBounds = true
                    // if their shop is closed, yeet them out!
                    if (InactivePlayersTask.hasShopClosed(playerName))
                    {
                        lastPosOutOfShop[playerName]?.let {
                            player.teleportTo(it.pos.withRotation(it.yRot, it.xRot))
                            DisplayManager.sendTitleMessage(
                                player,
                                Component.literal(I18n.get("shopClosedTitle")),
                                Component.literal(I18n.get("shopClosedSubtitle")),
                                3.seconds()
                            )
                        }
                    } else
                    {
                        // shop open for them and they entered: welcome walk-in!
                        ShopManager.enterShop(player, ShopManager.EntryType.WALK, shopBoundsIndex)
                    }
                }

                // player is definitely in no shop
                if (!foundInShopBounds)
                {
                    // register their last position for shop close logic
                    if (player.onGround() && !Util.getBlockAt(player.position().surroundingBlockPos().below())
                            .isSame(Blocks.AIR)
                    )
                    {
                        lastPosOutOfShop[playerName] =
                            TPPos(player.position(), player.yRot, player.xRot)
                    }
                }
            }
        }
    }

    private fun clearIllegalItems(player: ServerPlayer)
    {
        val illegalItems = listOf(Items.GLASS_BOTTLE, Items.BUCKET)
        player.inventory.clearOrCountMatchingItems(
            { itemStack -> itemStack.item in illegalItems },
            false,
            -1,
            player.inventoryMenu.craftSlots
        )
    }

    override fun reset()
    {
        lastPosOutOfShop.clear()
    }
}

data class TPPos(val pos: Vec3, val yRot: Float, val xRot: Float)