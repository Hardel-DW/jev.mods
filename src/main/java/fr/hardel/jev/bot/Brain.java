package fr.hardel.jev.bot;

import net.minecraft.resources.Identifier;

/** Whatever decides for the body. The body knows nothing of models; the contract stays in words and identifiers. */
public interface Brain {
    void tick(Bot bot);

    /** Multi-line text for the inspector. */
    String report();

    /** The player's instruction, in their words. */
    void directive(String text);

    /** Forces a behavior by hand; false when the identifier is unknown. */
    boolean behave(Identifier behavior, String argumentsJson);

    void replan();
}
