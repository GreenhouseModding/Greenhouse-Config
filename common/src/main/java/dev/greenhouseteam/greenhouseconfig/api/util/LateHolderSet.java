package dev.greenhouseteam.greenhouseconfig.api.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.mixin.HolderSetNamedAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface LateHolderSet<T> extends HolderSet<T> {

    ResourceKey<Registry<T>> registryKey();

    void bind(HolderLookup.RegistryLookup<T> registry);

    static <T> void bind(HolderLookup.RegistryLookup<T> registry, HolderSet<T> holderSet) {
        if (holderSet instanceof LateHolderSet<T> set)
            set.bind(registry);
    }

    @Override
    default boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return true;
    }

    default void bind(HolderLookup.Provider registries) {
        bind(registries.lookupOrThrow(registryKey()));
    }

    class Direct<T> extends HolderSet.ListBacked<T> implements LateHolderSet<T> {
        private final ResourceKey<Registry<T>> registry;
        private final List<ResourceKey<T>> keys;
        private List<Holder<T>> contents;

        public Direct(ResourceKey<Registry<T>> registry, List<ResourceKey<T>> keys) {
            this.registry = registry;
            this.keys = keys;
        }

        public String toString() {
            return "AlwaysSerializableDirectSet[" + this.keys + "]";
        }

        @Override
        protected List<Holder<T>> contents() {
            if (contents != null)
                return contents;
            return keys.stream().map(key -> (Holder<T>)Holder.Reference.createStandAlone(null, key)).toList();
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            if (contents != null)
                return Either.right(contents);
            return Either.right(keys.stream().map(key -> (Holder<T>)Holder.Reference.createStandAlone(null, key)).toList());
        }

        @Override
        public boolean contains(Holder<T> holder) {
            if (contents != null)
                return contents.contains(holder);
            ResourceKey<T> holderKey = holder.unwrapKey().orElse(null);
            if (holderKey == null)
                return false;
            return keys.stream().anyMatch(resourceKey -> resourceKey.isFor(holderKey.registryKey()) && resourceKey.location().equals(holderKey.location()));
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public ResourceKey<Registry<T>> registryKey() {
            return registry;
        }

        @Override
        public void bind(HolderLookup.RegistryLookup<T> registry) {
            ImmutableList.Builder<Holder<T>> builder = ImmutableList.builder();
            for (ResourceKey<T> key : keys) {
                if (!registry.get(key).isEmpty()) {
                    GreenhouseConfig.LOG.error("Could not read resource " + key.location() + " for registry " + key.registry() + ". Please check if it is valid.");
                    return;
                }
                builder.add(registry.get(key).get());
            }
            contents = builder.build();
        }
    }

    class Named<T> extends HolderSet.Named<T> implements LateHolderSet<T> {
        private final ResourceKey<Registry<T>> registry;

        public Named(ResourceKey<Registry<T>> registry, TagKey<T> tagKey) {
            super(null, tagKey);
            this.registry = registry;
        }

        public String toString() {
            String key = String.valueOf(this.key());
            return "AlwaysSerializableNamedSet(" + key + ")[" + this.contents() + "]";
        }

        @Override
        public ResourceKey<Registry<T>> registryKey() {
            return registry;
        }

        @Override
        public void bind(HolderLookup.RegistryLookup<T> registry) {
            if (registry.get(key()).isEmpty())
                GreenhouseConfig.LOG.error("Could not read resource " + key().location() + " for registry " + key().registry() + ". Please check if it is valid");
            ((HolderSetNamedAccessor)this).greenhouseconfig$invokeBind(registry.get(key()).get().stream().toList());
        }
    }

    class Mixed<T> extends HolderSet.ListBacked<T> implements LateHolderSet<T> {
        private final ResourceKey<Registry<T>> registry;
        private final List<Either<TagKey<T>, ResourceKey<T>>> keys;
        private List<Holder<T>> contents;

        public Mixed(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, ResourceKey<T>>> keys) {
            this.registry = registry;
            this.keys = List.copyOf(keys);
        }

        public Mixed(ResourceKey<Registry<T>> registry, List<TagKey<T>> tags, List<ResourceKey<T>> entries) {
            this.registry = registry;
            ImmutableList.Builder<Either<TagKey<T>, ResourceKey<T>>> builder = ImmutableList.builder();
            builder.addAll(tags.stream().map(Either::<TagKey<T>, ResourceKey<T>>left).toList());
            builder.addAll(entries.stream().map(Either::<TagKey<T>, ResourceKey<T>>right).toList());
            this.keys = builder.build();
        }

        public String toString() {
            return "AlwaysSerializableMixedSet[" + this.keys + "]";
        }

        public <T> T encode(DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder = ops.listBuilder();
            keys.forEach(key -> {
                key
                        .ifLeft(tagKey -> builder.add(ops.createString("#" + tagKey.location())))
                        .ifRight(resourceKey -> builder.add(ops.createString(resourceKey.location().toString())));
            });
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

        @Override
        public ResourceKey<Registry<T>> registryKey() {
            return registry;
        }

        @Override
        public void bind(HolderLookup.RegistryLookup<T> registry) {
            ImmutableList.Builder<Holder<T>> builder = ImmutableList.builder();
            keys.forEach(key -> {
                key
                        .ifLeft(tagKey -> {
                            if (registry.get(tagKey).isEmpty()) {
                                GreenhouseConfig.LOG.error("Could not read tag " + tagKey.location() + " for registry " + tagKey.registry() + ". Please check if it is valid.");
                                return;
                            }
                            builder.addAll(registry.get(tagKey).get().stream().toList());
                        })
                        .ifRight(resourceKey -> {
                            if (registry.get(resourceKey).isEmpty()) {
                                GreenhouseConfig.LOG.error("Could not read resource " + resourceKey.location() + " for registry " + resourceKey.registry() + ". Please check if it is valid");
                                return;
                                }
                            builder.add(registry.get(resourceKey).get());
                        });
            });
            contents = builder.build();
        }
    }
}