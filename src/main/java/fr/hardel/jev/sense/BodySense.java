package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BodySense implements Sense {
    private static final int HUNGRY_BELOW = 14;
    private static final int INVENTORY_SUMMARY_MAX = 20;

    public record Body(int health, String condition, int food, boolean hungry, String breath, String daylight, String weather, String light, boolean underground, String posture, String holding, List<String> hotbar, Map<String, Integer> inventory, boolean armored) implements Perception {
    }

    @Override
    public Perception perceive(Bot bot) {
        ServerPlayer player = bot.player();
        ServerLevel level = player.level();
        BlockPos feet = player.blockPosition();
        int food = player.getFoodData().getFoodLevel();
        return new Body(
            (int) player.getHealth(),
            condition(player.getHealth() / player.getMaxHealth()),
            food,
            food < HUNGRY_BELOW,
            breath(player),
            daylight(level),
            weather(level),
            light(level.getMaxLocalRawBrightness(feet)),
            !level.canSeeSky(feet),
            posture(player),
            name(player.getMainHandItem()),
            hotbar(player),
            inventory(player),
            armored(player)
        );
    }

    private static String condition(float ratio) {
        if (ratio > 0.66F) {
            return "healthy";
        }

        return ratio > 0.33F ? "hurt" : "critical";
    }

    private static String breath(ServerPlayer player) {
        if (!player.isUnderWater()) {
            return "fine";
        }

        float ratio = (float) player.getAirSupply() / player.getMaxAirSupply();
        return ratio > 0.5F ? "holding_breath" : "running_out_of_air";
    }

    private static String daylight(ServerLevel level) {
        if (level.dimensionType().hasFixedTime()) {
            return "timeless";
        }

        long time = level.getDefaultClockTime() % 24000;
        if (time < 6000) {
            return "morning";
        }

        if (time < 12000) {
            return "afternoon";
        }

        if (time < 13000) {
            return "dusk";
        }

        return time < 23000 ? "night" : "dawn";
    }

    private static String weather(ServerLevel level) {
        if (level.isThundering()) {
            return "thunderstorm";
        }

        return level.isRaining() ? "rain" : "clear";
    }

    private static String light(int brightness) {
        if (brightness <= 4) {
            return "dark";
        }

        return brightness <= 9 ? "dim" : "lit";
    }

    private static String posture(ServerPlayer player) {
        if (player.isSwimming()) {
            return "swimming";
        }

        if (player.isCrouching()) {
            return "sneaking";
        }

        return player.isSprinting() ? "sprinting" : "standing";
    }

    private static String name(ItemStack stack) {
        return stack.isEmpty() ? "empty_hand" : BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static List<String> hotbar(ServerPlayer player) {
        List<String> hotbar = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            hotbar.add(stack.isEmpty() ? "empty" : name(stack) + " x" + stack.getCount());
        }

        return hotbar;
    }

    private static Map<String, Integer> inventory(ServerPlayer player) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty()) {
                counts.merge(name(stack), stack.getCount(), Integer::sum);
            }
        }

        Map<String, Integer> summary = new LinkedHashMap<>();
        counts.entrySet().stream()
            .sorted(Comparator.comparingInt((Map.Entry<String, Integer> entry) -> entry.getValue()).reversed())
            .limit(INVENTORY_SUMMARY_MAX)
            .forEach(entry -> summary.put(entry.getKey(), entry.getValue()));
            
        return summary;
    }

    private static boolean armored(ServerPlayer player) {
        return !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() || !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || !player.getItemBySlot(EquipmentSlot.LEGS).isEmpty() || !player.getItemBySlot(EquipmentSlot.FEET).isEmpty();
    }
}
