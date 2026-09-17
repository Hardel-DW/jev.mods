package fr.hardel.jev.typesafe;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.net.URI;
import java.net.http.HttpRequest;

public enum Endpoint implements StringRepresentable {
    TYPESAFE("typesafe", "https://api.typesafe.ai/v1/systemone", "jev-latest", "noul", "TYPESAFE_API_KEY"),
    VERCEL("vercel", "https://ai-gateway.vercel.sh/v4/ai/evaluation-model", "typesafe-ai/jev", "boolean", "AI_GATEWAY_API_KEY");

    public static final Codec<Endpoint> CODEC = StringRepresentable.fromEnum(Endpoint::values);

    private final String name;
    private final URI uri;
    private final String model;
    private final String booleanType;
    private final String keyVariable;

    Endpoint(String name, String uri, String model, String booleanType, String keyVariable) {
        this.name = name;
        this.uri = URI.create(uri);
        this.model = model;
        this.booleanType = booleanType;
        this.keyVariable = keyVariable;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String keyVariable() {
        return keyVariable;
    }

    String bodyModel() {
        return this == TYPESAFE ? model : null;
    }

    String booleanType() {
        return booleanType;
    }

    HttpRequest.Builder request(String apiKey) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).header("Content-Type", "application/json").header("Authorization", "Bearer " + apiKey);

        if (this == VERCEL) {
            builder.header("ai-gateway-auth-method", "api-key").header("ai-evaluation-model-specification-version", "4").header("ai-model-id", model);
        }

        return builder;
    }
}
