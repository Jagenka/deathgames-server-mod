package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin
{
    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void preventResultUpdating(ScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void preventOnContentChanged(Inventory inventory, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }
}
