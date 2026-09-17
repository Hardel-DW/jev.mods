package fr.hardel.jev.bot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * The keys a player holds this tick, applied to the entity the way the client applies its own keyboard,
 * so vanilla physics moves the bot. Looking is a target reached at a human turning speed, never a snap.
 */
public final class Controls {
    private static final float TURN_PER_TICK = 18;
    private static final float INPUT_SCALE = 0.98F;
    private static final int SPRINT_MIN_FOOD = 7;

    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private boolean sprint;
    private float targetYaw;
    private float targetPitch;

    /** Forward and strafe as key intents: 1 forward, -1 backward; 1 left, -1 right. */
    public void move(float forward, float strafe) {
        this.forward = forward;
        this.strafe = strafe;
    }

    public void jump(boolean jump) {
        this.jump = jump;
    }

    public void sneak(boolean sneak) {
        this.sneak = sneak;
    }

    public void sprint(boolean sprint) {
        this.sprint = sprint;
    }

    public void look(float yaw, float pitch) {
        targetYaw = Mth.wrapDegrees(yaw);
        targetPitch = Mth.clamp(pitch, -90, 90);
    }

    public void lookAt(ServerPlayer player, Vec3 point) {
        Vec3 eyes = player.getEyePosition();
        double dx = point.x - eyes.x;
        double dy = point.y - eyes.y;
        double dz = point.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        look((float) Math.toDegrees(Math.atan2(-dx, dz)), (float) -Math.toDegrees(Math.atan2(dy, horizontal)));
    }

    public void turn(ServerPlayer player, float yawDelta, float pitchDelta) {
        look(player.getYRot() + yawDelta, player.getXRot() + pitchDelta);
    }

    public boolean looking(ServerPlayer player) {
        return Math.abs(Mth.degreesDifference(player.getYRot(), targetYaw)) < 0.5F && Math.abs(player.getXRot() - targetPitch) < 0.5F;
    }

    /** Releases every key, the look target stays. */
    public void release() {
        forward = 0;
        strafe = 0;
        jump = false;
        sneak = false;
        sprint = false;
    }

    /** The bot starts by looking where it already looks, otherwise the first tick would spin it to yaw 0. */
    void adopt(ServerPlayer player) {
        look(player.getYRot(), player.getXRot());
    }

    void apply(ServerPlayer player) {
        float yaw = Mth.approachDegrees(player.getYRot(), targetYaw, TURN_PER_TICK);
        float pitch = Mth.approach(player.getXRot(), targetPitch, TURN_PER_TICK);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);

        boolean slow = player.isCrouching() || player.isVisuallyCrawling();
        float scale = INPUT_SCALE * (slow ? (float) player.getAttributeValue(Attributes.SNEAKING_SPEED) : 1);
        player.zza = forward * scale;
        player.xxa = strafe * scale;
        player.setJumping(jump);
        player.setShiftKeyDown(sneak);
        player.setSprinting(sprint && forward > 0 && !sneak && player.getFoodData().getFoodLevel() >= SPRINT_MIN_FOOD);
    }
}
