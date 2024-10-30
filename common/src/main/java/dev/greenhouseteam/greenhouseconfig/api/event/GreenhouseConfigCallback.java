package dev.greenhouseteam.greenhouseconfig.api.event;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;

@FunctionalInterface
public interface GreenhouseConfigCallback {
    void onConfig(GreenhouseConfigHolder<?> holder, Object config, GreenhouseConfigSide side);
}
