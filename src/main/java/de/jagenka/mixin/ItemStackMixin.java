package de.jagenka.mixin;

import de.jagenka.config.Config;
import de.jagenka.gameplay.graplinghook.GrapplingHook;
import de.jagenka.gameplay.traps.TrapManager;
import de.jagenka.shop.Shop;
import kotlin.jvm.optionals.OptionalsKt;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        // Traps
        if (context.getItemInHand().getItem() == Items.BAT_SPAWN_EGG)
        {
            if (TrapManager.handleTrapPlacement(context))
            {
                cir.setReturnValue(InteractionResult.PASS);
                cir.cancel();
            }
        }

        // No Ender Pearls in shop
        if (context.getItemInHand().getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(context.getPlayer()))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ItemStack stackInHand = player.getItemInHand(hand);

        // Grapple
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.getItemInHand(hand).getItem() == GrapplingHook.Companion.getItemItem())
        {
            ItemStack itemStackInHand = serverPlayer.getItemInHand(hand);
            CustomData customData = itemStackInHand.getComponents().get(DataComponents.CUSTOM_DATA);
            if (customData != null)
            {
                Optional<Double> hookMaxDistance = customData.tag.getDouble("hookMaxDistance");
                Optional<Integer> cooldownSetting = customData.tag.getInt("hookCooldown");

                if (hookMaxDistance.isPresent() && cooldownSetting.isPresent())
                {
                    GrapplingHook.Companion.getOrCreate(hookMaxDistance.get(), cooldownSetting.get(), GrapplingHook.Companion.getDefaultType())
                            .forceTheHooker(serverPlayer, itemStackInHand);
                }
            }
        }

        // No Ender Pearls in shop
        if (stackInHand.getItem() == Items.ENDER_PEARL && Shop.INSTANCE.isInShopBounds(player))
        {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    /**
     * Custom Data check for infinite Items
     */
    @Inject(method = "consume", at = @At("HEAD"), cancellable = true)
    private void preventConsumptionOnInfiniteItems(int amount, LivingEntity owner, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        CustomData customData = ((ItemStack) (Object) this).getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData != null && OptionalsKt.getOrDefault(customData.tag.getBoolean("infinite"), false))
        {
            ci.cancel();
            if (owner instanceof Player player)
            {
                player.inventoryMenu.sendAllDataToRemote();
            }
        }
    }
}
