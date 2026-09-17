package fr.hardel.jev.sense;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Ears;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class HearingSense implements Sense {
    private static final long JUST_NOW_TICKS = 10;

    public record Heard(String sound, String source, @Nullable Relative where, String when) {
    }

    public record Hearing(List<Heard> recent) implements Perception {
    }

    @Override
    public Perception perceive(Bot bot) {
        ServerPlayer player = bot.player();
        Ears ears = bot.ears();
        List<Heard> heard = new ArrayList<>();
        
        for (Ears.Sound sound : ears.recent()) {
            Vec3 origin = sound.origin(player.level());
            Relative where = origin == null ? null : Relative.of(player, origin);
            String when = ears.now() - sound.tick() <= JUST_NOW_TICKS ? "just_now" : "seconds_ago";
            heard.add(new Heard(sound.id().getPath(), sound.source().getName(), where, when));
        }

        return new Hearing(heard);
    }
}
