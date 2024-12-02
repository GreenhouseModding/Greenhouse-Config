package house.greenhouse.greenhouseconfig.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderCodec;
import house.greenhouse.greenhouseconfig.impl.codec.DefaultedCodec;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderSetCodec;
import house.greenhouse.greenhouseconfig.impl.codec.CommentedCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class GreenhouseConfigCodecs {
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return new DefaultedCodec<>(name, codec, defaultValue).setPartial(() -> defaultValue);
    }

    public static <A> Codec<A> commentedCodec(Codec<A> codec, String... comments) {
        return new CommentedCodec<>(codec, comments);
    }

    public static <E> Codec<LateHolder<E>> lateHolderCodec(ResourceKey<Registry<E>> registry) {
        return new LateHolderCodec<>(registry).xmap(holder -> (LateHolder<E>)holder, holder -> holder);
    }

    public static <E> Codec<LateHolderSet<E>> lateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry).xmap(holder -> (LateHolderSet<E>)holder, holder -> holder);
    }
}