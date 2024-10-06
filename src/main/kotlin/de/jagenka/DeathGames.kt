package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.minecraftServer
import de.jagenka.Util.teleport
import de.jagenka.commands.DeathGamesCommand
import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.managers.*
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.makeParticipating
import de.jagenka.shop.Shop
import de.jagenka.stats.StatManager
import de.jagenka.stats.StatsIO
import de.jagenka.timer.ShopTask
import de.jagenka.timer.Timer
import de.jagenka.timer.Timer.tick
import de.jagenka.timer.seconds
import de.jagenka.util.I18n
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Text.literal
import net.minecraft.text.Texts
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DeathGames : DedicatedServerModInitializer
{
    val logger: Logger = LoggerFactory.getLogger("deathgames-server-mod")
    lateinit var commandRegistryAccess: CommandRegistryAccess

    var running = false

    var currentlyStarting = false
    var currentlyEnding = false

    var gameId: Long? = null
        private set

    override fun onInitializeServer()
    {
        ServerLifecycleEvents.SERVER_STARTED.register { minecraftServer ->
            Util.onServerLoaded(minecraftServer)
        }

        ServerTickEvents.START_SERVER_TICK.register {
            if (!isEnabled) return@register
            tick()
        }

        ServerLivingEntityEvents.ALLOW_DAMAGE.register { livingEntity: LivingEntity, damageSource: DamageSource, _: Float ->
            //println(damageSource.name + " " + DamageTypes.FALL.value.path)
            return@register !(!Config.misc.enableFallDamage && livingEntity.isPlayer && damageSource.name == DamageTypes.FALL.value.path)
        }

        registerCommands()

        StatsIO.loadStats()

        logger.info("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, commandRegistryAccess, _ ->
            DeathGames.commandRegistryAccess = commandRegistryAccess
            DeathGamesCommand.register(dispatcher)
        }
    }

    fun startGameWithCountdown()
    {
        if (!isEnabled) return
        if (currentlyStarting) return
        if (PlayerManager.getNonEmptyTeams().size < 2)
        {
            DisplayManager.sendChatMessage(literal(I18n.get("notEnoughTeams")).getWithStyle(Style.EMPTY.withColor(Formatting.RED))[0])
            return
        }

        currentlyStarting = true

        PlayerManager.getOnlinePlayers().forEach { player ->
            player.closeHandledScreen()
            DisplayManager.sendTitleMessage(player, literal("3"), literal(""), 1.seconds())
            Timer.schedule(1.seconds()) { DisplayManager.sendTitleMessage(player, literal("2"), literal(""), 1.seconds()) }
            Timer.schedule(2.seconds()) { DisplayManager.sendTitleMessage(player, literal("1"), literal(""), 1.seconds()) }
        }

        Timer.schedule(3.seconds()) { startGame() }
    }

    fun startGame()
    {
        if (!isEnabled) return

        gameId = System.currentTimeMillis()

        currentlyStarting = false

        val teamPlayers = PlayerManager.getTeamPlayers()

        StatManager.reset()
        KillManager.reset()
        MoneyManager.reset()
        Timer.reset()

        DisplayManager.reset()

        Shop.reset()

        KillManager.initLives()
        MoneyManager.initMoney()

        teamPlayers.forEach {
            it.clearStatusEffects()
            it.inventory.clear()
            it.health = 20f //set max hearts
            it.hungerManager.add(20, 1f) //set max food and saturation
            it.makeParticipating()
            it.changeGameMode(GameMode.ADVENTURE)
        }

        ifServerLoaded { server ->
            server.overworld.iterateEntities().toList().filter { it is ItemEntity || it is ProjectileEntity }.forEach { it.remove(Entity.RemovalReason.KILLED) }
        }

        PlayerManager.getOnlinePlayers().filter { it.getDGTeam() == null }.forEach { it.changeGameMode(GameMode.SPECTATOR) }

        SpawnManager.initSpawns()

        DisplayManager.showSidebar()

        val secondsToSpawnTp = Config.misc.startInShopTpAfterSeconds
        PlayerManager.getOnlinePlayers().forEach {
            it.closeHandledScreen()
            val (x, y, z) = Config.spawns.lobbySpawn
            it.setSpawnPoint(it.server.overworld.registryKey, BlockPos(x, y, z), 0f, true, false)

            if (Config.misc.startInShop)
            {
                it.teleport(Config.shopSettings.shopBounds.random().center)
                Timer.schedule((secondsToSpawnTp - 5).coerceAtLeast(0).seconds()) { ShopTask.sendTpOutMessage(it, 5) }
            }
        }

        if (Config.misc.startInShop)
        {
            ShopTask.tpOutActive = false
            DisplayManager.sendChatMessage(I18n.get("tpShopToSpawnGameStart", mapOf("time" to secondsToSpawnTp)))
            Timer.schedule(secondsToSpawnTp.seconds()) { postPrep() }
        } else
        {
            postPrep()
        }

        Timer.start()
        running = true
    }

    private fun postPrep()
    {
        minecraftServer?.let { server ->
            server.gameRules[GameRules.DO_DAYLIGHT_CYCLE].set(!Config.misc.freezeTime, server)
            server.overworld.timeOfDay = Config.misc.timeAtGameStart
        }

        PlayerManager.getOnlinePlayers().forEach {
            ShopTask.exitShop(it)
            DisplayManager.sendTitleMessage(it, Text.of(I18n.get("startTitle")), Text.of(I18n.get("startSubtitle")), 5.seconds())
        }

        ShopTask.tpOutActive = true
        Timer.gameMechsPaused = false
    }

    fun stopGame()
    {
        if (!isEnabled) return

        StatManager.gameStats.gameEnd = System.currentTimeMillis()

        currentlyEnding = true

        val winners = mutableListOf<Text>()
        val onlineParticipatingTeams = PlayerManager.getOnlineParticipatingTeams()
        onlineParticipatingTeams.forEach { team ->
            winners.add(team.getFormattedText())
        }
        val winnerCount = onlineParticipatingTeams.count()
        val winnerPlayers = Texts.join(winners, Text.of(", "))
        winners.clear()
        if (winnerCount != 0)
        {
            winners.add(
                Text.of(if (winnerCount != 1) I18n.get("winnerPlural") else I18n.get("winnerSingular"))
            )
            winners.add(winnerPlayers)
        }
        PlayerManager.getOnlinePlayers().forEach {
            DisplayManager.sendTitleMessage(it, Text.of(I18n.get("endTitle")), Texts.join(winners, Text.of(": ")), 5.seconds())
        }

        if (winnerCount == 1)
        {
            StatManager.gameStats.winner = onlineParticipatingTeams.getOrNull(0)
        }

        DisplayManager.resetBossBars()

        MoneyManager.reset()
        DisplayManager.updateLevelDisplay()

        BonusManager.disableAllPlatforms()

        PlayerManager.getOnlinePlayers().forEach { it.changeGameMode(GameMode.SPECTATOR) }

        DisplayManager.sendChatMessage("")
        DisplayManager.sendChatMessage(literal("Player K/Ds:").getWithStyle(Style.EMPTY.withBold(true))[0])
        StatManager.getKDs().forEach { (playerName, kills, deaths) ->
            DisplayManager.sendChatMessage("$playerName: $kills / $deaths")
        }
        DisplayManager.sendChatMessage("")

        StatManager.saveAllStatsAfterGame()

        Timer.schedule(10.seconds()) {
            PlayerManager.getOnlinePlayers().forEach {
                it.changeGameMode(GameMode.ADVENTURE)
                it.clearStatusEffects()
                it.inventory.clear()
                it.health = 20f //set max hearts
                it.hungerManager.add(20, 1f) //set max food and saturation
            }

            SpawnManager.resetSpawnColoring()

            PlayerManager.clearParticipatingStatusForEveryone()
            running = false
            currentlyEnding = false
        }
    }
}
