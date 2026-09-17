package fr.hardel.jev.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** The {@code /jev} root: every mod command mounts under it, gamemaster permission for all. */
public final class JevCommand {

    private JevCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("jev").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(SpawnCommand.tree())
            .then(RemoveCommand.tree())
            .then(ListCommand.tree())
            .then(GoalCommand.tree())
            .then(PlanCommand.tree())
            .then(BehaveCommand.tree())
            .then(DoCommand.tree())
            .then(DebugCommand.tree());
    }
}
