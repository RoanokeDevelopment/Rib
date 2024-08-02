package dev.roanoke.rib.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RegisterQuestCallback {

    Event<RegisterQuestCallback> EVENT = EventFactory.createArrayBacked(RegisterQuestCallback.class, (callbacks) -> () -> {
		for (RegisterQuestCallback callback : callbacks) {
			callback.interact();
		}
	});

    void interact();

}
