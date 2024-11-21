package house.greenhouse.greenhouseconfig.api.event;

import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;

@FunctionalInterface
public interface GreenhouseConfigCallback {
    void onConfig(GreenhouseConfigHolder<?> holder, Object config, GreenhouseConfigSide side);
}
