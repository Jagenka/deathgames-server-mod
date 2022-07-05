package de.jagenka

import kotlinx.serialization.Serializable
import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class BlockPos(val x: Int, val y: Int, val z: Int)
{
    fun relative(x: Int, y: Int, z: Int) = BlockPos(this.x + x, this.y + y, this.z + z)

    infix fun distanceTo(vec3d: Vec3d): Double = sqrt((this.x.toCenter() - vec3d.x).pow(2) + (this.y - vec3d.y).pow(2) + (this.z.toCenter() - vec3d.z).pow(2))

    fun hasInRange(pos: Vec3d, range: Double): Boolean = this distanceTo pos <= range

    operator fun plus(other: BlockPos) = this.relative(other.x, other.y, other.z)
    operator fun minus(other: BlockPos) = this.relative(-other.x, -other.y, -other.z)

    fun asMinecraftBlockPos(): net.minecraft.util.math.BlockPos = net.minecraft.util.math.BlockPos(x, y, z)
    fun toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

    companion object
    {
        fun from(vec3d: Vec3d) = from(vec3d.x, vec3d.y, vec3d.z)
        fun from(x: Double, y: Double, z: Double) = BlockPos(x.floor(), y.floor(), z.floor())
    }
}