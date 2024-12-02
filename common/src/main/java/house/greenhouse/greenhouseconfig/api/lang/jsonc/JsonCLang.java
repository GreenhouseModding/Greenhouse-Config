package house.greenhouse.greenhouseconfig.api.lang.jsonc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;

import com.mojang.serialization.DynamicOps;

public final class JsonCLang implements ConfigLang<JsonCElement> {
    public static final JsonCLang INSTANCE = new JsonCLang();

    private JsonCLang() {
    }

    @Override
    public DynamicOps<JsonCElement> getOps() {
        return JsonCOps.INSTANCE;
    }

    @Override
    public String getFileExtension() {
        return "jsonc";
    }

    @Override
    public void write(Writer writer, JsonCElement configObj) throws IOException {
        JsonCWriter jsonCWriter = new JsonCWriter(writer);
        jsonCWriter.write(configObj);
        jsonCWriter.flush();
    }

    @Override
    public JsonCElement read(Reader reader) throws IOException {
        JsonElement json = JsonParser.parseReader(reader);
        if (json.isJsonObject()) {
            return new JsonCObject(json.getAsJsonObject());
        } else {
            return new JsonCElement(json);
        }
    }
}
