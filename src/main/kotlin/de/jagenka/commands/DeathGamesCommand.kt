package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import de.jagenka.DGTeam
import de.jagenka.DeathGames
import de.jagenka.Util
import de.jagenka.Util.sendPrivateMessage
import de.jagenka.timer.GameOverTask
import de.jagenka.timer.Timer
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

object DeathGamesCommand //TODO
{
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>)
    {
        dispatcher.register(
            literal("deathgames")
                .then(literal("start").executes {
                    DeathGames.startGame()
                    return@executes 0
                })
                .then(literal("stop")
                    .requires { DeathGames.running }
                    .executes {
                        DeathGames.stopGame()
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
                        .then(literal("resume").executes {
                            Timer.start()
                            it.source.player.sendPrivateMessage("Timer is now running.")
                            return@executes 0
                        })
                        .then(literal("pause").executes {
                            Timer.pause()
                            it.source.player.sendPrivateMessage("Timer is now paused.")
                            return@executes 0
                        })
                        .then(literal("reset").executes {
                            Timer.reset()
                            it.source.player.sendPrivateMessage("Timer is now reset.")
                            return@executes 0
                        })
                )
                .then(
                    literal("join")
                        .then(argument("team", DGTeamArgumentType()).suggests { _, builder ->
                            CommandSource.suggestMatching(DGTeam.getValuesAsStringList(), builder)
                        }.executes {
                            handleJoinTeam(it, it.getArgument("team", DGTeam::class.java))
                            return@executes 0
                        })
                )
        )
    }

    private fun ServerCommandSource.isOp() = this.hasPermissionLevel(2)

    private fun handleJoinTeam(context: CommandContext<ServerCommandSource>, team: DGTeam)
    {
        Util.sendChatMessage(team.toString())
    }

    private fun handleConfig(context: CommandContext<ServerCommandSource>)
    {
        Util.sendChatMessage("confick")
    }
}

class DGTeamArgumentType : ArgumentType<DGTeam>
{
    override fun parse(reader: StringReader?): DGTeam
    {
        DGTeam.values().forEach {
            if(it.name == (reader?.readString() ?: "")) return it
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader)
    }
}
