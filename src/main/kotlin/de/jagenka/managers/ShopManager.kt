package de.jagenka.managers

import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager.sendPrivateMessage
import de.jagenka.shop.Shop
import de.jagenka.timer.ScheduledTask
import de.jagenka.timer.Timer
import de.jagenka.timer.seconds
import de.jagenka.timer.ticks
import de.jagenka.util.*
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object ShopManager
{
    /**
     * list of playerNames currently in a shop according to this manager
     */
    private val inAShop = mutableSetOf<String>()

    /**
     * playerName to how they entered the Shop
     */
    private val didEnterHow = mutableMapOf<String, EntryType>()

    /**
     * playerName to shop index as used in Config `shopBounds` List
     */
    private val inWhichShop = mutableMapOf<String, Int>()

    /**
     * playerName to shop exit timer task - for cancellation
     */
    private val exitTasks = mutableMapOf<String, ScheduledTask>()

    /**
     * call this method, when a player should be considered in a shop
     * this method will teleport in, schedule teleport out, display the right messages, and keep track of who is where
     * @param player who entered
     * @param entryType how they entered
     * @param shopIndex index as used in Config `shopBounds` List. If index is not available, a random one will be chosen.
     */
    fun enterShop(player: ServerPlayer, entryType: EntryType, shopIndex: Int)
    {
        val playerName = player.name.string

        clear(player) // remove old info about the player being in a shop, the newer entry counts

        inAShop.add(playerName)

        // DeathGames.logger.info("$playerName entering shop $shopIndex via $entryType")

        val legalIndices = Config.shopSettings.shopBounds.indices
        val chosenIndex = if (shopIndex in legalIndices) shopIndex else legalIndices.random()

        val teleportLocation =
            getSpawnInShop(player.level(), chosenIndex)
                .withRotation(0f, 0f)

        didEnterHow[playerName] = entryType
        inWhichShop[playerName] = chosenIndex

        var tpDelay: Int  // in ticks

        when (entryType)
        {
            EntryType.WALK ->
            {
                // no teleport needed, as player walked into shop
                tpDelay = Config.shopSettings.tpOutOfShopAfter.ticks()
            }

            EntryType.GAME_START ->
            {
                player.teleportTo(teleportLocation)
                tpDelay = Config.misc.startInShopTpAfterSeconds.seconds()

            }

            EntryType.RESPAWN ->
            {
                player.teleportTo(teleportLocation)
                tpDelay = Config.misc.respawnInShopTpAfterSeconds.seconds()
            }
        }

        DisplayManager.sendTitleMessage(
            player,
            Component.literal(I18n.get("shopEnteredTitle")),
            Component.literal(I18n.get("shopEnteredSubtitle")),
            3.seconds()
        )

        Timer.schedule((tpDelay - 5.seconds()).coerceAtLeast(0).ticks()) { sendTpOutMessage(player, 5) }
        player.sendPrivateMessage(I18n.get("tpShopToSpawnInSec", mapOf("time" to tpDelay / 20)))
        exitTasks[playerName] = Timer.schedule(tpDelay) {
            exitShop(player)
        }
    }

    fun exitShop(player: ServerPlayer)
    {
        val playerName = player.name.string

        // DeathGames.logger.info("$playerName exited shop ${inWhichShop[playerName]}, came in like ${didEnterHow[playerName]}")

        clear(player)
        Shop.clearRecentlyBought(playerName)
        tpPlayerOut(player)
    }

    fun clear(player: ServerPlayer)
    {
        val playerName = player.name.string

        // DeathGames.logger.info("$playerName clearing shop ${inWhichShop[playerName]}, came in like ${didEnterHow[playerName]}")

        inAShop.remove(playerName)
        didEnterHow.remove(playerName)
        inWhichShop.remove(playerName)
        exitTasks[playerName]?.let { Timer.unscheduleTask(it) }
        exitTasks.remove(playerName)
    }

    fun tpPlayerOut(player: ServerPlayer)
    {
        SpawnManager.teleportPlayerToSpawn(player)
        player.extinguishFire()
        player.closeContainer()
    }

    fun sendTpOutMessage(player: ServerPlayer, secondsLeft: Int)
    {
        if (secondsLeft > 0 && player.isOfficiallyInAShop)
        {
            player.sendPrivateMessage(I18n.get("shopTpOut", mapOf("seconds" to secondsLeft)))
            Timer.schedule(1.seconds()) { sendTpOutMessage(player, secondsLeft - 1) }
        }
    }

    /**
     * get tp position for a specific shop or a random one if index is missing or invalid
     */
    fun getSpawnInShop(level: BlockGetter, index: Int = Config.shopSettings.shopBounds.indices.random()): Vec3
    {
        if (index !in Config.shopSettings.shopBounds.indices)
        {
            return getSpawnInShop(level)
        }

        val box = AABB.of(Config.shopSettings.shopBounds[index])
        var destination: Vec3? = null
        val bottomCenter = box.bottomCenter

        for (i in 0 until box.ysize.toInt())
        {
            val potential = bottomCenter.relative(Direction.UP, i.toDouble())
            if (potential.getBlockPossBelow().any { SpawnManager.canBeTeleportedOnTop(level, it) })
            {
                destination = potential
                break
            }
        }

        return destination ?: bottomCenter // default is shit, but i donut care
    }

    val ServerPlayer.isOfficiallyInAShop: Boolean
        get() = this.name.string in inAShop

    /**
     * @return index as set in {@link ShopSettingsConfigEntry#shopBounds config} where given player is inside. returns the first hit, or null if in none
     */
    fun getShopBoundsIndexContaining(player: Player?): Int?
    {
        if (player == null) return null

        val playerPos = player.position().surroundingBlockPos()
        Config.shopSettings.shopBounds.forEachIndexed { index, box ->
            if (box.isInside(playerPos)) return index
        }
        return null
    }

    val Player?.isInShopBounds: Boolean
        get() = getShopBoundsIndexContaining(this) != null

    fun reset()
    {
        inAShop.clear()
        didEnterHow.clear()
        inWhichShop.clear()
        exitTasks.values.forEach { Timer.unscheduleTask(it) }
        exitTasks.clear()
    }

    enum class EntryType
    {
        WALK, GAME_START, RESPAWN
    }
}