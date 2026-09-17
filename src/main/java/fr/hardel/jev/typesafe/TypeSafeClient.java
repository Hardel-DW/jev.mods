package fr.hardel.jev.typesafe;

import fr.hardel.jev.sense.Perception;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** The System One call: state and questions in, a decision out, never on a game thread. */
public final class TypeSafeClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int ERROR_BODY_MAX = 300;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Endpoint endpoint;
    private final String apiKey;

    private record Request(String model, Map<String, Perception> state, Map<String, Question> questions) {
    }

    public TypeSafeClient(Endpoint endpoint, String apiKey) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    public CompletableFuture<Decision> decide(Map<String, Perception> state, Map<String, Question> questions) {
        String body = Json.compact(new Request(endpoint.bodyModel(), state, dialect(questions)));
        return CompletableFuture.supplyAsync(() -> send(body), executor);
    }

    private Map<String, Question> dialect(Map<String, Question> questions) {
        Map<String, Question> wire = new LinkedHashMap<>();
        questions.forEach((id, question) -> wire.put(id, question instanceof Question.Noul noul ? new Question.Noul(endpoint.booleanType(), noul.instructions(), noul.criteria()) : question));
        return wire;
    }

    private Decision send(String body) {
        HttpRequest request = endpoint.request(apiKey)
            .timeout(TIMEOUT)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
            
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new TypeSafeException(0, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TypeSafeException(0, "interrupted");
        }

        if (response.statusCode() / 100 != 2) {
            String detail = response.body();
            throw new TypeSafeException(response.statusCode(), "http " + response.statusCode() + ": " + detail.substring(0, Math.min(detail.length(), ERROR_BODY_MAX)));
        }

        return Json.parse(response.body(), Decision.class);
    }
}
