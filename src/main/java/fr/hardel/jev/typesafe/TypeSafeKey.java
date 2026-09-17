package fr.hardel.jev.typesafe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** The API key of the chosen endpoint: its environment variable first, else a one-line file beside the config. Absent means the reflexes stay off. */
public final class TypeSafeKey {
    private static final String FILE = "jev_key.txt";

    private TypeSafeKey() {
    }

    public static Optional<String> find(Endpoint endpoint, Path configDir) {
        String fromEnvironment = System.getenv(endpoint.keyVariable());
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return Optional.of(fromEnvironment.strip());
        }

        Path file = configDir.resolve(FILE);
        if (Files.notExists(file)) {
            return Optional.empty();
        }

        try {
            String key = Files.readString(file).strip();
            return key.isEmpty() ? Optional.empty() : Optional.of(key);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read " + file, exception);
        }
    }
}
