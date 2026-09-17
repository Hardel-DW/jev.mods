package fr.hardel.jev.typesafe;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public record Decision(String model, Map<String, Answer> answers, Usage usage) {

    public record Usage(@SerializedName(value = "input_tokens", alternate = "inputTokens") int inputTokens, @SerializedName(value = "output_tokens", alternate = "outputTokens") int outputTokens) {}

    public Answer.Choice choice(String question) {
        return (Answer.Choice) answers.get(question);
    }

    public Answer.Noul noul(String question) {
        return (Answer.Noul) answers.get(question);
    }

    public Answer.Score score(String question) {
        return (Answer.Score) answers.get(question);
    }
}
