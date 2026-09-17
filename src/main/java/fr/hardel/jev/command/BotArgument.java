package fr.hardel.jev.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Bots;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/** The {@code <bot>} argument: a living bot by name, suggested from the roster. */
final class BotArgument {
    private static final String NAME = "bot";
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(name -> Component.literal("No bot named " + name));

    private BotArgument() {
    }

    static RequiredArgumentBuilder<CommandSourceStack, String> node() {
        return Commands.argument(NAME, StringArgumentType.word())
            .suggests((_, builder) -> SharedSuggestionProvider.suggest(Bots.all().stream().map(Bot::name), builder));
    }

    static Bot get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, NAME);
        Bot bot = Bots.get(name);
        if (bot == null) {
            throw UNKNOWN.create(name);
        }

        return bot;
    }
}
