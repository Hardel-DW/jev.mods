package fr.hardel.jev.bot;

@FunctionalInterface
public interface Activity {
    boolean tick(Bot bot);
}
