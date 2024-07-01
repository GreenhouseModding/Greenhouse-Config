package dev.greenhouseteam.greenhouseconfig.api.event;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;

@FunctionalInterface
public interface GreenhouseConfigLoadEvent<T> {
    void onConfigLoad(GreenhouseConfigHolder<T> holder, T config, ConfigSide side);
}
