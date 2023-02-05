package de.jagenka.stats

import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.EntityDamageSource

enum class DamageType
{
    PROJECTILE, EXPLOSIVE, FALLING_BLOCK, OUT_OF_WORLD, FIRE, NEUTRAL, MAGIC, FALLING, CACTUS, MELEE, IN_FIRE, LIGHTNING_BOLT, ON_FIRE, LAVA, HOT_FLOOR, IN_WALL, CRAMMING, DROWN, STARVE, FLY_INTO_WALL, GENERIC, WITHER, ANVIL, DRAGON_BREATH, DRYOUT, SWEET_BERRY_BUSH, FREEZE, FALLING_STALACTITE, STALAGMITE;

    companion object
    {
        fun from(damageSource: DamageSource): DamageType
        {
            return if (damageSource.isExplosive) EXPLOSIVE
            else if (damageSource.isFromFalling) FALLING
            else if (damageSource.isFire) FIRE
            else if (damageSource.isMagic) MAGIC
            else if (damageSource.isProjectile) PROJECTILE
            else if (damageSource.isFallingBlock) FALLING_BLOCK
            else if (damageSource.isNeutral) NEUTRAL
            else if (damageSource.isOutOfWorld) OUT_OF_WORLD
            else if (damageSource == DamageSource.IN_FIRE) IN_FIRE
            else if (damageSource == DamageSource.LIGHTNING_BOLT) LIGHTNING_BOLT
            else if (damageSource == DamageSource.ON_FIRE) ON_FIRE
            else if (damageSource == DamageSource.LAVA) LAVA
            else if (damageSource == DamageSource.HOT_FLOOR) HOT_FLOOR
            else if (damageSource == DamageSource.IN_WALL) IN_WALL
            else if (damageSource == DamageSource.CRAMMING) CRAMMING
            else if (damageSource == DamageSource.DROWN) DROWN
            else if (damageSource == DamageSource.STARVE) STARVE
            else if (damageSource == DamageSource.CACTUS) CACTUS
            else if (damageSource == DamageSource.FALL) FALLING
            else if (damageSource == DamageSource.FLY_INTO_WALL) FLY_INTO_WALL
            else if (damageSource == DamageSource.OUT_OF_WORLD) OUT_OF_WORLD
            else if (damageSource == DamageSource.GENERIC) GENERIC
            else if (damageSource == DamageSource.MAGIC) MAGIC
            else if (damageSource == DamageSource.WITHER) WITHER
            else if (damageSource is EntityDamageSource && damageSource.name == "anvil") ANVIL
            else if (damageSource is EntityDamageSource && damageSource.name == "fallingBlock") FALLING_BLOCK
            else if (damageSource == DamageSource.DRAGON_BREATH) DRAGON_BREATH
            else if (damageSource == DamageSource.DRYOUT) DRYOUT
            else if (damageSource == DamageSource.SWEET_BERRY_BUSH) SWEET_BERRY_BUSH
            else if (damageSource == DamageSource.FREEZE) FREEZE
            else if (damageSource is EntityDamageSource && damageSource.name == "fallingStalactite") FALLING_STALACTITE
            else if (damageSource == DamageSource.STALAGMITE) STALAGMITE
            else MELEE
        }
    }
}