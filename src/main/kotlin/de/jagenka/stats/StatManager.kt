package de.jagenka.stats

import de.jagenka.DeathGames
import de.jagenka.Util.minecraftServer
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.shop.ShopEntry
import de.jagenka.timer.Timer
import net.minecraft.entity.damage.DamageSource
import net.minecraft.stat.Stat
import net.minecraft.stat.Stats
import net.minecraft.util.WorldSavePath

object StatManager
{
    var gameStats = GameEntry()
        private set
    val personalStats = mutableMapOf<String, PersonalGameEntry>().withDefault { PersonalGameEntry() }

    fun reset()
    {
        gameStats = GameEntry()
        personalStats.clear()
    }

    fun addBoughtItem(playerName: String, shopEntry: ShopEntry, price: Int)
    {
        val itemsBought = personalStats.gib(playerName).itemsBought.toMutableList()
        itemsBought.add(ItemBoughtEntry(shopEntry.nameForStat, shopEntry.amount, price, Timer.now().toLong()))
        personalStats.gib(playerName).itemsBought = itemsBought
    }

    /**
     * @param price should be negative, as it will be given to player
     */
    fun addRecentlyRefunded(playerName: String, shopEntry: ShopEntry, price: Int)
    {
        val itemsBought = personalStats.gib(playerName).itemsBought.toMutableList()
        itemsBought.add(ItemBoughtEntry("${shopEntry.nameForStat}_REFUND_RECENT", shopEntry.amount, -price, Timer.now().toLong()))
        personalStats.gib(playerName).itemsBought = itemsBought
    }

    @JvmStatic
    fun handleKillType(damageSource: DamageSource, killer: String, deceased: String)
    {
        personalStats.gib(killer).kills.add(
            KillEntry(
                deceased,
                damageSource.name,
                Timer.now().toLong()
            )
        )
    }

    @JvmStatic
    fun handleDeathType(damageSource: DamageSource, playerName: String)
    {
        personalStats.gib(playerName).deaths.add(DeathEntry(damageSource.name, Timer.now().toLong()))
    }

    @JvmStatic
    fun addDamageTaken(playerName: String, amount: Float)
    {
        if (!DeathGames.running || DeathGames.currentlyEnding) return

        personalStats.gib(playerName).damageTaken += amount
    }

    @JvmStatic
    fun addDamageDealt(playerName: String, amount: Float)
    {
        if (!DeathGames.running || DeathGames.currentlyEnding) return

        personalStats.gib(playerName).damageDealt += amount
    }

    @JvmStatic
    fun handleStatIncrease(playerName: String, stat: Stat<*>, amount: Int)
    {
        if (!DeathGames.running || DeathGames.currentlyEnding) return

        when (stat.value)
        {
            Stats.WALK_ONE_CM -> personalStats.gib(playerName).cmMovedOnGround += amount
            Stats.SPRINT_ONE_CM -> personalStats.gib(playerName).cmMovedOnGround += amount
            Stats.CROUCH_ONE_CM -> personalStats.gib(playerName).cmMovedOnGround += amount

            Stats.FALL_ONE_CM -> personalStats.gib(playerName).cmFallen += amount

            Stats.FLY_ONE_CM -> personalStats.gib(playerName).cmFlown += amount //this tracks any forward and upward movement
            Stats.AVIATE_ONE_CM -> personalStats.gib(playerName).cmByElytra += amount

            Stats.SWIM_ONE_CM -> personalStats.gib(playerName).cmMovedInWater += amount
            Stats.WALK_ON_WATER_ONE_CM -> personalStats.gib(playerName).cmMovedInWater += amount
            Stats.WALK_UNDER_WATER_ONE_CM -> personalStats.gib(playerName).cmMovedInWater += amount

            Stats.CLIMB_ONE_CM -> personalStats.gib(playerName).cmClimbed += amount

            Stats.JUMP -> personalStats.gib(playerName).timesJumped += amount
        }
    }

    fun updateAccountBalanceAverage(playerName: String, currentAccountBalance: Int)
    {
        if (!DeathGames.running || DeathGames.currentlyEnding) return

        val playerStats = personalStats.gib(playerName)
        val currentAverage = playerStats.accountBalanceAverage
        val now = Timer.now().coerceAtLeast(1)
        playerStats.accountBalanceAverage =
            (currentAverage * (now - 1) + currentAccountBalance) / now
    }

    @JvmStatic
    fun addHealAmount(playerName: String, amount: Float)
    {
        if (!DeathGames.running || DeathGames.currentlyEnding) return

        personalStats.gib(playerName).healthRegenerated += amount
    }

    /**
     * @return list of tracked KDs sorted by ration. a list element is a triple of (playerName, kills, deaths)
     */
    fun getKDs(): List<Triple<String, Int, Int>>
    {
        val result = mutableListOf<Triple<String, Int, Int>>()
        personalStats.forEach { (playerName, personalGameEntry) ->
            result.add(Triple(playerName, personalGameEntry.kills.count(), personalGameEntry.deaths.count()))
        }
        return result.sortedByDescending {
            (it.second.toDouble() / it.third.coerceAtLeast(1).toDouble()) +
                    if (it.third == 0) 1 else 0 // zero deaths is better than one death
        }.filter { PlayerManager.getTeam(it.first) != null }
    }

    /**
     * @return if saving was successful
     */
    fun saveAllStatsAfterGame(): Boolean
    {
        if (!DeathGames.currentlyEnding) return false

        gameStats.gameId = DeathGames.gameId ?: return false

        gameStats.options["shuffleEnabled"] = Config.spawns.enableShuffle.toString()
        gameStats.options["shuffleInterval"] = Config.spawns.shuffleInterval.toString()
        gameStats.options["captureEnabled"] = Config.spawns.enableCapture.toString()
        gameStats.options["captureTimeNeeded"] = Config.spawns.captureTimeNeeded.toString()
        gameStats.options["bonusPlatformsEnabled"] = Config.bonus.enableBonusPlatforms.toString()
        gameStats.options["respawnsPerTeam"] = Config.respawns.perTeam.toString()
        gameStats.options["refundPercent"] = Config.shopSettings.refundPercent.toString()
        gameStats.options["startInShop"] = Config.misc.startInShop.toString()

        gameStats.map = minecraftServer?.getSavePath(WorldSavePath.ROOT)?.parent?.fileName.toString()

        StatsIO.storeGame(gameStats)

        PlayerManager.getPlayers().forEach { playerName ->
            personalStats.gib(playerName).gameId = DeathGames.gameId ?: return false
            personalStats.gib(playerName).team = PlayerManager.getTeam(playerName)
            StatsIO.storePlayer(playerName, personalStats.gib(playerName))
        }

        return true
    }
}

// TODO: remove? was macht das überhaupt? warum?
fun <K, V> MutableMap<K, V>.gib(key: K): V = this.getOrPut(key) { this.getValue(key) }
