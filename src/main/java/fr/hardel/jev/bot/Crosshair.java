package fr.hardel.jev.bot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** What sits under the bot's crosshair, the way the client resolves it: the nearest of the block ray and the entity ray, each within its own reach. */
public final class Crosshair {
    private static final double ENTITY_PICK_MARGIN = 1.0;

    private Crosshair() {
    }

    public static HitResult pick(ServerPlayer player) {
        double reach = player.blockInteractionRange();
        HitResult block = player.pick(reach, 1, false);
        double limit = block.getType() == HitResult.Type.MISS ? reach : block.getLocation().distanceTo(player.getEyePosition());
        EntityHitResult entity = pickEntity(player, Math.min(limit, player.entityInteractionRange()));
        return entity == null ? block : entity;
    }

    private static @Nullable EntityHitResult pickEntity(ServerPlayer player, double reach) {
        Vec3 eyes = player.getEyePosition();
        Vec3 view = player.getViewVector(1);
        Vec3 end = eyes.add(view.scale(reach));
        AABB sweep = player.getBoundingBox().expandTowards(view.scale(reach)).inflate(ENTITY_PICK_MARGIN);
        Entity nearest = null;
        Vec3 nearestHit = null;
        double nearestDistance = reach * reach;
        for (Entity candidate : player.level().getEntities(player, sweep, Entity::isPickable)) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Vec3 hit = box.clip(eyes, end).orElse(null);
            if (hit == null) {
                continue;
            }

            double distance = eyes.distanceToSqr(hit);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestHit = hit;
                nearestDistance = distance;
            }
        }

        return nearest == null ? null : new EntityHitResult(nearest, nearestHit);
    }

    public static @Nullable BlockHitResult block(ServerPlayer player) {
        return pick(player) instanceof BlockHitResult hit ? hit : null;
    }
}
