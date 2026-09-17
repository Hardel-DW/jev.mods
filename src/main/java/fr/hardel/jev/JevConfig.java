package fr.hardel.jev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hardel.jev.typesafe.Endpoint;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** {@code config/jev.json}: which models think for the bots. Secrets never live here, they sit in {@code config/jev_key.txt} or the environment. */
public record JevConfig(Endpoint typesafeEndpoint, String strategistModel, String strategistEffort) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static final Codec<JevConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Endpoint.CODEC.optionalFieldOf("typesafe_endpoint", Endpoint.TYPESAFE).forGetter(JevConfig::typesafeEndpoint),
        Codec.STRING.optionalFieldOf("strategist_model", "gpt-5.6-luna").forGetter(JevConfig::strategistModel),
        Codec.STRING.optionalFieldOf("strategist_effort", "medium").forGetter(JevConfig::strategistEffort)
    ).apply(builder, JevConfig::new));

    private static JevConfig instance;
    private static Path directory;

    public static void register(Path configDir) {
        directory = configDir;
        Path file = configDir.resolve(Jev.MOD_ID + ".json");
        instance = Files.notExists(file) ? write(file, defaults()) : parse(file, read(file));
    }

    public static JevConfig get() {
        return instance;
    }

    public static Path directory() {
        return directory;
    }

    private static JevConfig defaults() {
        return CODEC.parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
    }

    private static JevConfig parse(Path file, JsonElement json) {
        return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(error -> new IllegalArgumentException("Config " + file + " is invalid: " + error));
    }

    private static JsonElement read(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file));
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read " + file, exception);
        }
    }

    private static JevConfig write(Path file, JevConfig config) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow()) + System.lineSeparator());
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to write " + file, exception);
        }

        return config;
    }
}
