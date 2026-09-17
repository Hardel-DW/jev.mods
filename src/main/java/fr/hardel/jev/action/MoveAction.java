package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Controls;
import fr.hardel.jev.bot.Countdown;

/** A key combination held for a pulse: the body moves as far as vanilla physics carries it in that time. */
public final class MoveAction implements Action {
    private final String description;
    private final float forward;
    private final float strafe;
    private final boolean jump;
    private final boolean sprint;
    private final int ticks;

    public MoveAction(String description, float forward, float strafe, boolean jump, boolean sprint, int ticks) {
        this.description = description;
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sprint = sprint;
        this.ticks = ticks;
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
        Controls controls = bot.controls();
        controls.move(forward, strafe);
        controls.jump(jump);
        controls.sprint(sprint);
        return new Countdown(ticks);
    }
}
