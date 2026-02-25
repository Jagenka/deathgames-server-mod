package de.jagenka.mixin;

import de.jagenka.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin
{
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void preventResultUpdating(AbstractContainerMenu abstractContainerMenu, ServerLevel serverLevel, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, @org.jspecify.annotations.Nullable RecipeHolder<CraftingRecipe> recipeHolder, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void preventOnContentChanged(Container container, CallbackInfo ci)
    {
        if (!Config.INSTANCE.isEnabled()) return;

        ci.cancel();
    }
}
