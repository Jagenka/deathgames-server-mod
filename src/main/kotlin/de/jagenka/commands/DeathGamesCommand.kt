package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.jagenka.DeathGames
import de.jagenka.Util.ifServerLoaded
import de.jagenka.config.Config
import de.jagenka.managers.DisplayManager
import de.jagenka.managers.PlayerManager.addToDGTeam
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.isOp
import de.jagenka.managers.PlayerManager.kickFromDGTeam
import de.jagenka.managers.SpawnManager
import de.jagenka.team.DGTeam
import de.jagenka.timer.Timer
import de.jagenka.util.I18n
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DeathGamesCommand
{
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
    {
        val literalArgumentBuilder = Commands.literal("deathgames")
            .then(literal("start")
                .requires { it.isOp() }
                .executes {
                    if (!DeathGames.running) DeathGames.startGameWithCountdown()
                    return@executes 0
                })
            .then(literal("stop")
                .requires { it.isOp() }
                .executes {
                    if (DeathGames.running) DeathGames.stopGame()
                    else it.source.sendFailure(Component.literal("Game is not running!"))
                    return@executes 0
                })
            .then(
                literal("timer")
                    .requires { it.isOp() }
                    .then(literal("resume").executes {
                        Timer.start()
                        it.source.sendSuccess({ Component.literal("Timer is now running.") }, false)
                        return@executes 0
                    })
                    .then(literal("pause").executes {
                        Timer.pause()
                        it.source.sendSuccess({ Component.literal("Timer is now paused.") }, false)
                        return@executes 0
                    })
                    .then(literal("reset").executes {
                        Timer.reset()
                        it.source.sendSuccess({ Component.literal("Timer is now reset.") }, false)
                        return@executes 0
                    })
            )
            .then(
                literal("join")
                    .then(argument("team", StringArgumentType.word()).suggests { _, builder ->
                        SharedSuggestionProvider.suggest(DGTeam.getValuesAsStringList(), builder)
                    }.executes {
                        handleJoinTeam(it, it.getArgument("team", String::class.java))
                        return@executes 0
                    }
                        .then(argument("player", StringArgumentType.word())
                            .requires { it.isOp() }
                            .suggests { context, builder ->
                                SharedSuggestionProvider.suggest(context.source.onlinePlayerNames, builder)
                            }.executes {
                                ifServerLoaded { minecraftServer ->
                                    val playerArgument = it.getArgument("player", String::class.java)
                                    val player = minecraftServer.playerList.getPlayer(playerArgument)
                                    if (player == null) it.source.sendFailure(Component.literal("$playerArgument is not a player!"))
                                    else handleJoinTeamForSomeoneElse(it, it.getArgument("team", String::class.java), player)
                                }

                                return@executes 0
                            })
                    )

            )
            .then(literal("leave").executes { context ->
                context.source.player?.let {
                    val leftTeam = handleLeaveTeam(context, it)
                    if (leftTeam == null) context.source.sendFailure(Component.literal("You're not part of a team!"))
                    else context.source.sendSuccess({ Component.literal("Successfully left $leftTeam.") }, false)
                } ?: context.source.sendFailure(Component.literal("You must be a player to do that!"))
                return@executes 0
            }
                .then(argument("player", StringArgumentType.word())
                    .requires { it.isOp() }
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(context.source.onlinePlayerNames, builder)
                    }.executes { context ->
                        ifServerLoaded { minecraftServer ->
                            val playerArgument = context.getArgument("player", String::class.java)
                            val player = minecraftServer.playerList.getPlayer(playerArgument)
                            if (player == null) context.source.sendFailure(Component.literal("$playerArgument is not a player!"))
                            else
                            {
                                val leftTeam = handleLeaveTeam(context, player)
                                if (leftTeam == null) context.source.sendFailure(Component.literal("${player.name.string} is not part of a team!"))
                                else context.source.sendSuccess(
                                    { Component.literal("Successfully kicked ${player.name.string} from $leftTeam.") },
                                    false
                                )
                            }
                        }
                        return@executes 0
                    })
            )
            .then(
                literal("shufflespawns")
                    .requires { it.isOp() }
                    .executes {
                        if (DeathGames.running) SpawnManager.shuffleSpawns()
                        else it.source.sendFailure(Component.literal("Game is not running!"))
                        return@executes 0
                    }
            )
            .then(
                literal("reloadConfig")
                    .requires { it.isOp() }
                    .executes {
                        try
                        {
                            Config.load()
                            I18n.loadI18n()
                            it.source.sendSuccess({ Component.literal("config reloaded") }, true)
                        } catch (e: Exception)
                        {
                            it.source.sendFailure(Component.literal("error reloading config"))
                        }
                        return@executes 0
                    }
            )

        val literalArgumentBuilderAfterConfig = DeathGamesConfigCommand.generateConfigCommand(literalArgumentBuilder)

        val baseLiteralCommandNode = dispatcher.register(literalArgumentBuilderAfterConfig)

        dispatcher.register(literal("dg").redirect(baseLiteralCommandNode))
        dispatcher.register(literal("deeznutz").redirect(baseLiteralCommandNode))
    }

    fun CommandSourceStack.isOp(): Boolean = this.player?.isOp() == true

    private fun handleLeaveTeam(context: CommandContext<CommandSourceStack>, player: ServerPlayer): DGTeam?
    {
        val dgTeam = player.getDGTeam()
        return if (dgTeam == null) null
        else
        {
            player.kickFromDGTeam()
            DisplayManager.displayMessageOnPlayerTeamJoin(player, null)
            dgTeam
        }
    }

    private fun handleJoinTeamForSomeoneElse(
        context: CommandContext<CommandSourceStack>,
        teamName: String,
        player: ServerPlayer
    )
    {
        if (DeathGames.running)
        {
            context.source.sendFailure(Component.literal("Cannot join while game is running!"))
            return
        }
        if (teamName !in DGTeam.getValuesAsStringList())
        {
            context.source.sendFailure(Component.literal("$teamName is not a valid team!"))
            return
        }

        val team = DGTeam.valueOf(teamName)

        if (player.addToDGTeam(team))
        {
            DisplayManager.displayMessageOnPlayerTeamJoin(player, team)
            context.source.sendSuccess(
                { Component.literal("Successfully added ${player.name.string} to $team.") },
                false
            )
        }
    }

    private fun handleJoinTeam(context: CommandContext<CommandSourceStack>, teamName: String)
    {
        if (DeathGames.running)
        {
            context.source.sendFailure(Component.literal("Cannot join while game is running!"))
            return
        }
        val player = context.source.player
        if (teamName !in DGTeam.getValuesAsStringList())
        {
            context.source.sendFailure(Component.literal("$teamName is not a valid team!"))
            return
        }

        val team = DGTeam.valueOf(teamName)

        player?.let {
            if (player.addToDGTeam(team))
            {
                DisplayManager.displayMessageOnPlayerTeamJoin(player, team)
                context.source.sendSuccess({ Component.literal("Successfully joined $team.") }, false)
            }
        } ?: context.source.sendFailure(Component.literal("You must be a player to do that!"))
    }
}
