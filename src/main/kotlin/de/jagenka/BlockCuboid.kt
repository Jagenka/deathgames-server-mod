package de.jagenka

import kotlinx.serialization.Serializable
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.min

@Serializable
class BlockCuboid
{
    val firstCorner: BlockPos
    val secondCorner: BlockPos

    constructor(firstCorner: BlockPos, secondCorner: BlockPos)
    {
        this.firstCorner = BlockPos(min(firstCorner.x, secondCorner.x), min(firstCorner.y, secondCorner.y), min(firstCorner.z, secondCorner.z))
        this.secondCorner = BlockPos(max(firstCorner.x, secondCorner.x), max(firstCorner.y, secondCorner.y), max(firstCorner.z, secondCorner.z))
    }

    fun contains(pos: BlockPos): Boolean
    {
        return (pos.x in firstCorner.x..secondCorner.x) && (pos.y in firstCorner.y..secondCorner.y) && (pos.z in firstCorner.z..secondCorner.z)
    }

    fun contains(pos: Vec3d): Boolean
    {
        return (pos.x in firstCorner.x.toFloat().rangeTo((secondCorner.x + 1).toFloat()))
                && (pos.y in firstCorner.y.toFloat().rangeTo((secondCorner.y + 1).toFloat()))
                && (pos.z in firstCorner.z.toFloat().rangeTo((secondCorner.z + 1).toFloat()))
    }

    fun containingBlockPositions(): Set<BlockPos>
    {
        val result = mutableSetOf<BlockPos>()
        for (x in firstCorner.x..secondCorner.x)
        {
            for (y in firstCorner.y..secondCorner.y)
            {
                for (z in firstCorner.z..secondCorner.z)
                {
                    result.add(BlockPos(x, y, z))
                }
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockCuboid

        if (firstCorner != other.firstCorner) return false
        if (secondCorner != other.secondCorner) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = firstCorner.hashCode()
        result = 31 * result + secondCorner.hashCode()
        return result
    }

    override fun toString() = "BlockCuboid(" + listOf(firstCorner, secondCorner).joinToString(", ") { it.toString() } + ")"

}