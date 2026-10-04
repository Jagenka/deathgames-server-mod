package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.ShopManager.getSpawnInShop
import de.jagenka.timer.Timer
import de.jagenka.timer.inSeconds
import de.jagenka.timer.seconds
import de.jagenka.util.sendFailure
import de.jagenka.util.sendSuccess
import de.jagenka.util.teleportTo
import de.jagenka.util.withRotation
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal

object StuckCommand
{
    /**
     * set of player names, that cannot use stuck command right now
     */
    val onCooldown = mutableSetOf<String>()

    val cooldownTime = 30.seconds()

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
    {
        val literalArgumentBuilder = Commands.literal("stuck")
            .executes {
                val player = it.source.player ?: return@executes -1
                if (player.name.string in onCooldown)
                {
                    sendFailure(it.source, "Command is on cooldown!")
                    return@executes -1
                } else
                {
                    if (DeathGames.running)
                    {
                        val teleportDestination = getSpawnInShop(player.level()).withRotation(0f, 0f)
                        if (player.teleportTo(teleportDestination))
                        {
                            onCooldown += player.name.string
                            Timer.schedule(cooldownTime) {
                                onCooldown -= player.name.string
                            }
                            sendSuccess(it.source, "Unstuck! Command on cooldown for ${cooldownTime.inSeconds()} seconds.")
                            return@executes 0
                        } else
                        {
                            sendFailure(it.source, "Error :(")
                            return@executes -1
                        }
                    } else
                    {
                        val teleportDestination = Config.spawns.lobbySpawn
                        if (player.teleportTo(teleportDestination))
                        {
                            sendSuccess(it.source, "Unstuck!")
                            return@executes 0
                        } else
                        {
                            sendFailure(it.source, "Error :(")
                            return@executes -1
                        }
                    }
                }
            }

        val literalCommandNode = dispatcher.register(literalArgumentBuilder)
        dispatcher.register(literal("unstuck").redirect(literalCommandNode))
    }
}