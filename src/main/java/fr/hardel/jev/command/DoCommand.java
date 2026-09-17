package fr.hardel.jev.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.action.Action;
import fr.hardel.jev.bot.Bot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** {@code /jev do <bot> <action>}: drives one primitive by hand, the same ones the model chooses from. */
final class DoCommand {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(action -> Component.literal("No action named " + action));
    private static final DynamicCommandExceptionType UNAVAILABLE = new DynamicCommandExceptionType(action -> Component.literal("Action " + action + " is not possible right now"));

    private DoCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("do")
            .then(BotArgument.node()
                .then(Commands.argument("action", StringArgumentType.word())
                    .suggests((_, builder) -> SharedSuggestionProvider.suggest(JevBuiltInRegistries.ACTION.keySet().stream().map(Identifier::getPath), builder))
                    .executes(DoCommand::perform)));
    }

    private static int perform(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Bot bot = BotArgument.get(context);
        String path = StringArgumentType.getString(context, "action");
        Identifier id = Jev.id(path);
        Action action = JevBuiltInRegistries.ACTION.getValue(id);
        if (action == null) {
            throw UNKNOWN.create(path);
        }

        if (!action.available(bot)) {
            throw UNAVAILABLE.create(path);
        }

        bot.perform(id, action);
        context.getSource().sendSuccess(() -> Component.literal(bot.name() + ": " + path), false);
        return 1;
    }
}
