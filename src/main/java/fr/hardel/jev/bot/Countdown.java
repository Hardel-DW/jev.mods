package fr.hardel.jev.bot;

/** Holds whatever keys are down for a fixed number of ticks. */
public final class Countdown implements Activity {
    private int remaining;

    public Countdown(int ticks) {
        this.remaining = ticks;
    }

    @Override
    public boolean tick(Bot bot) {
        return --remaining <= 0;
    }
}
