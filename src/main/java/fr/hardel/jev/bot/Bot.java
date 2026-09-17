package fr.hardel.jev.bot;

import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.action.Action;
import fr.hardel.jev.sense.Perception;
import fr.hardel.jev.sense.Sense;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Bot {
    private static final int LOG_CAPACITY = 64;
    private final ServerPlayer player;
    private final Ears ears;
    private final Controls controls = new Controls();
    private final Deque<String> log = new ArrayDeque<>();
    private final Brain brain;
    private @Nullable Activity activity;
    private String activityName = "idle";

    Bot(ServerPlayer player, Ears ears, Function<Bot, Brain> brain) {
        this.player = player;
        this.ears = ears;
        controls.adopt(player);
        this.brain = brain.apply(this);
    }

    public ServerPlayer player() {
        return player;
    }

    public String name() {
        return player.getGameProfile().name();
    }

    public Ears ears() {
        return ears;
    }

    public Controls controls() {
        return controls;
    }

    public Brain brain() {
        return brain;
    }

    public boolean busy() {
        return activity != null;
    }

    public String activityName() {
        return activityName;
    }

    public void perform(Identifier id, Action action) {
        controls.release();
        activity = action.start(this);
        activityName = id.getPath();
        note("start " + activityName);
    }

    public void note(String event) {
        if (log.size() == LOG_CAPACITY) {
            log.pollFirst();
        }

        log.addLast(player.level().getGameTime() + " " + event);
    }

    public List<String> log() {
        return new ArrayList<>(log).reversed();
    }

    void tick() {
        ears.tick(player.level().getGameTime());
        if (activity != null && activity.tick(this)) {
            activity = null;
            activityName = "idle";
            controls.release();
        }

        brain.tick(this);
        controls.apply(player);
        player.doTick();
        player.level().getChunkSource().move(player);
    }

    public Map<String, Perception> perceive() {
        Map<String, Perception> perceptions = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<Sense>, Sense> entry : JevBuiltInRegistries.SENSE.entrySet()) {
            perceptions.put(entry.getKey().identifier().getPath(), entry.getValue().perceive(this));
        }

        return perceptions;
    }
}
