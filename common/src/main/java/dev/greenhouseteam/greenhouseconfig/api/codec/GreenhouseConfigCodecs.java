package dev.greenhouseteam.greenhouseconfig.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolder;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.impl.codec.LateHolderCodec;
import dev.greenhouseteam.greenhouseconfig.impl.util.LateHolderImpl;
import dev.greenhouseteam.greenhouseconfig.impl.util.LateHolderSetImpl;
import dev.greenhouseteam.greenhouseconfig.impl.codec.DefaultedCodec;
import dev.greenhouseteam.greenhouseconfig.impl.codec.LateHolderSetCodec;
import dev.greenhouseteam.greenhouseconfig.impl.codec.CommentedCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GreenhouseConfigCodecs {
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return new DefaultedCodec<>(name, codec, defaultValue).setPartial(() -> defaultValue);
    }

    public static <A> Codec<A> commentedCodec(Codec<A> codec, String... comments) {
        return new CommentedCodec<>(codec, comments);
    }

    public static <E> Codec<Holder<E>> lateHolderCodec(ResourceKey<Registry<E>> registry) {
        return new LateHolderCodec<>(registry);
    }

    public static <E> Codec<HolderSet<E>> lateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry);
    }
}