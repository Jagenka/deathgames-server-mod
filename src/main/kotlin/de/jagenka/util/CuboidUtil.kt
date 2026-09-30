package de.jagenka.util

import de.jagenka.BlockPos
import de.jagenka.Coordinates
import de.jagenka.toCenter
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

// Util to replace BlockCuboid TODO: investigate BoundingBox class from Minecraft

fun Pair<BlockPos, BlockPos>.cuboidSmallCorner(): BlockPos
{
    return BlockPos(min(this.first.x, this.second.x), min(this.first.y, this.second.y), min(this.first.z, this.second.z))
}

fun Pair<BlockPos, BlockPos>.cuboidBigCorner(): BlockPos
{
    return BlockPos(max(this.first.x, this.second.x), max(this.first.y, this.second.y), max(this.first.z, this.second.z))
}

fun Pair<BlockPos, BlockPos>.cuboidCenter(): Coordinates
{
    val (dx, dy, dz) = this.cuboidSize()
    val smallCorner = this.cuboidSmallCorner()

    return Coordinates(smallCorner.x + dx / 2, smallCorner.y + dy / 2, smallCorner.z + dz / 2, 0f, 0f)
}

fun Pair<BlockPos, BlockPos>.cuboidSize(): Triple<Double, Double, Double>
{
    val bigCorner = this.cuboidBigCorner()
    val smallCorner = this.cuboidSmallCorner()

    return Triple(
        bigCorner.x.toCenter() - smallCorner.x.toCenter(),
        (bigCorner.y - smallCorner.y).toDouble(),
        bigCorner.z.toCenter() - smallCorner.z.toCenter()
    )
}

fun Pair<BlockPos, BlockPos>.cuboidContains(pos: BlockPos): Boolean
{
    return this.cuboidContains(pos.toVec3())
}

fun Pair<BlockPos, BlockPos>.cuboidContains(pos: Vec3): Boolean
{
    val bigCorner = this.cuboidBigCorner()
    val smallCorner = this.cuboidSmallCorner()

    return smallCorner.x <= pos.x && pos.x <= bigCorner.x &&
            smallCorner.y <= pos.y && pos.y <= bigCorner.y &&
            smallCorner.z <= pos.z && pos.z <= bigCorner.z
}