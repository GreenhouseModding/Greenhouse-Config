package dev.greenhouseteam.greenhouseconfig.platform;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;

import java.nio.file.Path;

public interface GHConfigIPlatformHelper {

    /**
     * Gets the enum value of the current platform
     *
     * @return The enum value of the current platform.
     */
    Platform getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the side for the config.
     * This will return SERVER whilst on a dedicated server, otherwise it will return client.
     *
     * @return The side.
     */
    ConfigSide getSide();

    Path getConfigPath();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    <T> void postLoadEvent(GreenhouseConfigHolder<T> holder, T config, ConfigSide side);

    <T> void postPopulationEvent(GreenhouseConfigHolder<T> holder, T config, ConfigSide side);
}