package dev.greenhouseteam.greenhouseconfig.platform;

import dev.greenhouseteam.greenhouseconfig.api.ConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class GreenhouseConfigFabricPlatformHelper implements GHConfigIPlatformHelper {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ConfigSide getSide() {
        return GreenhouseConfigFabric.isDedicatedServerContext() ? ConfigSide.SERVER : ConfigSide.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <T> void postLoadEvent(ConfigHolder<T> holder, T config, ConfigSide side) {
        GreenhouseConfigEvents.POST_LOAD.invoker().onConfigLoad((ConfigHolder<Object>) holder, config, side);
    }

    @Override
    public <T> void postPopulationEvent(ConfigHolder<T> holder, T config, ConfigSide side) {
        GreenhouseConfigEvents.POST_POPULATION.invoker().onConfigLoad((ConfigHolder<Object>) holder, config, side);
    }
}
