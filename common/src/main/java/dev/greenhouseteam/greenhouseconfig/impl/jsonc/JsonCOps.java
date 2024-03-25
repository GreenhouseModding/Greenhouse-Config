package dev.greenhouseteam.greenhouseconfig.impl.jsonc;

import com.google.common.collect.Lists;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCArray;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCElement;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCNull;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCObject;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCPrimitive;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class JsonCOps implements DynamicOps<JsonCElement> {
    public static final JsonCOps INSTANCE = new JsonCOps();

    @Override
    public String toString() {
        return "JSONC";
    }

    @Override
    public JsonCElement empty() {
        return JsonCNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonCElement input) {
        if (input instanceof JsonCObject) {
            return convertMap(outOps, input);
        } else if (input instanceof JsonCArray) {
            return convertList(outOps, input);
        } else if (input instanceof JsonCPrimitive primitive) {
            return JsonOps.INSTANCE.convertTo(outOps, primitive.json());
        } else if (input instanceof JsonCNull) {
            return outOps.empty();
        }
        throw new IllegalArgumentException("Could not convert invalid JsonCElement.");
    }

    @Override
    public DataResult<Number> getNumberValue(JsonCElement input) {
        if (input instanceof JsonCPrimitive primitive && primitive.json().getAsJsonPrimitive().isNumber()) {
            return JsonOps.INSTANCE.getNumberValue(primitive.json());
        }
        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public JsonCPrimitive createNumeric(Number i) {
        return new JsonCPrimitive(new JsonPrimitive(i));
    }

    @Override
    public DataResult<String> getStringValue(JsonCElement input) {
        if (input instanceof JsonCPrimitive primitive) {
            return JsonOps.INSTANCE.getStringValue(primitive.json());
        }
        return DataResult.error(() -> "Not a string: " + input);
    }

    @Override
    public JsonCPrimitive createString(String value) {
        return new JsonCPrimitive(new JsonPrimitive(value));
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final JsonCElement input) {
        if (input instanceof JsonCPrimitive primitive) {
            if (primitive.json().getAsJsonPrimitive().isBoolean()) {
                return DataResult.success(primitive.json().getAsBoolean());
            } else if (primitive.json().getAsJsonPrimitive().isNumber()) {
                return DataResult.success(primitive.json().getAsNumber().byteValue() != 0);
            }
        }
        return DataResult.error(() -> "Not a boolean: " + input);
    }

    @Override
    public JsonCElement createBoolean(final boolean value) {
        return new JsonCPrimitive(new JsonPrimitive(value));
    }

    @Override
    public DataResult<JsonCElement> mergeToList(final JsonCElement list, final JsonCElement value) {
        if (list == empty()) {
            return DataResult.success(empty());
        }

        if (!(list instanceof JsonCArray) && list != empty()) {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final JsonCArray result = new JsonCArray();
        if (list != empty()) {
            result.addAll((JsonCArray)list);
        }
        result.addElement(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonCElement> mergeToList(final JsonCElement list, final List<JsonCElement> values) {
        if (!(list instanceof JsonCArray) && list != empty()) {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final JsonCArray result = new JsonCArray();
        if (list != empty()) {
            result.addAll((JsonCArray)list);
        }
        values.forEach(result::addElement);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonCElement> mergeToMap(JsonCElement map, JsonCElement key, JsonCElement value) {
        if (!(map instanceof JsonCObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        if (!(key instanceof JsonCPrimitive || !((JsonCPrimitive)key).json().getAsJsonPrimitive().isString())) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }

        final JsonCObject output = new JsonCObject();
        if (map != empty()) {
            ((JsonCObject)map).elements().forEach(output::addElement);
        }
        output.addElement(((JsonCPrimitive)key).json().getAsString(), value);

        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonCElement> mergeToMap(final JsonCElement map, final MapLike<JsonCElement> values) {

        if (!(map instanceof JsonCObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        final JsonCObject output = new JsonCObject();
        if (map != empty()) {
            ((JsonCObject)map).elements().forEach(output::addElement);
        }

        final List<JsonCElement> missed = Lists.newArrayList();

        values.entries().forEach(entry -> {
            final JsonCElement key = entry.getFirst();
            if (!(key instanceof JsonCPrimitive) || !((JsonCPrimitive) key).json().getAsJsonPrimitive().isString()) {
                missed.add(key);
                return;
            }
            output.addElement(((JsonCPrimitive) key).json().getAsString(), entry.getSecond());
        });

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }
    @Override
    public DataResult<Stream<Pair<JsonCElement, JsonCElement>>> getMapValues(final JsonCElement input) {
        if (!(input instanceof JsonCObject object)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(object.elements().entrySet().stream().map(entry -> Pair.of(createString(entry.getKey()), entry.getValue() instanceof JsonCNull ? null : entry.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<JsonCElement, JsonCElement>>> getMapEntries(final JsonCElement input) {
        if (!(input instanceof JsonCObject)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(c -> {
            for (final Map.Entry<String, JsonCElement> entry : ((JsonCObject) input).elements().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue() instanceof JsonCNull ? null : entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<JsonCElement>> getMap(final JsonCElement input) {
        if (!(input instanceof JsonCObject object)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(new MapLike<>() {
            @Nullable
            @Override
            public JsonCElement get(final JsonCElement key) {
                if (key instanceof JsonCPrimitive primitive && primitive.json().getAsJsonPrimitive().isString()) {
                    final JsonCElement element = object.elements().get((primitive.json().getAsString()));
                    if (element instanceof JsonCNull) {
                        return null;
                    }
                    return element;
                }
                throw new IllegalArgumentException("Could not get JsonCElement from JsonCObject from a non string primitive.");
            }

            @Nullable
            @Override
            public JsonCElement get(final String key) {
                final JsonCElement element = object.elements().get(key);
                if (element instanceof JsonCNull) {
                    return null;
                }
                return element;
            }

            @Override
            public Stream<Pair<JsonCElement, JsonCElement>> entries() {
                return object.elements().entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public JsonCElement createMap(final Stream<Pair<JsonCElement, JsonCElement>> map) {
        final JsonCObject result = new JsonCObject();
        map.forEach(p -> result.addElement(((JsonCPrimitive)p.getFirst()).json().getAsString(), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Stream<JsonCElement>> getStream(final JsonCElement input) {
        if (input instanceof JsonCArray array) {
            return DataResult.success(array.elements().stream().map(e -> e instanceof JsonCNull ? null : e));
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<JsonCElement>>> getList(final JsonCElement input) {
        if (input instanceof JsonCArray array) {
            return DataResult.success(c -> {
                for (final JsonCElement element : array.elements()) {
                    c.accept(element instanceof JsonCNull ? null : element);
                }
            });
        }
        return DataResult.error(() -> "Not a jsonc array: " + input);
    }

    @Override
    public JsonCElement createList(final Stream<JsonCElement> input) {
        final JsonCArray result = new JsonCArray();
        input.forEach(result::addElement);
        return result;
    }

    @Override
    public JsonCElement remove(final JsonCElement input, final String key) {
        if (input instanceof JsonCObject object) {
            final JsonCObject result = new JsonCObject();
            object.elements().entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.addElement(entry.getKey(), entry.getValue()));
            return result;
        }
        return input;
    }

    @Override
    public ListBuilder<JsonCElement> listBuilder() {
        return new ArrayBuilder();
    }

    @Override
    public RecordBuilder<JsonCElement> mapBuilder() {
        return new JsonCRecordBuilder();
    }

    private static final class ArrayBuilder implements ListBuilder<JsonCElement> {
        private DataResult<JsonCArray> builder = DataResult.success(new JsonCArray(), Lifecycle.stable());

        @Override
        public DynamicOps<JsonCElement> ops() {
            return INSTANCE;
        }

        @Override
        public ListBuilder<JsonCElement> add(final JsonCElement value) {
            builder = builder.map(b -> {
                b.addElement(value);
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> add(final DataResult<JsonCElement> value) {
            builder = builder.apply2stable((b, element) -> {
                b.addElement(element);
                return b;
            }, value);
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<JsonCElement> build(final JsonCElement prefix) {
            final DataResult<JsonCElement> result = builder.flatMap(b -> {
                if (prefix == ops().empty()) {
                    return DataResult.success(b, Lifecycle.stable());
                }

                if (!(prefix instanceof JsonCArray)) {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }

                final JsonCArray array = new JsonCArray();
                array.addAll((JsonCArray) prefix);
                array.addAll(b);
                return DataResult.success(array, Lifecycle.stable());
            });

            builder = DataResult.success(new JsonCArray(), Lifecycle.stable());
            return result;
        }
    }

    private class JsonCRecordBuilder extends RecordBuilder.AbstractStringBuilder<JsonCElement, JsonCObject> {
        protected JsonCRecordBuilder() {
            super(JsonCOps.this);
        }

        @Override
        protected JsonCObject initBuilder() {
            return new JsonCObject();
        }

        @Override
        protected JsonCObject append(final String key, final JsonCElement value, final JsonCObject builder) {
            builder.addElement(key, value);
            return builder;
        }

        @Override
        protected DataResult<JsonCElement> build(final JsonCObject builder, final JsonCElement prefix) {
            if (prefix == null || prefix == ops().empty()) {
                return DataResult.success(builder);
            }
            if (prefix instanceof JsonCObject object) {
                final JsonCObject result = new JsonCObject();
                result.setComments(object.comments());
                for (final Map.Entry<String, JsonCElement> entry : ((JsonCObject) prefix).elements().entrySet()) {
                    result.addElement(entry.getKey(), entry.getValue());
                }
                for (final Map.Entry<String, JsonCElement> entry : builder.elements().entrySet()) {
                    result.addElement(entry.getKey(), entry.getValue());
                }
                return DataResult.success(result);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }
}
