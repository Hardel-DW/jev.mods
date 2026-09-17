package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Crosshair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Holds the attack key on the block under the crosshair until the server has broken it, with the timing a client would produce. */
public final class BreakAction implements Action {
    private static final int SEQUENCE = 0;

    @Override
    public String description() {
        return "Mine the block under the crosshair with the held item, for as long as it takes.";
    }

    @Override
    public boolean available(Bot bot) {
        ServerPlayer player = bot.player();
        BlockHitResult hit = Crosshair.block(player);
        if (hit == null) {
            return false;
        }

        ServerLevel level = player.level();
        BlockState state = level.getBlockState(hit.getBlockPos());
        return !state.isAir() && state.getDestroySpeed(level, hit.getBlockPos()) >= 0 && level.mayInteract(player, hit.getBlockPos());
    }

    @Override
    public Activity start(Bot bot) {
        ServerPlayer player = bot.player();
        BlockHitResult hit = Crosshair.block(player);
        player.gameMode.handleBlockBreakAction(hit.getBlockPos(), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, hit.getDirection(), player.level().getMaxY(), SEQUENCE);
        return new Breaking(hit.getBlockPos());
    }

    /** Mirrors the client: swing every tick, send stop once its own progress estimate completes, abort if the crosshair leaves the block. */
    private static final class Breaking implements Activity {
        private final BlockPos pos;
        private int ticks;
        private boolean stopped;

        private Breaking(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean tick(Bot bot) {
            ServerPlayer player = bot.player();
            ServerLevel level = player.level();
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                bot.note("broke " + pos.toShortString());
                return true;
            }

            if (stopped) {
                return false;
            }

            BlockHitResult hit = Crosshair.block(player);
            if (hit == null || !hit.getBlockPos().equals(pos)) {
                player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, hit == null ? null : hit.getDirection(), level.getMaxY(), SEQUENCE);
                bot.note("lost sight of " + pos.toShortString());
                return true;
            }

            player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getAttackAnimation(), false);
            float progress = state.getDestroyProgress(player, level, pos) * (++ticks + 1);
            if (progress >= 1) {
                player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, hit.getDirection(), level.getMaxY(), SEQUENCE);
                stopped = true;
            }

            return false;
        }
    }
}
