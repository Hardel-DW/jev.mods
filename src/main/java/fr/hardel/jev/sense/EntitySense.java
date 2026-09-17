package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EntitySense implements Sense {
    private static final double RANGE = 24;
    private static final double FOV_COS = Math.cos(Math.toRadians(70));
    private static final int SEEN_MAX = 12;

    public record Seen(String type, Relative where, boolean hostile, String health) {
    }

    public record Entities(List<Seen> visible) implements Perception {
    }

    @Override
    public Perception perceive(Bot bot) {
        ServerPlayer player = bot.player();
        Vec3 eyes = player.getEyePosition();
        Vec3 view = player.getViewVector(1);
        List<Entity> visible = player.level().getEntities(player, player.getBoundingBox().inflate(RANGE), entity -> entity.isAlive() && inView(eyes, view, entity) && player.hasLineOfSight(entity));
        visible.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(eyes)));
        List<Seen> seen = new ArrayList<>();
        for (Entity entity : visible.subList(0, Math.min(visible.size(), SEEN_MAX))) {
            seen.add(new Seen(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(), Relative.of(player, entity.position()), entity instanceof Enemy, health(entity)));
        }

        return new Entities(seen);
    }

    private static boolean inView(Vec3 eyes, Vec3 view, Entity entity) {
        Vec3 toEntity = entity.getEyePosition().subtract(eyes).normalize();
        return toEntity.dot(view) >= FOV_COS;
    }

    private static String health(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return "none";
        }

        float ratio = living.getHealth() / living.getMaxHealth();
        if (ratio > 0.66F) {
            return "healthy";
        }

        return ratio > 0.33F ? "hurt" : "dying";
    }
}
