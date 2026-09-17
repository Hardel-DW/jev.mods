package fr.hardel.jev.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Bots;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /jev remove <bot>}: the bot leaves like a disconnecting player, its data saved. */
final class RemoveCommand {

    private RemoveCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("remove").then(BotArgument.node().executes(RemoveCommand::remove));
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Bot bot = BotArgument.get(context);
        Bots.remove(bot);
        context.getSource().sendSuccess(() -> Component.literal("Bot " + bot.name() + " left"), true);
        return 1;
    }
}
