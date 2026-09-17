package fr.hardel.jev.strategist;

/** What the strategist decided: a behavior by name, its parameters as the JSON it filled, and the reasoning summary if any. */
public record Plan(String behavior, String arguments, String reasoning) {
}
