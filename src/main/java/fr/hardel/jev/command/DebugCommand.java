package fr.hardel.jev.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.hardel.jev.debug.DebugWindow;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.awt.GraphicsEnvironment;

/** {@code /jev debug}: opens or closes the bot inspector window on the machine running the server. */
final class DebugCommand {
    private static final SimpleCommandExceptionType HEADLESS = new SimpleCommandExceptionType(Component.literal("This server has no display for a debug window"));

    private DebugCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("debug").executes(context -> toggle(context.getSource()));
    }

    private static int toggle(CommandSourceStack source) throws CommandSyntaxException {
        if (GraphicsEnvironment.isHeadless()) {
            throw HEADLESS.create();
        }

        boolean open = DebugWindow.toggle(source.getServer());
        source.sendSuccess(() -> Component.literal(open ? "Debug window opened" : "Debug window closed"), false);
        return 1;
    }
}
