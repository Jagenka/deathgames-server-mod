package de.jagenka.stats

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.shop.ShopEntry
import net.minecraft.entity.damage.DamageSource
import net.minecraft.stat.Stat
import net.minecraft.stat.Stats
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object StatManager
{
    val gameStats = GameEntry()
    val personalStats = mutableMapOf<String, PersonalGameEntry>().withDefault { PersonalGameEntry() }

    fun addBoughtItem(playerName: String, shopEntry: ShopEntry)
    {
        val itemsBought = personalStats.gib(playerName).itemsBought.toMutableList()
        itemsBought.add(shopEntry.nameForStat)
        personalStats.gib(playerName).itemsBought = itemsBought
    }

    @JvmStatic
    fun handleKillType(damageSource: DamageSource, killer: String, deceased: String)
    {
        personalStats.gib(killer).kills.add(KillEntry(deceased, DamageType.from(damageSource), System.currentTimeMillis()))
    }

    @JvmStatic
    fun handleDeathType(damageSource: DamageSource, playerName: String)
    {
        personalStats.gib(playerName).deaths.add(DeathEntry(DamageType.from(damageSource), System.currentTimeMillis()))
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

    /**
     * @return if saving was successful
     */
    fun saveAllStatsAfterGame(): Boolean
    {
        if (!DeathGames.currentlyEnding) return false

        gameStats.gameId = DeathGames.gameId ?: return false
        gameStats.captureEnabled = Config.captureEnabled

        StatsIO.stats.playedGames.add(gameStats)

        PlayerManager.getPlayers().forEach { playerName ->
            personalStats.gib(playerName).gameId = DeathGames.gameId ?: return false
            personalStats.gib(playerName).team = PlayerManager.getTeam(playerName)
            StatsIO.stats.playerEntries.getOrPut(playerName) { PlayerEntry() }.games.add(personalStats.gib(playerName))
        }

        StatsIO.store()
        return true
    }
}

fun <K, V> MutableMap<K, V>.gib(key: K): V = this.getOrPut(key) { this.getValue(key) }
