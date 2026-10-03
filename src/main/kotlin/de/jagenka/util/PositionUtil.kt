package de.jagenka.util

import net.minecraft.core.BlockPos
import net.minecraft.core.PositionAndRotation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * @return Block position in which this double vector exists
 */
fun Vec3.surroundingBlockPos(): BlockPos
{
    return BlockPos.containing(this)
}

fun Vec3.withRotation(yRot: Float, xRot: Float): PositionAndRotation
{
    return PositionAndRotation.of(this, yRot, xRot)
}

/**
 * @return the center of this Block position as a double vector
 */
fun BlockPos.center(): Vec3
{
    return Vec3.atCenterOf(this)
}

/**
 * @return the bottom beneath the center of this Block position as a double vector
 */
fun BlockPos.bottomCenter(): Vec3
{
    return Vec3.atBottomCenterOf(this)
}

/**
 * Helper fun to teleport ServerPlayer to a PositionAndRotation
 * @return if tp was successful, just like {@link ServerPlayer#teleportTo(net.minecraft.server.level.ServerLevel, double, double, double, java.util.Set<net.minecraft.world.entity.Relative>, float, float, boolean) label}
 */
fun ServerPlayer.teleportTo(positionAndRotation: PositionAndRotation?): Boolean
{
    if (positionAndRotation == null) return false
    val (x, y, z, yRot, xRot) = positionAndRotation
    return this.teleportTo(level(), x, y, z, emptySet(), yRot, xRot, true)
}

operator fun PositionAndRotation.component1(): Double = this.position().x
operator fun PositionAndRotation.component2(): Double = this.position().y
operator fun PositionAndRotation.component3(): Double = this.position().z
operator fun PositionAndRotation.component4(): Float = this.yRot()
operator fun PositionAndRotation.component5(): Float = this.xRot()

operator fun BlockPos.component1(): Int = this.x
operator fun BlockPos.component2(): Int = this.y
operator fun BlockPos.component3(): Int = this.z

operator fun Vec3.component1(): Double = this.x
operator fun Vec3.component2(): Double = this.y
operator fun Vec3.component3(): Double = this.z

