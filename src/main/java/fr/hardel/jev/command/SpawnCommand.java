package fr.hardel.jev.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.hardel.jev.bot.Bots;
import fr.hardel.jev.mind.Mind;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /jev spawn <name>}: a bot joins and stands where the command was run, facing the same way. */
final class SpawnCommand {
    private static final DynamicCommandExceptionType EXISTS = new DynamicCommandExceptionType(name -> Component.literal("Bot " + name + " is already here"));

    private SpawnCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("spawn")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "name"))));
    }

    private static int spawn(CommandSourceStack source, String name) throws CommandSyntaxException {
        if (Bots.get(name) != null) {
            throw EXISTS.create(name);
        }

        Bots.spawn(source.getServer(), source.getLevel(), name, source.getPosition(), source.getRotation().y, Mind::new);
        source.sendSuccess(() -> Component.literal("Bot " + name + " joined"), true);
        return 1;
    }
}
