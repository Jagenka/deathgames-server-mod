package de.jagenka

import kotlinx.serialization.Serializable
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class Coordinates(val x: Int, val y: Int, val z: Int, val yaw: Float, val pitch: Float)
{
    constructor(pos: Vec3d, yaw: Float, pitch: Float) : this(pos.x, pos.y, pos.z, yaw, pitch)
    constructor(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) : this(x.floor(), y.floor(), z.floor(), yaw, pitch)

    @Deprecated("obsolete since Coordinates has Int xyz", level = DeprecationLevel.WARNING)
    infix fun distanceTo(other: Coordinates) = (other - this).length()
    infix fun distanceTo(pos: Vec3d) = sqrt((this.x.toCenter() - pos.x).pow(2) + (this.y - pos.y).pow(2) + (this.z.toCenter() - pos.z).pow(2))
    private fun length() = sqrt(this.x.toDouble().pow(2) + this.y.toDouble().pow(2) + this.z.toDouble().pow(2))

    fun relative(x: Double, y: Double, z: Double) = Coordinates(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch)
    fun relative(x: Int, y: Int, z: Int) = relative(x.toDouble(), y.toDouble(), z.toDouble())

    fun toVec3d() = Vec3d(x.toCenter(), y.toDouble(), z.toCenter())

    fun asBlockPos() = BlockPos(x, y, z)

    override fun toString() = "($x, $y, $z, y=%.2f, p=%.2f)".format(yaw, pitch)
}

fun Int.toCenter() = this + 0.5

operator fun Coordinates.plus(other: Coordinates) = Coordinates(this.x + other.x, this.y + other.y, this.z + other.z, this.yaw, this.pitch)
operator fun Coordinates.minus(other: Coordinates) = Coordinates(this.x - other.x, this.y - other.y, this.z - other.z, this.yaw, this.pitch)
operator fun Coordinates.times(value: Double) = Coordinates((this.x * value).floor(), (this.y * value).floor(), (this.z * value).floor(), this.yaw, this.pitch)
operator fun Coordinates.div(value: Double) = Coordinates((this.x / value).floor(), (this.y / value).floor(), (this.z / value).floor(), this.yaw, this.pitch)


//fun Vec3d.toDGCoordinates() = Coordinates(this)
fun ServerPlayerEntity.getDGCoordinates() = Coordinates(this.pos.x, this.pos.y, this.pos.z, this.yaw, this.pitch)