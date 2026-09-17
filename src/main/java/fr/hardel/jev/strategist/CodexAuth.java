package fr.hardel.jev.strategist;

import com.google.gson.annotations.SerializedName;
import fr.hardel.jev.Jev;
import fr.hardel.jev.typesafe.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/** The ChatGPT login of the Codex CLI on this machine, refreshed when its token is about to expire. Copied from Studio. */
public final class CodexAuth {
    private static final String TOKEN_ENDPOINT = "https://auth.openai.com/oauth/token";
    private static final String CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann";
    private static final String CHATGPT_AUTH_MODE = "chatgpt";
    private static final long EXPIRY_MARGIN_SECONDS = 60;
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    public record Credentials(String accessToken, String accountId) {
    }

    private record AuthFile(String authMode, Tokens tokens) {
    }

    private record Tokens(String accessToken, String idToken, String refreshToken) {
    }

    private record Claims(long exp, String chatgptAccountId, @SerializedName("https://api.openai.com/auth") AuthNamespace auth) {
    }

    private record AuthNamespace(String chatgptAccountId, List<Organization> organizations) {
    }

    private record Organization(String id) {
    }

    private record RefreshRequest(String clientId, String grantType, String refreshToken, String scope) {
    }

    private CodexAuth() {
    }

    public static Optional<Credentials> resolve() {
        Path path = authFile();
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        AuthFile auth;
        try {
            auth = Json.parse(Files.readString(path), AuthFile.class);
        } catch (IOException | RuntimeException exception) {
            Jev.LOGGER.warn("Failed to read Codex auth file at {}", path);
            return Optional.empty();
        }

        if (auth == null || !CHATGPT_AUTH_MODE.equals(auth.authMode) || auth.tokens == null) {
            return Optional.empty();
        }

        String accessToken = auth.tokens.accessToken;
        String idToken = auth.tokens.idToken;
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }

        if (isExpired(accessToken) && auth.tokens.refreshToken != null && !auth.tokens.refreshToken.isBlank()) {
            Tokens refreshed = refresh(auth.tokens.refreshToken);
            if (refreshed == null || refreshed.accessToken == null || refreshed.accessToken.isBlank()) {
                return Optional.empty();
            }

            accessToken = refreshed.accessToken;
            if (refreshed.idToken != null && !refreshed.idToken.isBlank()) {
                idToken = refreshed.idToken;
            }
        }

        return Optional.of(new Credentials(accessToken, accountId(idToken, accessToken)));
    }

    private static boolean isExpired(String accessToken) {
        Claims claims = decodeJwt(accessToken);
        if (claims == null || claims.exp <= 0) {
            return true;
        }

        return System.currentTimeMillis() / 1000L >= claims.exp - EXPIRY_MARGIN_SECONDS;
    }

    private static String accountId(String idToken, String accessToken) {
        String fromId = claimAccountId(decodeJwt(idToken));
        if (fromId != null) {
            return fromId;
        }

        String fromAccess = claimAccountId(decodeJwt(accessToken));
        return fromAccess == null ? "" : fromAccess;
    }

    private static String claimAccountId(Claims claims) {
        if (claims == null) {
            return null;
        }

        if (claims.chatgptAccountId != null && !claims.chatgptAccountId.isBlank()) {
            return claims.chatgptAccountId;
        }

        if (claims.auth == null) {
            return null;
        }

        if (claims.auth.chatgptAccountId != null && !claims.auth.chatgptAccountId.isBlank()) {
            return claims.auth.chatgptAccountId;
        }

        if (claims.auth.organizations != null && !claims.auth.organizations.isEmpty()) {
            return claims.auth.organizations.getFirst().id;
        }

        return null;
    }

    private static Claims decodeJwt(String jwt) {
        if (jwt == null) {
            return null;
        }

        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return Json.parse(new String(decoded, StandardCharsets.UTF_8), Claims.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static Tokens refresh(String refreshToken) {
        RefreshRequest request = new RefreshRequest(CLIENT_ID, "refresh_token", refreshToken, "openid profile email");
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(Json.compact(request), StandardCharsets.UTF_8))
            .build();

        try {
            HttpResponse<String> response = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                Jev.LOGGER.warn("Codex token refresh failed: http {}", response.statusCode());
                return null;
            }

            return Json.parse(response.body(), Tokens.class);
        } catch (IOException | RuntimeException exception) {
            Jev.LOGGER.warn("Codex token refresh failed", exception);
            return null;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static Path authFile() {
        String codexHome = System.getenv("CODEX_HOME");
        if (codexHome != null && !codexHome.isBlank()) {
            return Path.of(codexHome, "auth.json");
        }

        return Path.of(System.getProperty("user.home"), ".codex", "auth.json");
    }
}
