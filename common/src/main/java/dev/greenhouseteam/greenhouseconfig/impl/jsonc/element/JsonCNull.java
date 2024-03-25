package dev.greenhouseteam.greenhouseconfig.impl.jsonc.element;

import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;

public final class JsonCNull extends JsonCElement {
    public static final JsonCNull INSTANCE = new JsonCNull();

    private JsonCNull() {

    }

    @Override
    protected void writeJson(JsonCWriter jsonWriter) {

    }

    @Override
    public int hashCode() {
        return JsonCNull.class.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonCNull;
    }
}
