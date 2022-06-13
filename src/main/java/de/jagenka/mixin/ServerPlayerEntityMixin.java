package de.jagenka.mixin;

import de.jagenka.Testing;
import de.jagenka.managers.KillManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci)
    {
        try
        {
            LivingEntity primeAdversary = ((ServerPlayerEntity) (Object) this).getPrimeAdversary();
            if (primeAdversary instanceof ServerPlayerEntity)
            {
                KillManager.handlePlayerKill((ServerPlayerEntity) primeAdversary, (ServerPlayerEntity) (Object) this);
            }
        } catch (ClassCastException ignored)
        {
        }

        KillManager.handleDeath((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void dropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir)
    {
        Testing.dropTest(); // TODO: remove in release
        cir.setReturnValue(false);
        cir.cancel();
        ((ServerPlayerEntity) (Object) this).playerScreenHandler.updateToClient();
    }
}
