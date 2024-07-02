package dev.greenhouseteam.greenhouseconfig.api.lang.jsonc;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

public class CommentedJson implements CommentedValue {
    private final JsonElement json;
    private final String[] comments;

    public static final CommentedJson EMPTY = new CommentedJson(JsonNull.INSTANCE);

    public CommentedJson(JsonElement json) {
        this.json = json;
        this.comments = new String[]{};
    }

    public CommentedJson(JsonElement json, String... comments) {
        this.json = json;
        this.comments = comments;
    }

    public JsonElement json() {
        return json;
    }

    public String[] comments() {
        return comments;
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new CommentedJson(json, comments);
    }

    public static class Object extends CommentedJson {
        private Map<String, CommentedJson> map = new LinkedHashMap<>();

        public static final CommentedJson.Object EMPTY = new Object(Map.of());

        public Object(Map<String, CommentedJson> map) {
            super(null);
            this.map = map;
        }

        public Object(Map<String, CommentedJson> map, String... comments) {
            super(null, comments);
            this.map = map;
        }

        public Object(String... comments) {
            super(null, comments);
        }

        public Object(JsonObject object) {
            super(object);

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    map.put(entry.getKey(), new CommentedJson.Object(entry.getValue().getAsJsonObject()));
                } else {
                    map.put(entry.getKey(), new CommentedJson(entry.getValue()));
                }
            }
        }

        public Map<String, CommentedJson> getMap() {
            return ImmutableMap.copyOf(map);
        }

        public void put(String name, CommentedJson json) {
            map.put(name, json);
        }

        public void putAll(Map<String, CommentedJson> map) {
            this.map.putAll(map);
        }
    }
}
