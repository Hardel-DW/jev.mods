package fr.hardel.jev.strategist;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.behavior.Behavior;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * The slow thinker of one bot. Woken by events, it asks the LLM once, at most every half minute unless the wake is urgent,
 * and hands back a plan; the behaviors it may pick are the registry, presented as functions.
 */
public final class Strategist {
    private static final long MIN_INTERVAL_TICKS = 600;
    private static final long STALE_TICKS = 2400;
    private static final String INSTRUCTIONS = """
        You are the strategist of a Minecraft survival player controlled by software. You do not control the character; \
        you choose which behavior it runs next and with which parameters, by calling exactly one function.
        The long term goal is to finish the game: gather resources, craft tools, find diamonds, reach the Nether, then the End, and defeat the ender dragon. \
        Play it like a person who takes their time: explore, build, farm, fight or avoid monsters, and adapt to what the world offers.
        The player's directive, when present, overrides everything else. The journal lists what happened recently, most recent first. \
        Read the senses to judge the situation: what is visible, what the body needs, what was heard.
        Never repeat a behavior that just failed with the same parameters; change the plan instead.""";

    private final CodexClient client;
    private @Nullable CompletableFuture<Plan> pending;
    private long askedAt;
    private long lastCall = Long.MIN_VALUE;
    private @Nullable String wanted;
    private boolean urgent;
    private String lastPlan = "none yet";

    public Strategist(CodexClient client) {
        this.client = client;
    }

    public void wake(String reason, boolean urgent) {
        wanted = reason;
        this.urgent |= urgent;
    }

    public boolean thinking() {
        return pending != null;
    }

    public long quietFor(long now) {
        return now - lastCall;
    }

    /** Advances the slow loop; a plan comes back on the tick its answer arrived. */
    public @Nullable Plan tick(long now, Supplier<Brief> brief) {
        if (pending != null) {
            return settle(now);
        }

        if (wanted == null || !urgent && now - lastCall < MIN_INTERVAL_TICKS) {
            return null;
        }

        Jev.LOGGER.info("Strategist asked: {}", wanted);
        pending = client.plan(INSTRUCTIONS, brief.get(), functions());
        askedAt = now;
        lastCall = now;
        wanted = null;
        urgent = false;
        return null;
    }

    private @Nullable Plan settle(long now) {
        if (!pending.isDone()) {
            if (now - askedAt > STALE_TICKS) {
                pending.cancel(true);
                pending = null;
                lastPlan = "timed out";
            }

            return null;
        }

        CompletableFuture<Plan> done = pending;
        pending = null;
        try {
            Plan plan = done.join();
            lastPlan = plan.behavior() + " " + plan.arguments() + (plan.reasoning().isEmpty() ? "" : "\n" + plan.reasoning());
            return plan;
        } catch (CompletionException exception) {
            lastPlan = "failed: " + exception.getCause().getMessage();
            Jev.LOGGER.warn("Strategist failed: {}", exception.getCause().getMessage());
            wanted = "retry";
            return null;
        }
    }

    private static List<CodexClient.Function> functions() {
        List<CodexClient.Function> functions = new ArrayList<>();
        for (Map.Entry<ResourceKey<Behavior>, Behavior> entry : JevBuiltInRegistries.BEHAVIOR.entrySet()) {
            functions.add(new CodexClient.Function(entry.getKey().identifier().getPath(), entry.getValue().description(), entry.getValue().parameters()));
        }

        return functions;
    }

    public String lastPlan() {
        return lastPlan;
    }
}
