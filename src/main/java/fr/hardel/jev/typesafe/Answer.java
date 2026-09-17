package fr.hardel.jev.typesafe;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

public sealed interface Answer permits Answer.Choice, Answer.Noul, Answer.Score {

    record Choice(String choice, Map<String, Double> probabilities, double confidence) implements Answer {
    }

    record Noul(double noul) implements Answer {
    }

    record Score(double score, Map<String, String> legend, Map<String, Double> probabilities, double confidence) implements Answer {
    }

    final class Adapter implements JsonDeserializer<Answer> {
        @Override
        public Answer deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
            String kind = object.get("type").getAsString();
            return switch (kind) {
                case "choice" -> context.deserialize(object, Choice.class);
                case "noul" -> context.deserialize(object, Noul.class);
                case "boolean" -> new Noul(object.get("probability").getAsDouble());
                case "score" -> context.deserialize(object, Score.class);
                default -> throw new JsonParseException("Unknown answer type " + kind);
            };
        }
    }
}
