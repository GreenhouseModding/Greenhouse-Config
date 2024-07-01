package dev.greenhouseteam.greenhouseconfig.impl.network;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record SyncGreenhouseConfigPacket(String configName, @Nullable Object config) implements CustomPacketPayload {
    public static final ResourceLocation ID = GreenhouseConfig.asResource("sync_config");
    public static final Type<SyncGreenhouseConfigPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncGreenhouseConfigPacket> STREAM_CODEC = StreamCodec.of(SyncGreenhouseConfigPacket::write, SyncGreenhouseConfigPacket::read);

    public static SyncGreenhouseConfigPacket read(FriendlyByteBuf buf) {
        String configName = buf.readUtf();
        if (!GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.containsKey(configName))
            return new SyncGreenhouseConfigPacket(configName, null);
        GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.get(configName);
        var streamCodec = holder.getNetworkCodec(holder.get());
        if (streamCodec == null)
            return new SyncGreenhouseConfigPacket(configName, null);
        Object newConfig = streamCodec.decode(buf);
        return new SyncGreenhouseConfigPacket(configName, newConfig);
    }

    public static void write(FriendlyByteBuf buf, SyncGreenhouseConfigPacket packet) {
        buf.writeUtf(packet.configName);
        GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.get(packet.configName);
        if (packet.config == null)
            throw new IllegalArgumentException("Could not sync non existent config with id '" + packet.configName + "',");
        var streamCodec = holder.getNetworkCodec(holder.get());
        if (streamCodec == null)
            throw new IllegalStateException("Could not sync non sync-able config.");
        streamCodec.encode(buf, packet.config);
    }

    public void handleConfiguration() {
        Minecraft.getInstance().execute(() -> {
            if (config == null)
                return;
            GreenhouseConfigHolder<Object> holder = (GreenhouseConfigHolder<Object>) GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.get(configName);
            GreenhouseConfigStorage.updateConfig(holder, config);
        });
    }

    public void handlePlay() {
        Minecraft.getInstance().execute(() -> {
            if (config == null)
                return;
            GreenhouseConfigHolder<Object> holder = (GreenhouseConfigHolder<Object>) GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.get(configName);
            GreenhouseConfigStorage.updateConfig(holder, config);

        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
