package fr.hardel.jev.event;

import fr.hardel.jev.command.JevCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CommandRegistrationEvent {

    private CommandRegistrationEvent() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(JevCommand.node()));
    }
}
