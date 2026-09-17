package fr.hardel.jev.action;

import fr.hardel.jev.bot.Activity;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Crosshair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The use key: on the block or entity under the crosshair, else the held item on its own.
 * An item that finishes by itself (food) is held to the end; one that never would (bow, shield) is released after a charge.
 */
public final class UseAction implements Action {
    private static final int CHARGE_TICKS = 25;
    private static final int SELF_FINISHING_MAX = 1200;

    @Override
    public String description() {
        return "Right click: use the held item on what is under the crosshair, or on its own (eat, drink, place, open, interact).";
    }

    @Override
    public boolean available(Bot bot) {
        ServerPlayer player = bot.player();
        return switch (Crosshair.pick(player)) {
            case BlockHitResult hit -> player.level().mayInteract(player, hit.getBlockPos());
            case EntityHitResult _ -> true;
            default -> !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        };
    }

    @Override
    public Activity start(Bot bot) {
        ServerPlayer player = bot.player();
        HitResult target = Crosshair.pick(player);
        InteractionResult result = switch (target) {
            case BlockHitResult hit -> useOn(player, hit);
            case EntityHitResult hit -> player.interactOn(hit.getEntity(), InteractionHand.MAIN_HAND, hit.getLocation());
            default -> player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        };

        if (result instanceof InteractionResult.Success success && success.shouldSwing()) {
            player.swingAndResetAttackStrength(InteractionHand.MAIN_HAND, player.getMainHandItem().getInteractAnimation(), true);
        }

        bot.note("use " + target.getType().name().toLowerCase() + " " + result.getClass().getSimpleName().toLowerCase());
        return new Holding();
    }

    private static InteractionResult useOn(ServerPlayer player, BlockHitResult hit) {
        ServerLevel level = player.level();
        ItemStack held = player.getMainHandItem();
        return player.gameMode.useItemOn(player, level, held, InteractionHand.MAIN_HAND, hit);
    }

    private static final class Holding implements Activity {
        private int held;

        @Override
        public boolean tick(Bot bot) {
            ServerPlayer player = bot.player();
            if (!player.isUsingItem()) {
                return true;
            }

            boolean endless = player.getUseItem().getUseDuration(player) > SELF_FINISHING_MAX;
            if (endless && ++held >= CHARGE_TICKS) {
                player.releaseUsingItem();
                return true;
            }

            return false;
        }
    }
}
