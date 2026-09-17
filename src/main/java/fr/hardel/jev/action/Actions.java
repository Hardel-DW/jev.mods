package fr.hardel.jev.action;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import net.minecraft.core.Registry;

/** The catalogue of primitives, published to the registry at mod init. Pulses are 250 milliseconds, turns 30 degrees: one human keypress. */
public final class Actions {
    private static final int PULSE = 5;
    private static final int SPRINT_PULSE = 10;
    private static final float TURN = 30;
    private static final float TILT = 20;

    private Actions() {
    }

    public static void register() {
        register("forward", new MoveAction("Walk forward for a moment.", 1, 0, false, false, PULSE));
        register("backward", new MoveAction("Walk backward for a moment, without turning.", -1, 0, false, false, PULSE));
        register("strafe_left", new MoveAction("Sidestep left for a moment, without turning.", 0, 1, false, false, PULSE));
        register("strafe_right", new MoveAction("Sidestep right for a moment, without turning.", 0, -1, false, false, PULSE));
        register("sprint_forward", new MoveAction("Sprint forward, faster and hungrier than walking.", 1, 0, false, true, SPRINT_PULSE));
        register("jump_forward", new MoveAction("Jump while moving forward: climbs a one block step or crosses a small gap.", 1, 0, true, false, PULSE));
        register("jump", new MoveAction("Jump in place.", 0, 0, true, false, PULSE));
        register("turn_left", new TurnAction("Turn the head 30 degrees to the left.", -TURN, 0));
        register("turn_right", new TurnAction("Turn the head 30 degrees to the right.", TURN, 0));
        register("look_up", new TurnAction("Tilt the gaze 20 degrees upward.", 0, -TILT));
        register("look_down", new TurnAction("Tilt the gaze 20 degrees downward.", 0, TILT));
        register("look_ahead", new LevelLookAction());
        register("break", new BreakAction());
        register("use", new UseAction());
        register("wait", new WaitAction());
    }

    private static void register(String path, Action action) {
        Registry.register(JevBuiltInRegistries.ACTION, Jev.id(path), action);
    }
}
