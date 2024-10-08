package dev.roanoke.rib.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RibPreInitCallback {

    Event<RibPreInitCallback> EVENT = EventFactory.createArrayBacked(RibPreInitCallback.class, (callbacks) -> () -> {
		for (RibPreInitCallback callback : callbacks) {
			callback.interact();
		}
	});

    void interact();


}
