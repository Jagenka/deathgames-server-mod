package de.jagenka

import de.jagenka.Util.ifServerLoaded
import de.jagenka.Util.minecraftServer
import de.jagenka.Util.teleport
import de.jagenka.commands.DeathGamesCommand
import de.jagenka.config.Config
import de.jagenka.config.Config.isEnabled
import de.jagenka.gameplay.traps.TrapManager
import de.jagenka.managers.*
import de.jagenka.managers.PlayerManager.getDGTeam
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
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.Style
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.GameType
import net.minecraft.world.level.gamerules.GameRules
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DeathGames : DedicatedServerModInitializer
{
    val logger: Logger = LoggerFactory.getLogger("deathgames-server-mod")
    lateinit var commandBuildContext: CommandBuildContext

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
            return@register !(!Config.misc.enableFallDamage && livingEntity is Player && damageSource.`is`(DamageTypes.FALL))
        }

        registerCommands()

        StatsIO.loadStats()

        logger.info("DeathGames Mod initialized!")
    }

    private fun registerCommands()
    {
        CommandRegistrationCallback.EVENT.register { dispatcher, commandRegistryAccess, _ ->
            DeathGames.commandBuildContext = commandRegistryAccess
            DeathGamesCommand.register(dispatcher)
        }
    }

    fun startGameWithCountdown()
    {
        if (!isEnabled) return
        if (currentlyStarting) return
        if (PlayerManager.getNonEmptyTeams().size < 2)
        {
            DisplayManager.sendChatMessage(
                Component.literal(I18n.get("notEnoughTeams")).withColor(ChatFormatting.RED.color ?: 0)
            )
            return
        }

        currentlyStarting = true

        PlayerManager.getOnlinePlayers().forEach { player ->
            player.closeContainer()
            DisplayManager.sendTitleMessage(player, Component.literal("3"), Component.literal(""), 1.seconds())
            Timer.schedule(1.seconds()) {
                DisplayManager.sendTitleMessage(
                    player,
                    Component.literal("2"),
                    Component.literal(""),
                    1.seconds()
                )
            }
            Timer.schedule(2.seconds()) {
                DisplayManager.sendTitleMessage(
                    player,
                    Component.literal("1"),
                    Component.literal(""),
                    1.seconds()
                )
            }
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
            it.removeAllEffects()
            it.inventory.clearContent()
            it.health = 20f //set max hearts
            it.foodData.eat(20, 1f) //set max food and saturation
            PlayerManager.addParticipant(it.name.string)
            it.setGameMode(GameType.ADVENTURE)
        }

        // remove items drops and stuck projectiles from map
        ifServerLoaded { server ->
            server.overworld().allEntities.toList().filter {
                it is ItemEntity ||
                        it is Projectile ||
                        (it as? ArmorStand)?.customName?.string == "hook"
            }.forEach { it.remove(Entity.RemovalReason.KILLED) }
        }
        // remove previously laid traps
        TrapManager.reset()

        PlayerManager.getOnlinePlayers().filter { it.getDGTeam() == null }
            .forEach { it.setGameMode(GameType.SPECTATOR) }

        SpawnManager.initSpawns()

        DisplayManager.showSidebar()

        val secondsToSpawnTp = Config.misc.startInShopTpAfterSeconds
        PlayerManager.getOnlinePlayers().forEach {
            it.closeContainer()
            val (x, y, z) = Config.spawns.lobbySpawn
            it.adjustSpawnLocation(it.level(), BlockPos(x, y, z).asMinecraftBlockPos())

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
            server.worldData.gameRules.set(GameRules.ADVANCE_TIME, !Config.misc.freezeTime, server)
            server.overworld().dayTime = Config.misc.timeAtGameStart
        }

        PlayerManager.getOnlinePlayers().forEach {
            ShopTask.exitShop(it) // beginning the game is the same as exiting shop, even if game doesn't start in shop
            SpawnManager.giveRespawnItems(it) // we need do manually give respawn items, as leaving the shop should not give items
            DisplayManager.sendTitleMessage(
                it,
                Component.literal(I18n.get("startTitle")),
                Component.literal(I18n.get("startSubtitle")),
                5.seconds()
            )
        }

        // give compass to next bonus platform
        BonusManager.updateAllCompasses()

        ShopTask.tpOutActive = true
        Timer.gameMechsPaused = false
    }

    fun stopGame()
    {
        if (!isEnabled) return

        StatManager.gameStats.gameEnd = System.currentTimeMillis()

        currentlyEnding = true

        val winners = mutableListOf<Component>()
        val onlineParticipatingTeams = PlayerManager.getOnlineParticipatingTeams()
        onlineParticipatingTeams.forEach { team ->
            winners.add(team.getFormattedText())
        }
        val winnerCount = onlineParticipatingTeams.count()
        val winnerPlayers = ComponentUtils.formatList(winners, Component.literal(", "))
        winners.clear()
        if (winnerCount != 0)
        {
            winners.add(
                Component.literal(if (winnerCount != 1) I18n.get("winnerPlural") else I18n.get("winnerSingular"))
            )
            winners.add(winnerPlayers)
        }
        PlayerManager.getOnlinePlayers().forEach {
            DisplayManager.sendTitleMessage(
                it,
                Component.literal(I18n.get("endTitle")),
                ComponentUtils.formatList(winners, Component.literal(": ")),
                5.seconds()
            )
        }

        if (winnerCount == 1)
        {
            StatManager.gameStats.winner = onlineParticipatingTeams.getOrNull(0)
        }

        DisplayManager.resetBossBars()

        MoneyManager.reset()
        DisplayManager.updateLevelDisplay()

        BonusManager.disableAllPlatforms()

        PlayerManager.getOnlinePlayers().forEach { it.setGameMode(GameType.SPECTATOR) }

        DisplayManager.sendChatMessage("")
        DisplayManager.sendChatMessage(
            Component.literal("Player K/Ds:").toFlatList(Style.EMPTY.withBold(true))[0]
        ) // TODO: einfacher?
        StatManager.getKDs().forEach { (playerName, kills, deaths) ->
            DisplayManager.sendChatMessage("$playerName: $kills / $deaths")
        }
        DisplayManager.sendChatMessage("")

        StatManager.saveAllStatsAfterGame()

        Timer.schedule(10.seconds()) {
            PlayerManager.getOnlinePlayers().forEach {
                it.setGameMode(GameType.ADVENTURE)
                it.removeAllEffects()
                it.inventory.clearContent()
                it.health = 20f //set max hearts
                it.foodData.eat(20, 1f) //set max food and saturation
            }

            SpawnManager.resetSpawnColoring()

            PlayerManager.clearParticipatingStatusForEveryone()
            running = false
            currentlyEnding = false
        }
    }
}
