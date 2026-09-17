package fr.hardel.jev.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.hardel.jev.bot.Bot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /jev goal <bot> <text>}: the player's instruction, in their own words; the strategist replans around it. */
final class GoalCommand {

    private GoalCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("goal")
            .then(BotArgument.node()
                .then(Commands.argument("text", StringArgumentType.greedyString()).executes(GoalCommand::set)));
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Bot bot = BotArgument.get(context);
        String text = StringArgumentType.getString(context, "text");
        bot.brain().directive(text);
        context.getSource().sendSuccess(() -> Component.literal(bot.name() + " will consider: " + text), true);
        return 1;
    }
}
