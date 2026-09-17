package fr.hardel.jev.typesafe;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** The one JSON dialect of the mod: records in, snake_case out, the shape the TypeSafe API and the debug window both read. */
public final class Json {
    private static final Gson COMPACT = builder().create();
    private static final Gson PRETTY = builder().setPrettyPrinting().create();

    private Json() {
    }

    private static GsonBuilder builder() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(Answer.class, new Answer.Adapter());
    }

    public static String compact(Object value) {
        return COMPACT.toJson(value);
    }

    public static String pretty(Object value) {
        return PRETTY.toJson(value);
    }

    public static <T> T parse(String json, Class<T> type) {
        return COMPACT.fromJson(json, type);
    }
}
