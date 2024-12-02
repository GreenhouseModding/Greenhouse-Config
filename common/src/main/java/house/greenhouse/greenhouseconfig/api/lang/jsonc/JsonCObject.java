package house.greenhouse.greenhouseconfig.api.lang.jsonc;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonCObject extends JsonCElement {
    private Map<String, JsonCElement> members = new LinkedHashMap<>();

    public static final JsonCObject EMPTY = new JsonCObject(Map.of());

    public JsonCObject(Map<String, JsonCElement> members) {
        super(null);
        this.members = members;
    }

    public JsonCObject(Map<String, JsonCElement> members, String... comments) {
        super(null, comments);
        this.members = members;
    }

    public JsonCObject(String... comments) {
        super(null, comments);
    }

    public JsonCObject(JsonObject object) {
        super(object);

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            members.put(entry.getKey(), entry.getValue().isJsonObject() ?
                    new JsonCObject(entry.getValue().getAsJsonObject()) :
                    new JsonCElement(entry.getValue()));
        }
    }

    public Map<String, JsonCElement> members() {
        return ImmutableMap.copyOf(members);
    }

    public void put(String name, JsonCElement json) {
        members.put(name, json);
    }

    public void putAll(Map<String, JsonCElement> map) {
        members.putAll(map);
    }
}
