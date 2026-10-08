package de.jagenka.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.jagenka.DeathGames
import de.jagenka.config.Config
import de.jagenka.managers.ShopManager
import de.jagenka.managers.SpawnManager
import de.jagenka.timer.InactivePlayersTask
import de.jagenka.timer.Timer
import de.jagenka.timer.inSeconds
import de.jagenka.timer.seconds
import de.jagenka.util.sendFailure
import de.jagenka.util.sendSuccess
import de.jagenka.util.teleportTo
import de.jagenka.util.withRotation
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object StuckCommand
{
    /**
     * map of player names to ticks remaining, in which they cannot use stuck command
     */
    val onCooldown = mutableSetOf<String>()

    val cooldownTime = 30.seconds()
    val unstuckDelay = 5.seconds()

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
    {
        val stuckCommand = Commands.literal("stuck")
            .executes {
                return@executes doUnstucking(it)
            }
        val unstuckCommand = Commands.literal("unstuck")
            .executes {
                return@executes doUnstucking(it)
            }

        dispatcher.register(stuckCommand)
        dispatcher.register(unstuckCommand)
    }

    fun doUnstucking(it: CommandContext<CommandSourceStack>): Int
    {
        val player = it.source.player ?: return -1
        if (player.name.string in onCooldown)
        {
            sendFailure(it.source, "Command is on cooldown!")
            return -1
        } else
        {
            if (DeathGames.running)
            {
                it.source.sendSystemMessage(Component.literal("Un-stucking in ${unstuckDelay.inSeconds()} seconds..."))
                Timer.schedule(unstuckDelay) {
                    val teleportDestination = if (!InactivePlayersTask.hasShopClosed(player.name.string))
                    {
                        ShopManager.getSpawnInShop(player.level()).withRotation(0f, 0f)
                    } else
                    {
                        SpawnManager.getSpawnCoordinates(player)
                    }
                    if (player.teleportTo(teleportDestination))
                    {
                        onCooldown += player.name.string
                        Timer.schedule(cooldownTime) {
                            onCooldown -= player.name.string
                        }
                        sendSuccess(it.source, "Unstuck! Command on cooldown for ${cooldownTime.inSeconds()} seconds.")
                    } else
                    {
                        sendFailure(it.source, "Error :(")
                    }
                }
            } else
            {
                val teleportDestination = Config.spawns.lobbySpawn
                if (player.teleportTo(teleportDestination))
                {
                    sendSuccess(it.source, "Unstuck!")
                } else
                {
                    sendFailure(it.source, "Error :(")
                }
            }
        }
        return 0
    }
}