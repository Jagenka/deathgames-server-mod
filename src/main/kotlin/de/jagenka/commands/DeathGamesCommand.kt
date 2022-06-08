package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.jagenka.team.DGTeam
import de.jagenka.DeathGames
import de.jagenka.Util.ifServerLoaded
import de.jagenka.managers.PlayerManager.addToDGTeam
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.PlayerManager.kickFromDGTeam
import de.jagenka.managers.SpawnManager
import de.jagenka.timer.Timer
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object DeathGamesCommand
{
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>)
    {
        val baseLiteralCommandNode = dispatcher.register(
            literal("deathgames")
                .then(literal("start").executes {
                    if (!DeathGames.running) DeathGames.startGame()
                    return@executes 0
                })
                .then(literal("stop")
                    .requires { it.isOp() }
                    .executes {
                        if (DeathGames.running) DeathGames.stopGame()
                        else it.source.sendError(Text.of("Game is not running!"))
                        return@executes 0
                    })
                .then(literal("config")
                    .requires { it.isOp() }
                    .executes {
                        handleConfig(it)
                        return@executes 0
                    })
                .then(
                    literal("timer")
                        .requires { it.isOp() }
                        .then(literal("resume").executes {
                            Timer.start()
                            it.source.sendFeedback(Text.of("Timer is now running."), false)
                            return@executes 0
                        })
                        .then(literal("pause").executes {
                            Timer.pause()
                            it.source.sendFeedback(Text.of("Timer is now paused."), false)
                            return@executes 0
                        })
                        .then(literal("reset").executes {
                            Timer.reset()
                            it.source.sendFeedback(Text.of("Timer is now reset."), false)
                            return@executes 0
                        })
                )
                .then(
                    literal("join")
                        .then(argument("team", StringArgumentType.word()).suggests { _, builder ->
                            CommandSource.suggestMatching(DGTeam.getValuesAsStringList(), builder)
                        }.executes {
                            handleJoinTeam(it, it.getArgument("team", String::class.java))
                            return@executes 0
                        }
                            .then(argument("player", StringArgumentType.word())
                                .requires { it.isOp() }
                                .suggests { context, builder ->
                                    CommandSource.suggestMatching(context.source.playerNames, builder)
                                }.executes {
                                    ifServerLoaded { minecraftServer ->
                                        val playerArgument = it.getArgument("player", String::class.java)
                                        val player = minecraftServer.playerManager.getPlayer(playerArgument)
                                        if (player == null) it.source.sendError(Text.of("$playerArgument is not a player!"))
                                        else handleJoinTeamForSomeoneElse(it, it.getArgument("team", String::class.java), player)
                                    }

                                    return@executes 0
                                })
                        )

                )
                .then(literal("leave").executes { context ->
                    context.source.player?.let {
                        val leftTeam = handleLeaveTeam(context, it)
                        if (leftTeam == null) context.source.sendError(Text.of("You're not part of a team!"))
                        else context.source.sendFeedback(Text.of("Successfully left $leftTeam."), false)
                    }

                    return@executes 0
                }
                    .then(argument("player", StringArgumentType.word())
                        .requires { it.isOp() }
                        .suggests { context, builder ->
                            CommandSource.suggestMatching(context.source.playerNames, builder)
                        }.executes { context ->
                            ifServerLoaded { minecraftServer ->
                                val playerArgument = context.getArgument("player", String::class.java)
                                val player = minecraftServer.playerManager.getPlayer(playerArgument)
                                if (player == null) context.source.sendError(Text.of("$playerArgument is not a player!"))
                                else
                                {
                                    val leftTeam = handleLeaveTeam(context, player)
                                    if (leftTeam == null) context.source.sendError(Text.of("${player.name.string} is not part of a team!"))
                                    else context.source.sendFeedback(Text.of("Successfully kicked ${player.name.string} from $leftTeam."), false)
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
                            else it.source.sendError(Text.of("Game is not running!"))
                            return@executes 0
                        }
                )
        )

        dispatcher.register(literal("dg").redirect(baseLiteralCommandNode))
        dispatcher.register(literal("deeznutz").redirect(baseLiteralCommandNode))
    }

    private fun ServerCommandSource.isOp() = this.hasPermissionLevel(2)

    private fun handleLeaveTeam(context: CommandContext<ServerCommandSource>, player: ServerPlayerEntity): DGTeam?
    {
        val dgTeam = player.getDGTeam()
        return if (dgTeam == null) null
        else
        {
            player.kickFromDGTeam()
            dgTeam
        }
    }

    private fun handleJoinTeamForSomeoneElse(context: CommandContext<ServerCommandSource>, teamName: String, player: ServerPlayerEntity)
    {
        if (DeathGames.running)
        {
            context.source.sendError(Text.of("Cannot join while game is running!"))
            return
        }
        if (teamName !in DGTeam.getValuesAsStringList())
        {
            context.source.sendError(Text.of("$teamName is not a valid team!"))
            return
        }

        val team = DGTeam.valueOf(teamName)

        player.addToDGTeam(team)
        context.source.sendFeedback(Text.of("Successfully added ${player.name.string} to $team."), false)
    }

    private fun handleJoinTeam(context: CommandContext<ServerCommandSource>, teamName: String)
    {
        if (DeathGames.running)
        {
            context.source.sendError(Text.of("Cannot join while game is running!"))
            return
        }
        val player = context.source.player
        if (teamName !in DGTeam.getValuesAsStringList())
        {
            context.source.sendError(Text.of("$teamName is not a valid team!"))
            return
        }

        val team = DGTeam.valueOf(teamName)

        player?.addToDGTeam(team)
        context.source.sendFeedback(Text.of("Successfully joined $team."), false)
    }

    private fun handleConfig(context: CommandContext<ServerCommandSource>)
    {

    }
}
