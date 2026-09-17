package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The footing a walker reads at a glance: what happens one to four steps ahead, left, right and behind. */
public final class TerrainSense implements Sense {
    private static final int REACH = 4;

    public enum Footing {
        CLEAR, STEP_UP, BLOCKED, STEP_DOWN, DROP, WATER, HAZARD, UNKNOWN
    }

    public record Terrain(Map<String, List<Footing>> steps) implements Perception {
    }

    @Override
    public Perception perceive(Bot bot) {
        ServerPlayer player = bot.player();
        Map<String, List<Footing>> steps = new LinkedHashMap<>();
        steps.put("ahead", probe(player, 0));
        steps.put("left", probe(player, -90));
        steps.put("right", probe(player, 90));
        steps.put("behind", probe(player, 180));
        return new Terrain(steps);
    }

    private static List<Footing> probe(ServerPlayer player, float yawOffset) {
        double yaw = Math.toRadians(player.getYRot() + yawOffset);
        Vec3 step = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        List<Footing> footings = new ArrayList<>();
        for (int distance = 1; distance <= REACH; distance++) {
            Vec3 sample = player.position().add(step.scale(distance));
            footings.add(footing(player.level(), BlockPos.containing(sample)));
        }

        return footings;
    }

    private static Footing footing(ServerLevel level, BlockPos feet) {
        if (!level.isLoaded(feet)) {
            return Footing.UNKNOWN;
        }

        BlockState ground = level.getBlockState(feet.below());
        BlockState atFeet = level.getBlockState(feet);
        BlockState atHead = level.getBlockState(feet.above());
        
        if (hazard(ground) || hazard(atFeet)) {
            return Footing.HAZARD;
        }

        if (atFeet.getFluidState().is(FluidTags.WATER) || ground.getFluidState().is(FluidTags.WATER)) {
            return Footing.WATER;
        }

        boolean feetSolid = solid(level, feet, atFeet);
        boolean headSolid = solid(level, feet.above(), atHead);
        if (feetSolid) {
            return !headSolid && !solid(level, feet.above(2), level.getBlockState(feet.above(2))) ? Footing.STEP_UP : Footing.BLOCKED;
        }

        if (headSolid) {
            return Footing.BLOCKED;
        }

        if (solid(level, feet.below(), ground)) {
            return Footing.CLEAR;
        }

        return solid(level, feet.below(2), level.getBlockState(feet.below(2))) ? Footing.STEP_DOWN : Footing.DROP;
    }

    private static boolean solid(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean hazard(BlockState state) {
        return state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.FIRE) || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SWEET_BERRY_BUSH);
    }
}
