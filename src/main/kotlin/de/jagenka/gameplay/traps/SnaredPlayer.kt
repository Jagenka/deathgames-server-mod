package de.jagenka.gameplay.traps

import de.jagenka.Coordinates
import net.minecraft.server.network.ServerPlayerEntity

data class SnaredPlayer(val player: ServerPlayerEntity, var coordinates: Coordinates?)