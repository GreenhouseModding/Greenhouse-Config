package dev.greenhouseteam.greenhouseconfig.api.lang.jsonc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.greenhouseteam.greenhouseconfig.api.lang.ConfigLang;

import com.mojang.serialization.DynamicOps;

public final class JsonCLang implements ConfigLang<CommentedJson> {
    public static final JsonCLang INSTANCE = new JsonCLang();

    private JsonCLang() {
    }

    @Override
    public DynamicOps<CommentedJson> getOps() {
        return JsonCOps.INSTANCE;
    }

    @Override
    public void write(Writer writer, CommentedJson configObj) throws IOException {
        JsonCWriter jsonCWriter = new JsonCWriter(writer);
        jsonCWriter.writeJson(configObj);
        jsonCWriter.flush();
    }

    @Override
    public CommentedJson read(Reader reader) throws IOException {
        JsonElement json = JsonParser.parseReader(reader);
        if (json.isJsonObject()) {
            return new CommentedJson.Object(json.getAsJsonObject());
        } else {
            return new CommentedJson(json);
        }
    }
}
