package fr.hardel.jev;

import fr.hardel.jev.action.Action;
import fr.hardel.jev.behavior.Behavior;
import fr.hardel.jev.sense.Sense;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** The keys of the custom registries of Jev, the mirror of vanilla's {@code Registries}; the instances live in {@link JevBuiltInRegistries}. */
public final class JevRegistries {
    public static final ResourceKey<Registry<Action>> ACTION = ResourceKey.createRegistryKey(Jev.id("action"));
    public static final ResourceKey<Registry<Sense>> SENSE = ResourceKey.createRegistryKey(Jev.id("sense"));
    public static final ResourceKey<Registry<Behavior>> BEHAVIOR = ResourceKey.createRegistryKey(Jev.id("behavior"));

    private JevRegistries() {
    }
}
