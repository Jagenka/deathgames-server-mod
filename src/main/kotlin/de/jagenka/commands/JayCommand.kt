package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object JayCommand
{
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>)
    {
        dispatcher.register(
            CommandManager.literal("jay").executes {
                handle(it)
                return@executes 0
            }
        )
    }

    private fun handle(context: CommandContext<ServerCommandSource>)
    {

    }
}