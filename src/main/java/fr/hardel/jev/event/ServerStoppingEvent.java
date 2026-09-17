package fr.hardel.jev.event;

import fr.hardel.jev.bot.Bots;
import fr.hardel.jev.debug.DebugWindow;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerStoppingEvent {

    private ServerStoppingEvent() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> {
            DebugWindow.close();
            Bots.forget();
        });
    }
}
