package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;

/** One primitive the body can be asked to do: described for the model, offered only when possible, executed as a bounded activity. */
public interface Action {
    /** What the model reads to choose it. */
    String description();

    /** Whether the bot can do it right now; an unavailable action is never offered. */
    boolean available(Bot bot);

    Activity start(Bot bot);
}
