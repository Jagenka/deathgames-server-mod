package de.jagenka.stats

import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.PlayerManager
import de.jagenka.shop.ShopEntry
import net.minecraft.entity.damage.DamageSource

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
        personalStats.gib(killer).kills.add(KillEntry(deceased, DamageType.from(damageSource)))
    }

    @JvmStatic
    fun handleDeathType(damageSource: DamageSource, playerName: String)
    {
        personalStats.gib(playerName).deaths.add(DamageType.from(damageSource))
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
