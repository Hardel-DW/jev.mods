package fr.hardel.jev.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Bots;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /jev list}: every bot, where it stands and what it is doing. */
final class ListCommand {

    private ListCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("list").executes(context -> list(context.getSource()));
    }

    private static int list(CommandSourceStack source) {
        for (Bot bot : Bots.all()) {
            source.sendSuccess(() -> Component.literal(bot.name() + " at " + bot.player().blockPosition().toShortString() + ", " + bot.activityName()), false);
        }

        return Bots.all().size();
    }
}
