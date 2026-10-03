package de.jagenka.gameplay.traps

import net.minecraft.core.PositionAndRotation
import net.minecraft.server.level.ServerPlayer

data class SnaredPlayer(val player: ServerPlayer, var positionAndRotation: PositionAndRotation?)