package dev.greenhouseteam.greenhouseconfig.api.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.mixin.HolderSetNamedAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LateHolderSet<T> extends HolderSet.ListBacked<T> {
    private final ResourceKey<Registry<T>> registry;
    private final List<Either<TagKey<T>, ResourceKey<T>>> keys;
    private List<Holder<T>> contents;

    public static <T> LateHolderSet<T> createFromTags(ResourceKey<Registry<T>> registry, List<TagKey<T>> tags) {
        return new LateHolderSet(registry, tags.stream().map(Either::left).toList());
    }

    public static <T> LateHolderSet<T> createFromEntries(ResourceKey<Registry<T>> registry, List<ResourceKey<T>> entries) {
        return new LateHolderSet(registry, entries.stream().map(Either::right).toList());
    }

    public static <T> LateHolderSet<T> createMixed(ResourceKey<Registry<T>> registry, List<TagKey<T>> tags, List<ResourceKey<T>> entries) {
        ImmutableList.Builder<Either<TagKey<T>, ResourceKey<T>>> builder = ImmutableList.builder();
        builder.addAll(tags.stream().map(Either::<TagKey<T>, ResourceKey<T>>left).toList());
        builder.addAll(entries.stream().map(Either::<TagKey<T>, ResourceKey<T>>right).toList());
        return new LateHolderSet(registry, builder.build());
    }

    protected LateHolderSet(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, ResourceKey<T>>> keys) {
        this.registry = registry;
        this.keys = List.copyOf(keys);
    }

    public static <T> void bind(HolderLookup.RegistryLookup<T> registry, HolderSet<T> holderSet, Consumer<String> onException) {
        if (!(holderSet instanceof LateHolderSet<T> late))
            return;
        ImmutableList.Builder<Holder<T>> builder = ImmutableList.builder();
        late.keys.forEach(key -> {
            key.ifLeft(tagKey -> {
                if (registry.get(tagKey).isEmpty()) {
                    onException.accept("Could not get tag " + tagKey.location() + " from registry " + tagKey.registry().location() + ".");
                    return;
                }
                builder.addAll(registry.getOrThrow(tagKey).stream().toList());
            }).ifRight(resourceKey -> {
                if (registry.get(resourceKey).isEmpty()) {
                    onException.accept("Could not get " + resourceKey.location() + " from registry " + resourceKey.registry() + ".");
                    return;
                }
                builder.add(registry.getOrThrow(resourceKey));
            });
        });
        late.contents = builder.build();
    }

    public String toString() {
        return "LateHolderSet[" + this.keys + "]";
    }

    public <E> E encode(DynamicOps<E> ops, E prefix) {
        if (keys.size() == 1) {
            return ops.createString(keys.get(0).map(
                    tagKey -> "#" + tagKey.location().toString(),
                    resourceKey -> resourceKey.location().toString())
            );
        }

        ListBuilder<E> builder = ops.listBuilder();
        for (Either<TagKey<T>, ResourceKey<T>> key : keys) {
            key
                    .ifLeft(tagKey -> builder.add(ops.createString("#" + tagKey.location())))
                    .ifRight(resourceKey -> builder.add(ops.createString(resourceKey.location().toString())));
        }
        return builder.build(prefix).getOrThrow();
    }

    @Override
    protected List<Holder<T>> contents() {
        if (contents != null)
            return contents;
        return List.of();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        if (contents != null)
            return Either.right(contents);
        return Either.right(List.of());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        if (contents != null)
            return contents.contains(holder);
        ResourceKey<T> holderKey = holder.unwrapKey().orElse(null);
        if (holderKey == null)
            return false;
        return keys.stream().anyMatch(either -> either.map(holder::is, resourceKey -> resourceKey.isFor(holderKey.registryKey()) && resourceKey.location().equals(holderKey.location())));
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    public ResourceKey<Registry<T>> registryKey() {
        return registry;
    }

    public List<Either<TagKey<T>, ResourceKey<T>>> keys() {
        return keys;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return true;
    }
}