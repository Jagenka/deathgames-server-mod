package de.jagenka.mixin;

import de.jagenka.DeathGames;
import de.jagenka.Kill;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
{
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci)
    {
        Entity attacker = damageSource.getAttacker();
        if (attacker != null)
        {
            if (attacker instanceof ServerPlayerEntity) DeathGames.registerKill(new Kill((ServerPlayerEntity) attacker, (ServerPlayerEntity) (Object) this));
            else System.out.println("attacker is not a ServerPlayerEntity");
        }
    }
}
