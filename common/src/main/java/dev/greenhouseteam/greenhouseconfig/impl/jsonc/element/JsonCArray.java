package dev.greenhouseteam.greenhouseconfig.impl.jsonc.element;

import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonCArray extends JsonCElement {
    private final List<JsonCElement> elements = new ArrayList<>();

    public List<JsonCElement> elements() {
        return List.copyOf(this.elements);
    }

    public void addElement(JsonCElement element) {
        this.elements.add(element);
    }

    public void addAll(JsonCArray element) {
        this.elements.addAll(element.elements);
    }

    public void clear() {
        this.elements.clear();
    }

    @Override
    protected void writeJson(JsonCWriter jsonWriter) throws IOException {
        jsonWriter.comments(this.comments());
        jsonWriter.beginArray();
        for (JsonCElement e : elements()) {
            e.writeJson(jsonWriter);
        }
        jsonWriter.endArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements(), this.comments());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JsonCArray object)) {
            return false;
        }
        return object.elements().equals(this.elements()) && object.comments().equals(this.comments());
    }
}
