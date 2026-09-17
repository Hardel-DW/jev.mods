package fr.hardel.jev.behavior;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.strategist.Schema;

public interface Behavior {
    String description();

    Schema parameters();

    Session begin(Bot bot, Arguments arguments);
}
