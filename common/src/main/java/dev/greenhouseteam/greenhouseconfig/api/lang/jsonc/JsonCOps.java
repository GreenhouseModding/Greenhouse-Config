package dev.greenhouseteam.greenhouseconfig.api.lang.jsonc;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class JsonCOps implements DynamicOps<CommentedJson> {
    public static final JsonCOps INSTANCE = new JsonCOps();

    @Override
    public String toString() {
        return "JSONC";
    }

    @Override
    public CommentedJson.Object empty() {
        return CommentedJson.Object.EMPTY;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, CommentedJson input) {
        Map<U, U> map = new HashMap<>();
        if (input instanceof CommentedJson.Object object) {
            for (Map.Entry<String, CommentedJson> entry : object.getMap().entrySet())
                map.put(outOps.createString(entry.getKey()), JsonOps.INSTANCE.convertTo(outOps, input.json()));
            return outOps.createMap(map);
        }
        return JsonOps.INSTANCE.convertTo(outOps, input.json());
    }

    @Override
    public DataResult<Number> getNumberValue(CommentedJson input) {
        if (input instanceof CommentedJson.Object)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getNumberValue(input.json());
    }

    @Override
    public CommentedJson createNumeric(Number i) {
        return new CommentedJson(new JsonPrimitive(i));
    }

    @Override
    public DataResult<String> getStringValue(CommentedJson input) {
        if (input instanceof CommentedJson.Object)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getStringValue(input.json());
    }

    @Override
    public CommentedJson createString(String value) {
        return new CommentedJson(new JsonPrimitive(value));
    }

    @Override
    public DataResult<Boolean> getBooleanValue(CommentedJson input) {
        if (input instanceof CommentedJson.Object)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getBooleanValue(input.json());
    }

    @Override
    public CommentedJson createBoolean(final boolean value) {
        return new CommentedJson(new JsonPrimitive(value));
    }

    @Override
    public DataResult<CommentedJson> mergeToList(final CommentedJson list, final CommentedJson value) {
        if (list == empty())
            return DataResult.success(empty());

        if (!(list.json() instanceof JsonArray) && list != empty())
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);

        final JsonArray result = new JsonArray();
        if (list.json() != JsonNull.INSTANCE)
            result.addAll((JsonArray)list.json());

        result.add(value.json());
        return DataResult.success(new CommentedJson(result, list.comments()));
    }

    @Override
    public DataResult<CommentedJson> mergeToList(final CommentedJson list, final List<CommentedJson> values) {
        if (list == null)
            return DataResult.error(() -> "mergeToList called with null.");

        if (!(list instanceof CommentedJson) && list != empty())
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);

        final JsonArray result = new JsonArray();
        if (list != empty())
            result.addAll((JsonArray)list.json());

        values.forEach(commented -> result.add(commented.json()));
        return DataResult.success(new CommentedJson(result, list.comments()));
    }

    @Override
    public DataResult<CommentedJson> mergeToMap(CommentedJson map, CommentedJson key, CommentedJson value) {
        if (!(map instanceof CommentedJson.Object) && map != empty())
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);

        if (!(key.json() instanceof JsonPrimitive || !key.json().getAsJsonPrimitive().isString()))
            return DataResult.error(() -> "key is not a string: " + key, map);

        final Map<String, CommentedJson> output = new HashMap<>();
        if (map != null && map != empty())
            output.putAll(((CommentedJson.Object) map).getMap());
        output.put(key.json().getAsString(), value);

        return DataResult.success(new CommentedJson.Object(output, map != null ? map.comments() : new String[]{}));
    }

    @Override
    public DataResult<CommentedJson> mergeToMap(final CommentedJson map, final MapLike<CommentedJson> values) {
        if (!(map instanceof CommentedJson.Object) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        final CommentedJson.Object output = new CommentedJson.Object(map != null ? map.comments() : new String[]{});
        if (map != null && map != empty())
            output.putAll(((CommentedJson.Object) map).getMap());

        final List<JsonElement> missed = Lists.newArrayList();

        values.entries().forEach(entry -> {
            final JsonElement key = entry.getFirst().json();
            if (!(key instanceof JsonPrimitive) || !key.getAsJsonPrimitive().isString()) {
                missed.add(key);
                return;
            }
            output.put(key.getAsString(), entry.getSecond());
        });

        if (!missed.isEmpty())
            return DataResult.error(() -> "some keys are not strings: " + missed, output);

        return DataResult.success(output);
    }
    @Override
    public DataResult<Stream<Pair<CommentedJson, CommentedJson>>> getMapValues(final CommentedJson input) {
        if (!(input instanceof CommentedJson.Object object))
            return DataResult.error(() -> "Not a JSON object: " + input);

        return DataResult.success(object.getMap().entrySet().stream().map(entry -> Pair.of(createString(entry.getKey()), entry.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<CommentedJson, CommentedJson>>> getMapEntries(final CommentedJson input) {
        if (!(input instanceof CommentedJson.Object object))
            return DataResult.error(() -> "Not a JSON object: " + input);

        return DataResult.success(c -> {
            for (final Map.Entry<String, CommentedJson> entry : object.getMap().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<CommentedJson>> getMap(final CommentedJson input) {
        if (!(input instanceof CommentedJson.Object object)) {
            return DataResult.error(() -> "Not a commented JSON object: " + input);
        }
        return DataResult.success(new MapLike<>() {
            @Nullable
            @Override
            public CommentedJson get(final CommentedJson key) {
                if (key.json() instanceof JsonPrimitive primitive && primitive.isString()) {
                    final CommentedJson element = object.getMap().get((primitive.getAsString()));
                    if (element.json() instanceof JsonNull)
                        return null;
                    return element;
                }
                throw new IllegalArgumentException("Could not get JsonElement from CommentedJson.Object from a non string primitive.");
            }

            @Nullable
            @Override
            public CommentedJson get(final String key) {
                final CommentedJson element = object.getMap().get(key);
                if (element.json() instanceof JsonNull) {
                    return null;
                }
                return element;
            }

            @Override
            public Stream<Pair<CommentedJson, CommentedJson>> entries() {
                return object.getMap().entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public CommentedJson createMap(final Stream<Pair<CommentedJson, CommentedJson>> map) {
        final CommentedJson.Object result = new CommentedJson.Object();
        map.forEach(p -> result.put(p.getFirst().json().getAsString(), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Stream<CommentedJson>> getStream(final CommentedJson input) {
        if (input.json() instanceof JsonArray array) {
            return DataResult.success(array.asList().stream().map(e -> e instanceof JsonNull ? null : new CommentedJson(e)));
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<CommentedJson>>> getList(final CommentedJson input) {
        if (input.json() instanceof JsonArray array) {
            return DataResult.success(c -> {
                for (final JsonElement element : array.asList()) {
                    c.accept(element instanceof JsonNull ? null : new CommentedJson(element));
                }
            });
        }
        return DataResult.error(() -> "Not a jsonc array: " + input);
    }

    @Override
    public CommentedJson createList(final Stream<CommentedJson> input) {
        final JsonArray result = new JsonArray();
        input.forEach(commented -> result.add(commented.json()));
        return new CommentedJson(result);
    }

    @Override
    public CommentedJson remove(final CommentedJson input, final String key) {
        if (input.json() instanceof JsonObject object) {
            final JsonObject result = new JsonObject();
            object.entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return new CommentedJson(result, input.comments());
        }
        return input;
    }

    @Override
    public ListBuilder<CommentedJson> listBuilder() {
        return new ArrayBuilder();
    }

    @Override
    public RecordBuilder<CommentedJson> mapBuilder() {
        return new JsonCRecordBuilder();
    }

    private static final class ArrayBuilder implements ListBuilder<CommentedJson> {
        private DataResult<CommentedJson> builder = DataResult.success(new CommentedJson(new JsonArray()), Lifecycle.stable());

        @Override
        public DynamicOps<CommentedJson> ops() {
            return INSTANCE;
        }

        @Override
        public ListBuilder<CommentedJson> add(final CommentedJson value) {
            builder = builder.map(b -> {
                ((JsonArray)b.json()).add(value.json());
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<CommentedJson> add(final DataResult<CommentedJson> value) {
            if (value.isError())
                return this;
            builder = builder.apply2stable((b, element) -> {
                ((JsonArray)b.json()).add(value.getOrThrow().json());
                return b;
            }, value);
            return this;
        }

        @Override
        public ListBuilder<CommentedJson> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<CommentedJson> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<CommentedJson> build(final CommentedJson prefix) {
            final DataResult<CommentedJson> result = builder.flatMap(b -> {
                if (prefix == ops().empty()) {
                    return DataResult.success(b, Lifecycle.stable());
                }

                if (!(prefix.json() instanceof JsonArray)) {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }

                final JsonArray array = new JsonArray();
                array.addAll((JsonArray) prefix.json());
                array.addAll((JsonArray) b.json());
                return DataResult.success(new CommentedJson(array), Lifecycle.stable());
            });

            builder = result;
            return result;
        }
    }

    private class JsonCRecordBuilder extends RecordBuilder.AbstractStringBuilder<CommentedJson, CommentedJson.Object> {
        protected JsonCRecordBuilder() {
            super(JsonCOps.this);
        }

        @Override
        protected CommentedJson.Object initBuilder() {
            return new CommentedJson.Object();
        }


        @Override
        protected CommentedJson.Object append(String key, CommentedJson value, CommentedJson.Object builder) {
            builder.put(key, value);
            return builder;
        }

        @Override
        protected DataResult<CommentedJson> build(final CommentedJson.Object builder, final CommentedJson prefix) {
            if (prefix == null || prefix == ops().empty()) {
                return DataResult.success(builder);
            }
            if (prefix instanceof CommentedJson.Object object) {
                final CommentedJson.Object result = new CommentedJson.Object();
                for (final Map.Entry<String, CommentedJson> entry : object.getMap().entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                for (final Map.Entry<String, CommentedJson> entry : builder.getMap().entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(result);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }
}
