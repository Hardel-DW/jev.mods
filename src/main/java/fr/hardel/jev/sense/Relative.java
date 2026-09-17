package fr.hardel.jev.sense;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record Relative(Bearing bearing, Elevation elevation, Distance distance) {

    public enum Bearing {
        AHEAD, AHEAD_RIGHT, RIGHT, BEHIND_RIGHT, BEHIND, BEHIND_LEFT, LEFT, AHEAD_LEFT;

        static Bearing of(float degrees) {
            float clockwise = Mth.wrapDegrees(degrees);
            float magnitude = Math.abs(clockwise);
            if (magnitude < 22.5F) {
                return AHEAD;
            }

            if (magnitude > 157.5F) {
                return BEHIND;
            }

            boolean right = clockwise > 0;
            if (magnitude < 67.5F) {
                return right ? AHEAD_RIGHT : AHEAD_LEFT;
            }

            if (magnitude < 112.5F) {
                return right ? RIGHT : LEFT;
            }

            return right ? BEHIND_RIGHT : BEHIND_LEFT;
        }
    }

    public enum Elevation {
        ABOVE, LEVEL, BELOW;

        private static final double MARGIN = 1.5;

        static Elevation of(double dy) {
            if (dy > MARGIN) {
                return ABOVE;
            }

            return dy < -MARGIN ? BELOW : LEVEL;
        }
    }

    public enum Distance {
        TOUCHING, NEAR, CLOSE, MEDIUM, FAR;

        public static Distance of(double blocks) {
            if (blocks <= 1.5) {
                return TOUCHING;
            }

            if (blocks <= 4) {
                return NEAR;
            }

            if (blocks <= 8) {
                return CLOSE;
            }

            return blocks <= 16 ? MEDIUM : FAR;
        }
    }

    public static Relative of(ServerPlayer player, Vec3 point) {
        Vec3 eyes = player.getEyePosition();
        double dx = point.x - eyes.x;
        double dz = point.z - eyes.z;
        float yawTo = (float) Math.toDegrees(Math.atan2(-dx, dz));
        return new Relative(Bearing.of(yawTo - player.getYRot()), Elevation.of(point.y - eyes.y), Distance.of(eyes.distanceTo(point)));
    }
}
