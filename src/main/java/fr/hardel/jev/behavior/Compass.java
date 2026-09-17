package fr.hardel.jev.behavior;

import net.minecraft.util.Mth;

public enum Compass {
    SOUTH(0), WEST(90), NORTH(180), EAST(-90);

    private final float yaw;

    Compass(float yaw) {
        this.yaw = yaw;
    }

    public float yaw() {
        return yaw;
    }

    public static Compass facing(float yaw) {
        float wrapped = Mth.wrapDegrees(yaw);
        if (Math.abs(wrapped) <= 45) {
            return SOUTH;
        }

        if (Math.abs(wrapped) >= 135) {
            return NORTH;
        }

        return wrapped > 0 ? WEST : EAST;
    }

    public String word() {
        return name().toLowerCase();
    }
}
