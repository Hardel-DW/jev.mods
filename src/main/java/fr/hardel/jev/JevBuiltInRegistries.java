package fr.hardel.jev;

import com.mojang.serialization.Lifecycle;
import fr.hardel.jev.action.Action;
import fr.hardel.jev.behavior.Behavior;
import fr.hardel.jev.sense.Sense;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

/** Jev's registries, mirror of vanilla's {@code BuiltInRegistries}. Server only, never synced. */
public final class JevBuiltInRegistries {
    public static final Registry<Action> ACTION = new MappedRegistry<>(JevRegistries.ACTION, Lifecycle.stable());
    public static final Registry<Sense> SENSE = new MappedRegistry<>(JevRegistries.SENSE, Lifecycle.stable());
    public static final Registry<Behavior> BEHAVIOR = new MappedRegistry<>(JevRegistries.BEHAVIOR, Lifecycle.stable());

    private JevBuiltInRegistries() {
    }
}
