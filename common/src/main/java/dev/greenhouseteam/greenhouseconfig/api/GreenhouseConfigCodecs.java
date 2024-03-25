package dev.greenhouseteam.greenhouseconfig.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public class GreenhouseConfigCodecs {
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return ExtraCodecs.strictOptionalField(codec, name).xmap(
                o -> o.orElse(defaultValue),
                Optional::of
        );
    }
}
