package de.jagenka.stats

import net.minecraft.entity.damage.DamageSource

enum class DamageType
{
    PROJECTILE, FIRE, NEUTRAL, MELEE, // legacy
    MAGIC, FALLING, CACTUS, EXPLOSIVE, FALLING_BLOCK, OUT_OF_WORLD,
    IN_FIRE, LIGHTNING_BOLT, ON_FIRE, LAVA, HOT_FLOOR, IN_WALL, CRAMMING, DROWN, STARVE, FLY_INTO_WALL, GENERIC, WITHER, ANVIL, DRAGON_BREATH, DRYOUT, SWEET_BERRY_BUSH, FREEZE, FALLING_STALACTITE, STALAGMITE,
    ERROR, STING, MOB, PLAYER, ARROW, TRIDENT, FIREWORKS, FIREBALL, WITHER_SKULL, THROWN, INDIRECT_MAGIC, THORNS, SONIC_BOOM, BAD_RESPAWN_POINT
;

    companion object
    {
        @JvmStatic
        fun from(damageSource: DamageSource): DamageType
        {
            // <- means tested
            return when (damageSource.name)
            {
                "inFire" -> IN_FIRE //
                "onFire" -> ON_FIRE //
                "lightningBolt" -> LIGHTNING_BOLT //
                "lava" -> LAVA //
                "hotFloor" -> HOT_FLOOR //
                "inWall" -> IN_WALL //
                "cramming" -> CRAMMING //
                "drown" -> DROWN //
                "starve" -> STARVE //
                "cactus" -> CACTUS //
                "fall" -> FALLING //
                "flyIntoWall" -> FLY_INTO_WALL //
                "outOfWorld" -> OUT_OF_WORLD //
                "generic" -> GENERIC
                "magic" -> MAGIC //
                "wither" -> WITHER //
                "dragonBreath" -> DRAGON_BREATH
                "dryout" -> DRYOUT
                "sweetBerryBush" -> SWEET_BERRY_BUSH //
                "freeze" -> FREEZE
                "stalagmite" -> STALAGMITE //
                "fallingBlock" -> FALLING_BLOCK
                "anvil" -> ANVIL //
                "fallingStalactite" -> FALLING_STALACTITE //
                "sting" -> STING //
                "mob" -> MOB //
                "player" -> PLAYER //
                "arrow" -> ARROW //
                "trident" -> TRIDENT //
                "fireworks" -> FIREWORKS //
                "fireball" -> FIREBALL //
                "witherSkull" -> WITHER_SKULL //
                "thrown" -> THROWN
                "indirectMagic" -> INDIRECT_MAGIC //
                "thorns" -> THORNS //
                "explosion", "explosion.player" -> EXPLOSIVE //
                "sonic_boom" -> SONIC_BOOM //
                "badRespawnPoint" -> BAD_RESPAWN_POINT
                else -> ERROR
            }
        }
    }
}