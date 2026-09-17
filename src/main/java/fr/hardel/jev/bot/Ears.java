package fr.hardel.jev.bot;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Ears {
    private static final int MEMORY_TICKS = 100;
    private static final int CAPACITY = 32;
    private final Deque<Sound> sounds = new ArrayDeque<>();
    private long now;

    public record Sound(long tick, Identifier id, SoundSource source, @Nullable Vec3 position, int entityId) {
        public @Nullable Vec3 origin(ServerLevel level) {
            if (position != null) {
                return position;
            }

            Entity emitter = level.getEntity(entityId);
            return emitter == null ? null : emitter.position();
        }
    }

    void hear(ClientboundSoundPacket packet) {
        remember(new Sound(now, id(packet.getSound()), packet.getSource(), new Vec3(packet.getX(), packet.getY(), packet.getZ()), -1));
    }

    void hear(ClientboundSoundEntityPacket packet) {
        remember(new Sound(now, id(packet.getSound()), packet.getSource(), null, packet.getId()));
    }

    private static Identifier id(Holder<SoundEvent> sound) {
        return sound.value().location();
    }

    private void remember(Sound sound) {
        if (sounds.size() == CAPACITY) {
            sounds.pollFirst();
        }

        sounds.addLast(sound);
    }

    void tick(long tick) {
        now = tick;
        while (!sounds.isEmpty() && now - sounds.peekFirst().tick() > MEMORY_TICKS) {
            sounds.pollFirst();
        }
    }

    public long now() {
        return now;
    }

    public List<Sound> recent() {
        List<Sound> recent = new ArrayList<>(sounds);
        return recent.reversed();
    }
}
