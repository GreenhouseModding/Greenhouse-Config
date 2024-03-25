package dev.greenhouseteam.greenhouseconfig.impl.jsonc.element;

import com.google.common.collect.ImmutableMap;
import com.google.gson.internal.LinkedTreeMap;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class JsonCObject extends JsonCElement {
    private final Map<String, JsonCElement> elements = new LinkedHashMap<>();

    public Map<String, JsonCElement> elements() {
        return ImmutableMap.copyOf(this.elements);
    }

    public void addElement(String key, JsonCElement element) {
        this.elements.put(key, element);
    }

    public void addElementToBeginning(String key, JsonCElement element) {
        LinkedTreeMap<String, JsonCElement> elements = new LinkedTreeMap<>();
        elements.putAll(this.elements);

        this.elements.clear();
        this.elements.put(key, element);
        this.elements.putAll(elements);
    }

    @Override
    protected void writeJson(JsonCWriter jsonWriter) throws IOException {
        jsonWriter.comments(this.comments());
        jsonWriter.beginObject();
        for (Map.Entry<String, JsonCElement> e : elements().entrySet()) {
            jsonWriter.comments(e.getValue().comments());
            jsonWriter.key(e.getKey());
            e.getValue().writeJson(jsonWriter);
        }
        jsonWriter.endObject();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements(), this.comments());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JsonCObject object)) {
            return false;
        }
        return object.elements().equals(this.elements()) && object.comments().equals(this.comments());
    }
}
