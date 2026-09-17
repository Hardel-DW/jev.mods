package fr.hardel.jev.behavior;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ActionSet {

    private ActionSet() {
    }

    public static Set<Identifier> all() {
        return new LinkedHashSet<>(JevBuiltInRegistries.ACTION.keySet());
    }

    /** Walking, turning and looking, nothing that changes the world. */
    public static Set<Identifier> locomotion() {
        Set<Identifier> set = new LinkedHashSet<>();
        for (String path : new String[] {"forward", "backward", "strafe_left", "strafe_right", "sprint_forward", "jump_forward", "jump",
            "turn_left", "turn_right", "look_up", "look_down", "look_ahead", "wait"}) {
            set.add(Jev.id(path));
        }

        return set;
    }

    public static Set<Identifier> with(Set<Identifier> base, String... paths) {
        Set<Identifier> set = new LinkedHashSet<>(base);
        for (String path : paths) {
            set.add(Jev.id(path));
        }

        return set;
    }
}
