package fr.hardel.jev.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.bot.Bot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** {@code /jev behave <bot> <behavior> [json]}: starts a behavior by hand, bypassing the strategist. */
final class BehaveCommand {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(behavior -> Component.literal("No behavior named " + behavior));

    private BehaveCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("behave")
            .then(BotArgument.node()
                .then(Commands.argument("behavior", StringArgumentType.word())
                    .suggests((_, builder) -> SharedSuggestionProvider.suggest(JevBuiltInRegistries.BEHAVIOR.keySet().stream().map(Identifier::getPath), builder))
                    .executes(context -> begin(context, "{}"))
                    .then(Commands.argument("json", StringArgumentType.greedyString())
                        .executes(context -> begin(context, StringArgumentType.getString(context, "json"))))));
    }

    private static int begin(CommandContext<CommandSourceStack> context, String json) throws CommandSyntaxException {
        Bot bot = BotArgument.get(context);
        String path = StringArgumentType.getString(context, "behavior");
        if (!bot.brain().behave(Jev.id(path), json)) {
            throw UNKNOWN.create(path);
        }

        context.getSource().sendSuccess(() -> Component.literal(bot.name() + " now " + path + " " + json), false);
        return 1;
    }
}
