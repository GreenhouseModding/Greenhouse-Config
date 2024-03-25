package dev.greenhouseteam.greenhouseconfig.impl.jsonc.element;

import com.google.gson.JsonPrimitive;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;

import java.io.IOException;
import java.util.Objects;

public class JsonCPrimitive extends JsonCElement {
    private final JsonPrimitive json;

    public JsonCPrimitive(JsonPrimitive json) {
        this.json = json;
    }

    public JsonPrimitive json() {
        return this.json;
    }

    protected void writeJson(JsonCWriter jsonWriter) throws IOException {
        if (this.json.isJsonNull()) {
            jsonWriter.writeValue("null");
        } else if (this.json.isJsonPrimitive()) {
            JsonPrimitive primitive = this.json();
            if (primitive.isNumber()) {
                jsonWriter.writeValue(primitive.getAsNumber());
            } else if (primitive.isBoolean()) {
                jsonWriter.writeValue(primitive.getAsBoolean());
            } else {
                jsonWriter.writeValue(primitive.getAsString());
            }
        } else {
            throw new IllegalArgumentException("Couldn't write " + this.getClass());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.json(), this.comments());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JsonCPrimitive object)) {
            return false;
        }
        return object.json().equals(this.json()) && object.comments().equals(this.comments());
    }
}
