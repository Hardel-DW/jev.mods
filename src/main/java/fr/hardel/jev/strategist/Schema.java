package fr.hardel.jev.strategist;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A JSON schema for a function's parameters, the closed shapes a strategist may fill: strings, integers, booleans, enumerations. */
public final class Schema {
    private final String type = "object";
    private final Map<String, Property> properties = new LinkedHashMap<>();
    private final List<String> required = new ArrayList<>();
    @SerializedName("additionalProperties")
    private final boolean additionalProperties = false;

    private record Property(String type, String description, @SerializedName("enum") List<String> values) {
    }

    private Schema() {
    }

    public static Schema object() {
        return new Schema();
    }

    public Schema string(String name, String description, boolean requiredField) {
        return add(name, new Property("string", description, null), requiredField);
    }

    public Schema integer(String name, String description, boolean requiredField) {
        return add(name, new Property("integer", description, null), requiredField);
    }

    public Schema bool(String name, String description, boolean requiredField) {
        return add(name, new Property("boolean", description, null), requiredField);
    }

    public Schema enumeration(String name, String description, boolean requiredField, String... values) {
        return add(name, new Property("string", description, List.of(values)), requiredField);
    }

    private Schema add(String name, Property property, boolean requiredField) {
        properties.put(name, property);
        if (requiredField) {
            required.add(name);
        }

        return this;
    }
}
