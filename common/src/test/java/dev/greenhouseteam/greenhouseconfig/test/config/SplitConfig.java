package dev.greenhouseteam.greenhouseconfig.test.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SplitConfig(TextColor color, ClientConfigValues clientValues) {
    public static final SplitConfig DEFAULT = new SplitConfig(TextColor.parseColor("#0095a8").getOrThrow(), ClientConfigValues.DEFAULT);

    public static final Codec<SplitConfig> SERVER_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("This is a value that exists on both the client and server."), TextColor.CODEC), "color", DEFAULT.color()).forGetter(SplitConfig::color)
    ).apply(inst, (t1) -> new SplitConfig(t1, null)));
    public static final Codec<SplitConfig> CLIENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("This is a value that exists on both the client and server."), TextColor.CODEC), "color", DEFAULT.color()).forGetter(SplitConfig::color),
            ClientConfigValues.CODEC.forGetter(SplitConfig::clientValues)
    ).apply(inst, SplitConfig::new));

    public static StreamCodec<FriendlyByteBuf, SplitConfig> streamCodec(SplitConfig clientConfig) {
        return ByteBufCodecs.fromCodec(TextColor.CODEC).<FriendlyByteBuf>cast().map(textColor -> new SplitConfig(textColor, clientConfig.clientValues()), SplitConfig::color);
    }

    public record ClientConfigValues(TextColor color) {
        public static final ClientConfigValues DEFAULT = new ClientConfigValues(TextColor.parseColor("#54bf6b").getOrThrow());

        public static final MapCodec<ClientConfigValues> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("This is a value that only exists on the client."), TextColor.CODEC), "client_color", DEFAULT.color()).forGetter(ClientConfigValues::color)
        ).apply(inst, ClientConfigValues::new));
    }
}
