package dev.greenhouseteam.greenhouseconfig.api.codec;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.mixin.HolderSetCodecAccessor;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class LateHolderSetCodec<E> extends HolderSetCodec<E> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public LateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
        super(registry, RegistryFixedCodec.create(registry), false);
        registryKey = registry;
    }

    public static <E> LateHolderSetCodec<E> create(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry);
    }

    @Override
    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> ops, T value) {
        if (ops.getStream(value).isSuccess() && ops.getStream(value).getOrThrow().anyMatch(t -> ops.getStringValue(t).isSuccess() && ops.getStringValue(t).getOrThrow().startsWith("#"))) {
            ImmutableList.Builder<Either<TagKey<E>, ResourceKey<E>>> keys = ImmutableList.builder();
            ops.getStream(value).getOrThrow().forEach(t -> {
                String string = ops.getStringValue(t).getOrThrow();
                if (string.startsWith("#"))
                    keys.add(Either.left(TagKey.create(registryKey, new ResourceLocation(string.substring(1)))));
                else
                    keys.add(Either.right(ResourceKey.create(registryKey, new ResourceLocation(string))));
            });
            return DataResult.success(Pair.of(new LateHolderSet.Mixed(registryKey, keys.build()), value));
        }
        return super.decode(ops, value);
    }

    @Override
    public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> ops, T prefix) {
        if (holderSet instanceof LateHolderSet.Mixed<E> mixed)
            return DataResult.success(mixed.encode(ops, prefix));
        if (holderSet instanceof HolderSet.Named<E> named)
            return DataResult.success(ops.createString("#" + named.key().location()));
        return ((HolderSetCodecAccessor<E>)this).greenhouseconfig$getHomogenousListCodec().encode(holderSet.stream().toList(), ops, prefix);
    }
}
