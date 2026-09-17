package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Crosshair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VisionSense implements Sense {
    private static final double RANGE = 24;
    private static final float HORIZONTAL_FOV = 90;
    private static final float VERTICAL_FOV = 60;
    private static final float RAY_STEP = 7.5F;
    private static final int SEEN_MAX = 20;

    public record Seen(String block, int count, Relative.Distance nearest, Relative.Bearing bearing, Relative.Elevation elevation) {
    }

    public record Focus(String target, Relative.Distance distance, boolean reachable) {
    }

    public record Vision(Focus crosshair, List<Seen> blocks) implements Perception {
    }

    private static final class Sighting {
        private int count;
        private double nearest = Double.MAX_VALUE;
        private Vec3 nearestPoint = Vec3.ZERO;

        private void hit(Vec3 point, double distance) {
            count++;
            if (distance < nearest) {
                nearest = distance;
                nearestPoint = point;
            }
        }
    }

    @Override
    public Perception perceive(Bot bot) {
        ServerPlayer player = bot.player();
        return new Vision(focus(player), blocks(player));
    }

    private static Focus focus(ServerPlayer player) {
        HitResult hit = Crosshair.pick(player);
        double distance = hit.getLocation().distanceTo(player.getEyePosition());
        return switch (hit) {
            case BlockHitResult block -> new Focus(blockName(player.level(), block.getBlockPos()), Relative.Distance.of(distance), player.isWithinBlockInteractionRange(block.getBlockPos(), 1));
            case EntityHitResult entity -> new Focus(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getEntity().getType()).getPath(), Relative.Distance.of(distance), player.isWithinEntityInteractionRange(entity.getEntity(), 3));
            default -> new Focus("nothing", Relative.Distance.FAR, false);
        };
    }

    private static List<Seen> blocks(ServerPlayer player) {
        ServerLevel level = player.level();
        Vec3 eyes = player.getEyePosition();
        Map<String, Sighting> sightings = new HashMap<>();
        for (float pitch = -VERTICAL_FOV / 2; pitch <= VERTICAL_FOV / 2; pitch += RAY_STEP) {
            for (float yaw = -HORIZONTAL_FOV / 2; yaw <= HORIZONTAL_FOV / 2; yaw += RAY_STEP) {
                Vec3 direction = Vec3.directionFromRotation(player.getXRot() + pitch, player.getYRot() + yaw);
                BlockHitResult hit = level.clip(new ClipContext(eyes, eyes.add(direction.scale(RANGE)), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    sightings.computeIfAbsent(blockName(level, hit.getBlockPos()), _ -> new Sighting()).hit(hit.getLocation(), eyes.distanceTo(hit.getLocation()));
                }
            }
        }

        List<Seen> seen = new ArrayList<>();
        sightings.entrySet().stream()
            .sorted(Comparator.comparingInt((Map.Entry<String, Sighting> entry) -> entry.getValue().count).reversed())
            .limit(SEEN_MAX)
            .forEach(entry -> {
                Sighting sighting = entry.getValue();
                Relative where = Relative.of(player, sighting.nearestPoint);
                seen.add(new Seen(entry.getKey(), sighting.count, where.distance(), where.bearing(), where.elevation()));
            });

        return seen;
    }

    private static String blockName(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }
}
