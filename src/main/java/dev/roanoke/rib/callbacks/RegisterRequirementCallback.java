package dev.roanoke.rib.callbacks;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RegisterRequirementCallback {

    Event<RegisterRequirementCallback> EVENT = EventFactory.createArrayBacked(RegisterRequirementCallback.class, (callbacks) -> () -> {
		for (RegisterRequirementCallback callback : callbacks) {
			callback.interact();
		}
	});

    void interact();

}
