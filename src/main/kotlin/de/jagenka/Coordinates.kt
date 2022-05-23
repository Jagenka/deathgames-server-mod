package de.jagenka

import net.minecraft.util.math.Vec3d
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.math.pow
import kotlin.math.sqrt

@ConfigSerializable
data class Coordinates(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f)
{
    constructor(pos: Vec3d) : this(pos.x, pos.y, pos.z)

    operator fun Coordinates.plus(other: Coordinates) = Coordinates(this.x + other.x, this.y + other.y, this.z + other.z, this.yaw, this.pitch)
    operator fun Coordinates.minus(other: Coordinates) = Coordinates(this.x - other.x, this.y - other.y, this.z - other.z, this.yaw, this.pitch)
    infix fun distanceTo(other: Coordinates) = (other - this).length()
    private fun length() = sqrt(this.x.pow(2) + this.y.pow(2) + this.z.pow(2))

    fun toVec3d() = Vec3d(x, y, z)
}

fun Vec3d.toDGCoordinates() = Coordinates(this)