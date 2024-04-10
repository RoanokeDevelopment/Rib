package dev.roanoke.rib.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ItemCraftedCallback {

    Event<ItemCraftedCallback> EVENT = EventFactory.createArrayBacked(ItemCraftedCallback.class,
            (listeners) -> (player, itemStack, amount) -> {

        for (ItemCraftedCallback listener: listeners) {
            listener.interact(player, itemStack, amount);
        }
        });


        void interact(ServerPlayerEntity player, ItemStack itemStack, Integer amount);
}
