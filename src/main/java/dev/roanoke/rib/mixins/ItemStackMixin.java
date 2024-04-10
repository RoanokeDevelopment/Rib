package dev.roanoke.rib.mixins;

import dev.roanoke.rib.callbacks.ItemCraftedCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "onCraft", at = @At("HEAD"))
    private void onItemCrafted(World world, PlayerEntity player, int i, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity) {
            ItemCraftedCallback.EVENT.invoker()
                    .interact((ServerPlayerEntity) player, (ItemStack) (Object) this, i);
        }
    }

}
