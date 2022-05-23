package de.jagenka

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.math.pow
import kotlin.math.sqrt

@ConfigSerializable
data class Coordinates(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f)
{
    operator fun Coordinates.plus(other: Coordinates) = Coordinates(this.x + other.x, this.y + other.y, this.z + other.z, this.yaw, this.pitch)
    operator fun Coordinates.minus(other: Coordinates) = Coordinates(this.x - other.x, this.y - other.y, this.z - other.z, this.yaw, this.pitch)
    infix fun distanceTo(other: Coordinates) = (other - this).length()
    private fun length() = sqrt(this.x.pow(2) + this.y.pow(2) + this.z.pow(2))
}