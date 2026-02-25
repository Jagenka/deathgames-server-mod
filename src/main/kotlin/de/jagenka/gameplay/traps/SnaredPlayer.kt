package de.jagenka.gameplay.traps

import de.jagenka.Coordinates
import net.minecraft.server.level.ServerPlayer

data class SnaredPlayer(val player: ServerPlayer, var coordinates: Coordinates?)