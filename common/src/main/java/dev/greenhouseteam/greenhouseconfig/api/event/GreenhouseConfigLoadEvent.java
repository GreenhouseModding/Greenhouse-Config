package dev.greenhouseteam.greenhouseconfig.api.event;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.ConfigHolder;

@FunctionalInterface
public interface GreenhouseConfigLoadEvent<T> {
    void onConfigLoad(ConfigHolder<T> holder, T config, ConfigSide side);
}
