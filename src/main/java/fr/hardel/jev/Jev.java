package fr.hardel.jev;

import fr.hardel.jev.action.Actions;
import fr.hardel.jev.behavior.Behaviors;
import fr.hardel.jev.event.CommandRegistrationEvent;
import fr.hardel.jev.event.ServerStoppingEvent;
import fr.hardel.jev.sense.Senses;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Jev implements ModInitializer {
    public static final String MOD_ID = "jev";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        JevConfig.register(FabricLoader.getInstance().getConfigDir());
        Actions.register();
        Senses.register();
        Behaviors.register();
        CommandRegistrationEvent.register();
        ServerStoppingEvent.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
