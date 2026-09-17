package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;

/** Brings the gaze back to the horizon without changing heading. */
public final class LevelLookAction implements Action {

    @Override
    public String description() {
        return "Look straight ahead at the horizon, keeping the current heading.";
    }

    @Override
    public boolean available(Bot bot) {
        return bot.player().getXRot() != 0;
    }

    @Override
    public Activity start(Bot bot) {
        bot.controls().look(bot.player().getYRot(), 0);
        return current -> current.controls().looking(current.player());
    }
}
