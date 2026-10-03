package de.jagenka.util

// Util to replace BlockCuboid TODO: investigate BoundingBox class from Minecraft or AABB?

/*
fun Pair<BlockPos, BlockPos>.cuboidSmallCorner(): BlockPos
{
    return BlockPos(min(this.first.x, this.second.x), min(this.first.y, this.second.y), min(this.first.z, this.second.z))
}

fun Pair<BlockPos, BlockPos>.cuboidBigCorner(): BlockPos
{
    return BlockPos(max(this.first.x, this.second.x), max(this.first.y, this.second.y), max(this.first.z, this.second.z))
}

fun Pair<BlockPos, BlockPos>.cuboidCenter(): Vec3
{
    val (dx, dy, dz) = this.cuboidSize()
    val smallCorner = this.cuboidSmallCorner()

    return Vec3(smallCorner.x + dx / 2, smallCorner.y + dy / 2, smallCorner.z + dz / 2)
}

fun Pair<BlockPos, BlockPos>.cuboidSize(): Triple<Double, Double, Double>
{
    val bigCorner = this.cuboidBigCorner()
    val smallCorner = this.cuboidSmallCorner()

    return Triple(
        (bigCorner.x + 1 - smallCorner.x).toDouble(),
        (bigCorner.y + 1 - smallCorner.y).toDouble(),
        (bigCorner.z + 1 - smallCorner.z).toDouble()
    )
}

fun Pair<BlockPos, BlockPos>.cuboidContains(pos: BlockPos?): Boolean
{
    return pos != null && this.cuboidContains(pos.center())
}

fun Pair<BlockPos, BlockPos>.cuboidContains(pos: Vec3): Boolean
{
    val bigCorner = this.cuboidBigCorner()
    val smallCorner = this.cuboidSmallCorner()

    return smallCorner.x <= pos.x && pos.x <= bigCorner.x + 1 &&
            smallCorner.y <= pos.y && pos.y <= bigCorner.y + 1 &&
            smallCorner.z <= pos.z && pos.z <= bigCorner.z + 1
}
 */