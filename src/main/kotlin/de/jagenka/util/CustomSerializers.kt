package de.jagenka.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.BlockPos
import net.minecraft.core.PositionAndRotation
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.Vec3

@Serializable
@SerialName("BoundingBox")
private data class BoundingBoxSurrogate(
    @Serializable(with = BlockPosSerializer::class) val minCorner: BlockPos,
    @Serializable(with = BlockPosSerializer::class) val maxCorner: BlockPos
)

object BoundingBoxSerializer : KSerializer<BoundingBox>
{
    override val descriptor: SerialDescriptor
        get() = BoundingBoxSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BoundingBox)
    {
        encoder.encodeSerializableValue(
            BoundingBoxSurrogate.serializer(),
            BoundingBoxSurrogate(
                BlockPos(value.minX(), value.minY(), value.minZ()),
                BlockPos(value.maxX(), value.maxY(), value.maxZ())
            )
        )
    }

    override fun deserialize(decoder: Decoder): BoundingBox
    {
        val (minCorner, maxCorner) = decoder.decodeSerializableValue<BoundingBoxSurrogate>(BoundingBoxSurrogate.serializer())
        return BoundingBox.fromCorners(minCorner, maxCorner)
    }
}

@Serializable
@SerialName("BlockPos")
private data class BlockPosSurrogate(val x: Int, val y: Int, val z: Int)

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

@Serializable
@SerialName("Vec3")
private data class Vec3Surrogate(val x: Double, val y: Double, val z: Double)

object Vec3Serializer : KSerializer<Vec3>
{
    override val descriptor: SerialDescriptor
        get() = Vec3Surrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Vec3)
    {
        encoder.encodeSerializableValue(
            Vec3Surrogate.serializer(),
            Vec3Surrogate(value.x, value.y, value.z)
        )
    }

    override fun deserialize(decoder: Decoder): Vec3
    {
        val (x, y, z) = decoder.decodeSerializableValue(Vec3Surrogate.serializer())
        return Vec3(x, y, z)
    }
}

@Serializable
@SerialName("PositionAndRotation")
private data class PositionAndRotationSurrogate(
    @Serializable(with = Vec3Serializer::class) val position: Vec3,
    val yRot: Float,
    val xRot: Float,
)

object PositionAndRotationSerializer : KSerializer<PositionAndRotation>
{
    override val descriptor: SerialDescriptor
        get() = PositionAndRotationSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: PositionAndRotation)
    {
        encoder.encodeSerializableValue(
            PositionAndRotationSurrogate.serializer(),
            PositionAndRotationSurrogate(value.position(), value.yRot(), value.xRot())
        )
    }

    override fun deserialize(decoder: Decoder): PositionAndRotation
    {
        val (position, yRot, xRot) = decoder.decodeSerializableValue<PositionAndRotationSurrogate>(PositionAndRotationSurrogate.serializer())
        return PositionAndRotation.of(position, yRot, xRot)
    }
}