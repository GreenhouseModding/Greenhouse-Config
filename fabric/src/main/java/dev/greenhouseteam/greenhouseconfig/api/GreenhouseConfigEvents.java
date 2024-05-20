package dev.greenhouseteam.greenhouseconfig.api;

import dev.greenhouseteam.greenhouseconfig.api.event.GreenhouseConfigLoadEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class GreenhouseConfigEvents {
    public static final Event<GreenhouseConfigLoadEvent<Object>> POST_LOAD = EventFactory.createArrayBacked(GreenhouseConfigLoadEvent.class, callbacks -> (holder, config, side) -> {
        for (GreenhouseConfigLoadEvent<Object> callback : callbacks) {
            callback.onConfigLoad(holder, config, side);
        }
    });

    public static final Event<GreenhouseConfigLoadEvent<Object>> POST_POPULATION = EventFactory.createArrayBacked(GreenhouseConfigLoadEvent.class, callbacks -> (holder, config, side) -> {
        for (GreenhouseConfigLoadEvent<Object> callback : callbacks) {
            callback.onConfigLoad(holder, config, side);
        }
    });
}
