package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;

/** Turns the head by a fixed angle at the body's turning speed; over once the target is reached. */
public final class TurnAction implements Action {
    private final String description;
    private final float yaw;
    private final float pitch;

    public TurnAction(String description, float yaw, float pitch) {
        this.description = description;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean available(Bot bot) {
        return bot.player().isAlive();
    }

    @Override
    public Activity start(Bot bot) {
        bot.controls().turn(bot.player(), yaw, pitch);
        return current -> current.controls().looking(current.player());
    }
}
