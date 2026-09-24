package de.jagenka.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixin
{
    // Not needed right now.

    // preventDrop moved to LivingEntityMixin
}
