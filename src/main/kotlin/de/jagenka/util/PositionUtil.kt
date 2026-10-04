package de.jagenka.util

import net.minecraft.core.BlockPos
import net.minecraft.core.PositionAndRotation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState
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

fun Vec3.getBlockStatesBelow(level: BlockGetter): List<BlockState>
{
    return this.getBlockPossBelow().map { level.getBlockState(it) }
}

fun Vec3.getBlockPossBelow(): List<BlockPos>
{
    val result = mutableListOf<BlockPos>()

    if (this.x - this.x.toInt() == 0.0)
    {
        result.add(this.surroundingBlockPos())
        result.add(this.surroundingBlockPos().offset(-1, 0, 0))
    }

    if (this.z - this.z.toInt() == 0.0)
    {
        result.add(this.surroundingBlockPos())
        result.add(this.surroundingBlockPos().offset(0, 0, -1))
    }

    return result.map { it.below() }
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

