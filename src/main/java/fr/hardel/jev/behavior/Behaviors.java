package fr.hardel.jev.behavior;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import net.minecraft.core.Registry;

/** The catalogue of behaviors, published to the registry at mod init; the strategist sees exactly this list as its functions. */
public final class Behaviors {

    private Behaviors() {
    }

    public static void register() {
        register("explore", new ExploreBehavior());
        register("gather_wood", new GatherWoodBehavior());
    }

    private static void register(String path, Behavior behavior) {
        Registry.register(JevBuiltInRegistries.BEHAVIOR, Jev.id(path), behavior);
    }
}
