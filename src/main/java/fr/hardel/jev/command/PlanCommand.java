package fr.hardel.jev.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.hardel.jev.bot.Bot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /jev plan <bot>}: wakes the strategist now instead of waiting for an event. */
final class PlanCommand {

    private PlanCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("plan").then(BotArgument.node().executes(PlanCommand::replan));
    }

    private static int replan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Bot bot = BotArgument.get(context);
        bot.brain().replan();
        context.getSource().sendSuccess(() -> Component.literal(bot.name() + " is thinking"), false);
        return 1;
    }
}
