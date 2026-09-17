package fr.hardel.jev.sense;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import net.minecraft.core.Registry;

/** The catalogue of senses; registration order is the order of the fragments in the state. */
public final class Senses {

    private Senses() {
    }

    public static void register() {
        register("body", new BodySense());
        register("terrain", new TerrainSense());
        register("vision", new VisionSense());
        register("entities", new EntitySense());
        register("hearing", new HearingSense());
    }

    private static void register(String path, Sense sense) {
        Registry.register(JevBuiltInRegistries.SENSE, Jev.id(path), sense);
    }
}
