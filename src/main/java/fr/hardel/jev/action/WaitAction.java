package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Countdown;

public final class WaitAction implements Action {
    private static final int TICKS = 5;

    @Override
    public String description() {
        return "Stand still for a moment. Only when no other action is useful.";
    }

    @Override
    public boolean available(Bot bot) {
        return true;
    }

    @Override
    public Activity start(Bot bot) {
        return new Countdown(TICKS);
    }
}
