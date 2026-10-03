package de.jagenka.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.BlockPos
import net.minecraft.world.level.levelgen.structure.BoundingBox

@Serializable
@SerialName("BoundingBox")
data class BoundingBoxSurrogate(val minX: Int, val minY: Int, val minZ: Int, val maxX: Int, val maxY: Int, val maxZ: Int)

object BoundingBoxSerializer : KSerializer<BoundingBox>
{
    override val descriptor: SerialDescriptor
        get() = BoundingBoxSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BoundingBox)
    {
        encoder.encodeSerializableValue(
            BoundingBoxSurrogate.serializer(),
            BoundingBoxSurrogate(value.minX(), value.minY(), value.minZ(), value.maxX(), value.maxY(), value.maxZ())
        )
    }

    override fun deserialize(decoder: Decoder): BoundingBox
    {
        val (minX, minY, minZ, maxX, maxY, maxZ) = decoder.decodeSerializableValue(BoundingBoxSurrogate.serializer())
        return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }
}

@Serializable
@SerialName("BlockPos")
data class BlockPosSurrogate(val x: Int, val y: Int, val z: Int)

object BlockPosSerializer : KSerializer<BlockPos>
{
    override val descriptor: SerialDescriptor
        get() = BlockPosSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BlockPos)
    {
        encoder.encodeSerializableValue(
            BlockPosSurrogate.serializer(),
            BlockPosSurrogate(value.x, value.y, value.z)
        )
    }

    override fun deserialize(decoder: Decoder): BlockPos
    {
        val (x, y, z) = decoder.decodeSerializableValue(BlockPosSurrogate.serializer())
        return BlockPos(x, y, z)
    }
}