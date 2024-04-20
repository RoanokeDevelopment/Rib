package dev.roanoke.rib.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.ActionResult;

public interface RibInitCallback {

    Event<RibInitCallback> EVENT = EventFactory.createArrayBacked(RibInitCallback.class, (callbacks) -> () -> {
		for (RibInitCallback callback : callbacks) {
			callback.interact();
		}
	});

    void interact();

}
