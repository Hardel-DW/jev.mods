package fr.hardel.jev.strategist;

import fr.hardel.jev.typesafe.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The Codex responses endpoint on a ChatGPT subscription, asked for exactly one function call. Wire shapes copied from Studio. */
public final class CodexClient {
    private static final URI ENDPOINT = URI.create("https://chatgpt.com/backend-api/codex/responses");
    private static final String ORIGINATOR = "codex_cli_rs";
    private static final String RESPONSES_BETA = "responses=experimental";
    private static final String DATA_PREFIX = "data:";
    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private static final int ERROR_BODY_MAX = 500;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    private final String model;
    private final String effort;

    public record Function(String name, String description, Schema parameters) {
    }

    private record Body(String model, String instructions, List<MessageItem> input, Reasoning reasoning, List<ToolWire> tools, String toolChoice, boolean stream, boolean store) {
    }

    private record Reasoning(String effort, String summary) {
    }

    private record MessageItem(String type, String role, List<Part> content) {
    }

    private record Part(String type, String text) {
    }

    private record ToolWire(String type, String name, String description, Schema parameters) {
    }

    private record Event(String type, Response response, Error error) {
    }

    private record Response(List<Item> output, Error error) {
    }

    private record Item(String type, String name, String arguments, List<Part> summary) {
    }

    private record Error(String message) {
    }

    public CodexClient(String model, String effort) {
        this.model = model;
        this.effort = effort;
    }

    public CompletableFuture<Plan> plan(String instructions, Brief brief, List<Function> functions) {
        Body body = new Body(
            model,
            instructions,
            List.of(new MessageItem("message", "user", List.of(new Part("input_text", Json.compact(brief))))),
            new Reasoning(effort, "auto"),
            functions.stream().map(function -> new ToolWire("function", function.name(), function.description(), function.parameters())).toList(),
            "required",
            true,
            false);
        return CompletableFuture.supplyAsync(() -> send(Json.compact(body)), executor);
    }

    private Plan send(String body) {
        CodexAuth.Credentials credentials = CodexAuth.resolve().orElseThrow(() -> new StrategistException("no ChatGPT login found for the Codex CLI"));
        HttpRequest.Builder builder = HttpRequest.newBuilder(ENDPOINT)
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .header("Authorization", "Bearer " + credentials.accessToken())
            .header("OpenAI-Beta", RESPONSES_BETA)
            .header("originator", ORIGINATOR)
            .header("session_id", UUID.randomUUID().toString())
            .timeout(TIMEOUT)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (!credentials.accountId().isBlank()) {
            builder.header("ChatGPT-Account-Id", credentials.accountId());
        }

        HttpResponse<Stream<String>> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofLines());
        } catch (IOException exception) {
            throw new StrategistException("network error: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new StrategistException("interrupted");
        }

        if (response.statusCode() / 100 != 2) {
            String error = response.body().collect(Collectors.joining("\n"));
            throw new StrategistException("http " + response.statusCode() + ": " + error.substring(0, Math.min(error.length(), ERROR_BODY_MAX)));
        }

        try (Stream<String> lines = response.body()) {
            return lines.filter(line -> line.startsWith(DATA_PREFIX))
                .map(line -> line.substring(DATA_PREFIX.length()).strip())
                .filter(payload -> payload.startsWith("{"))
                .map(payload -> Json.parse(payload, Event.class))
                .map(CodexClient::completed)
                .filter(plan -> plan != null)
                .findFirst()
                .orElseThrow(() -> new StrategistException("the strategist answered without choosing a behavior"));
        }
    }

    private static Plan completed(Event event) {
        switch (event.type == null ? "" : event.type) {
            case "response.failed", "error" -> throw new StrategistException(failure(event));
            case "response.completed" -> {
                return plan(event.response);
            }
            default -> {
                return null;
            }
        }
    }

    private static Plan plan(Response response) {
        if (response == null || response.output == null) {
            throw new StrategistException("empty completion");
        }

        if (response.error != null) {
            throw new StrategistException(response.error.message);
        }

        List<String> reasoning = new ArrayList<>();
        Item call = null;
        for (Item item : response.output) {
            if ("function_call".equals(item.type) && call == null) {
                call = item;
            } else if ("reasoning".equals(item.type) && item.summary != null) {
                item.summary.stream().filter(part -> "summary_text".equals(part.type) && part.text != null).forEach(part -> reasoning.add(part.text));
            }
        }

        if (call == null) {
            throw new StrategistException("the strategist answered without choosing a behavior");
        }

        return new Plan(call.name, call.arguments == null || call.arguments.isBlank() ? "{}" : call.arguments, String.join("\n", reasoning));
    }

    private static String failure(Event event) {
        if (event.error != null && event.error.message != null) {
            return event.error.message;
        }

        return event.response != null && event.response.error != null && event.response.error.message != null ? event.response.error.message : "codex stream failed";
    }
}
