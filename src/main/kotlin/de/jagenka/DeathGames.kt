package de.jagenka

import de.jagenka.Util.ifServerLoaded
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
import de.jagenka.timer.seconds
import de.jagenka.util.I18n
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Text.literal
import net.minecraft.text.Texts
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode
import org.slf4j.LoggerFactory

object DeathGames : DedicatedServerModInitializer
{
    val logger = LoggerFactory.getLogger("deathgames-server-mod")

    var running = false

    var currentlyStarting = false
    var currentlyEnding = false

    var gameId: Long? = null
        private set

    override fun onInitializeServer()
    {
        registerCommands()

        StatsIO.loadStats()

        logger.info("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
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
            Timer.schedule({ DisplayManager.sendTitleMessage(player, literal("2"), literal(""), 1.seconds()) }, 1.seconds())
            Timer.schedule({ DisplayManager.sendTitleMessage(player, literal("1"), literal(""), 1.seconds()) }, 2.seconds())
        }

        Timer.schedule({ startGame() }, 3.seconds())
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

        SpawnManager.shuffleSpawns()

        val secondsToSpawnTp = Config.startInShopTpAfterSeconds

        PlayerManager.getOnlinePlayers().forEach {
            it.closeHandledScreen()
            val (x, y, z) = Config.lobbySpawn
            it.setSpawnPoint(it.server.overworld.registryKey, BlockPos(x, y, z), 0f, true, false)

            if (Config.startInShop)
            {
                it.teleport(Config.shopBounds.random().center)
                Timer.schedule({ ShopTask.sendTpOutMessage(it, 5) }, (secondsToSpawnTp - 5).coerceAtLeast(0).seconds())
            }
        }

        if (Config.startInShop)
        {
            ShopTask.tpOutActive = false
            DisplayManager.sendChatMessage(I18n.get("tpShopToSpawnGameStart", mapOf("time" to secondsToSpawnTp)))
            Timer.schedule({ postPrep() }, secondsToSpawnTp.seconds())
        } else
        {
            postPrep()
        }

        Timer.start()
        running = true
    }

    private fun postPrep()
    {
        PlayerManager.getOnlinePlayers().forEach {
            ShopTask.exitShop(it)
            DisplayManager.sendTitleMessage(it, Text.of(I18n.get("startTitle")), Text.of(I18n.get("startSubtitle")), 5.seconds())
        }

        DisplayManager.showSidebar()

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
                Text.of(if (winnerCount != 1) I18n.get("winnerPlural") else I18n.get("winnerSingular")) //TODO: change
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

        Timer.schedule({
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
        }, 10.seconds())
    }
}
