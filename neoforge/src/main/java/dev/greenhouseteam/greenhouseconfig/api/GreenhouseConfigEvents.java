package dev.greenhouseteam.greenhouseconfig.api;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public class GreenhouseConfigEvents {
    public static class PostLoad<T> extends Event implements IModBusEvent {
        private final GreenhouseConfigHolder<T> holder;
        private final T config;
        private final ConfigSide side;

        private PostLoad(GreenhouseConfigHolder<T> holder, T config, ConfigSide side) {
            this.holder = holder;
            this.config = config;
            this.side = side;
        }

        public String getConfigName() {
            return holder.getConfigName();
        }

        public GreenhouseConfigHolder<T> getHolder() {
            return holder;
        }

        public T getConfig() {
            return config;
        }

        public ConfigSide getSide() {
            return side;
        }

        @ApiStatus.Internal
        public static <T> void post(GreenhouseConfigHolder<T> modId, T config, ConfigSide side) {
            PostLoad<T> event = new PostLoad<T>(modId, config, side);
            ModLoader.postEvent(event);
        }
    }

    public static class PostPopulation<T> extends Event implements IModBusEvent {
        private final GreenhouseConfigHolder<T> holder;
        private final T config;
        private final ConfigSide side;

        private PostPopulation(GreenhouseConfigHolder<T> holder, T config, ConfigSide side) {
            this.holder = holder;
            this.config = config;
            this.side = side;
        }

        public String getConfigName() {
            return holder.getConfigName();
        }

        public GreenhouseConfigHolder<T> getHolder() {
            return holder;
        }

        public T getConfig() {
            return config;
        }

        public ConfigSide getSide() {
            return side;
        }

        @ApiStatus.Internal
        public static <T> void post(GreenhouseConfigHolder<T> modId, T config, ConfigSide side) {
            PostLoad<T> event = new PostLoad<T>(modId, config, side);
            ModLoader.postEvent(event);
        }
    }
}
