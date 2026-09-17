package fr.hardel.jev.behavior;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record Arguments(JsonObject json) {

    public static Arguments parse(String json) {
        return new Arguments(json.isBlank() ? new JsonObject() : JsonParser.parseString(json).getAsJsonObject());
    }

    public static Arguments none() {
        return new Arguments(new JsonObject());
    }

    public String string(String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    public int integer(String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }
}
