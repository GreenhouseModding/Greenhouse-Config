package dev.greenhouseteam.greenhouseconfig.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;

public class GreenhouseConfigCodecs {
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return codec.optionalFieldOf(name).xmap(
                o -> o.orElse(defaultValue),
                Optional::of
        );
    }

    public static <A> Codec<A> jsonCCodec(List<String> comments, Codec<A> codec) {
        return new JsonCCodec<>(comments, codec);
    }
}
