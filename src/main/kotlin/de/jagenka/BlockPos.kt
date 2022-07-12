package de.jagenka

import de.jagenka.gameplay.rendering.GPS.surface
import kotlinx.serialization.Serializable
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class BlockPos(val x: Int, val y: Int, val z: Int)
{
    fun relative(x: Int = 0, y: Int = 0, z: Int = 0) = BlockPos(this.x + x, this.y + y, this.z + z)

    infix fun distanceTo(vec3d: Vec3d): Double = sqrt((this.x.toCenter() - vec3d.x).pow(2) + (this.y - vec3d.y).pow(2) + (this.z.toCenter() - vec3d.z).pow(2))

    fun manhattanDistanceTo(other: BlockPos): Int = abs(other.x - this.x) + abs(other.y - this.y) + abs(other.z - this.z)

    fun hasInRange(pos: Vec3d, range: Double): Boolean = this distanceTo pos <= range

    operator fun plus(other: BlockPos) = this.relative(other.x, other.y, other.z)
    operator fun minus(other: BlockPos) = this.relative(-other.x, -other.y, -other.z)

    fun asMinecraftBlockPos(): net.minecraft.util.math.BlockPos = net.minecraft.util.math.BlockPos(x, y, z)
    fun toVec3d(): Vec3d = Vec3d(x.toCenter(), y.toDouble(), z.toCenter())

    fun getNeighbors(): Set<BlockPos>
    {
        return setOf(relative(x = -1), relative(x = 1), relative(y = -1), relative(y = 1), relative(z = -1), relative(z = 1))
    }

    fun canPlayerExistHere(): Boolean
    {
        return Util.canPlayerStandIn(this.relative(y = 1)) &&
                Util.canPlayerStandIn(this)
    }

    fun canPlayerStandHere(): Boolean
    {
        return canPlayerExistHere() &&
                !Util.canPlayerStandIn(this.relative(y = -1))
    }

    fun getPossibleWalkDestinations(): Set<BlockPos>
    {
        val possible = mutableSetOf<BlockPos>()

        getNeighbors().forEach { if (it.canPlayerStandHere()) possible.add(it) } // manhattan distance 1

        for (dx in setOf(-1, 1))
        {
            for (dz in setOf(-1, 1))
            {
                val diagonal = relative(x = dx, z = dz)
                if (relative(x = dx) in possible && relative(z = dz) in possible && diagonal.canPlayerStandHere())
                {
                    possible.add(diagonal) // manhattan distance 2, dy=0
                }
            }
        }

        for (dx in setOf(-1, 1))
        {
            val diagonalUp = relative(x = dx, y = 1)
            if (relative(y = 1).canPlayerExistHere() && diagonalUp.canPlayerStandHere())
            {
                possible.add(diagonalUp) // manhattan distance 2, dy=1
            }

            val diagonalDown = relative(x = dx).surface()
            if (relative(x = dx).canPlayerExistHere() && diagonalDown.canPlayerStandHere())
            {
                possible.add(diagonalDown) // manhattan distance 2, dy=-1
            }
        }
        for (dz in setOf(-1, 1))
        {
            val diagonalUp = relative(z = dz, y = 1)
            if (relative(y = 1).canPlayerExistHere() && diagonalUp.canPlayerStandHere())
            {
                possible.add(diagonalUp) // manhattan distance 2, dy=1
            }

            val diagonalDown = relative(z = dz).surface()
            if (relative(z = dz).canPlayerExistHere() && diagonalDown.canPlayerStandHere())
            {
                possible.add(diagonalDown) // manhattan distance 2, dy=-1
            }
        }


        return possible.toSet()
    }

    companion object
    {
        fun from(vec3d: Vec3d) = from(vec3d.x, vec3d.y, vec3d.z)
        fun from(x: Double, y: Double, z: Double) = BlockPos(x.floor(), y.floor(), z.floor())
        fun from(blockPos: net.minecraft.util.math.BlockPos) = BlockPos(blockPos.x, blockPos.y, blockPos.z)

        fun net.minecraft.util.math.BlockPos.toDGBlockPos() = from(this)
    }
}