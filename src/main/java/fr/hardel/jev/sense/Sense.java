package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;

@FunctionalInterface
public interface Sense {
    Perception perceive(Bot bot);
}
