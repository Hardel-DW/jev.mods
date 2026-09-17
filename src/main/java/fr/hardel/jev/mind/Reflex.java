package fr.hardel.jev.mind;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.action.Action;
import fr.hardel.jev.behavior.Session;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.sense.Perception;
import fr.hardel.jev.typesafe.Answer;
import fr.hardel.jev.typesafe.Decision;
import fr.hardel.jev.typesafe.Json;
import fr.hardel.jev.typesafe.Question;
import fr.hardel.jev.typesafe.TypeSafeClient;
import fr.hardel.jev.typesafe.TypeSafeException;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** One question round at a time: what to do next among what is possible, answered while the body finishes its previous move. */
final class Reflex {
    private static final String ACTION = "action";
    private static final double STALE_DISTANCE = 1.0;
    private static final long STALE_TICKS = 100;
    private static final long RETRY_TICKS = 20;
    private static final long BACKOFF_TICKS = 60;

    private final TypeSafeClient client;
    private @Nullable CompletableFuture<Decision> pending;
    private long askedAt;
    private Vec3 askedFrom = Vec3.ZERO;
    private Map<Identifier, Action> askedOffer = Map.of();
    private long quietUntil;
    private String lastRequest = "";
    private String lastResponse = "";

    private record Exchange(Map<String, Perception> state, Map<String, Question> questions) {
    }

    Reflex(TypeSafeClient client) {
        this.client = client;
    }

    void tick(Bot bot, Session session) {
        long now = bot.player().level().getGameTime();
        if (pending != null) {
            settle(bot, session, now);
            return;
        }

        if (!bot.busy() && now >= quietUntil) {
            ask(bot, session, now);
        }
    }

    private void ask(Bot bot, Session session, long now) {
        Map<Identifier, Action> offer = new LinkedHashMap<>();
        for (Identifier id : session.offered(bot)) {
            Action action = JevBuiltInRegistries.ACTION.getValue(id);
            if (action.available(bot)) {
                offer.put(id, action);
            }
        }

        if (offer.isEmpty()) {
            quietUntil = now + RETRY_TICKS;
            return;
        }

        Map<String, String> criteria = new LinkedHashMap<>();
        offer.forEach((id, action) -> criteria.put(id.getPath(), action.description()));
        Map<String, Perception> state = bot.perceive();
        state.putAll(session.context(bot));
        Map<String, Question> questions = new LinkedHashMap<>();
        questions.put(ACTION, new Question.Choice(session.instructions(bot), criteria));
        questions.putAll(session.questions(bot));
        lastRequest = Json.pretty(new Exchange(state, questions));
        pending = client.decide(state, questions);
        askedAt = now;
        askedFrom = bot.player().position();
        askedOffer = offer;
    }

    private void settle(Bot bot, Session session, long now) {
        if (!pending.isDone()) {
            if (now - askedAt > STALE_TICKS) {
                pending.cancel(true);
                pending = null;
                bot.note("reflex timed out");
            }

            return;
        }

        CompletableFuture<Decision> done = pending;
        pending = null;
        Decision decision;
        try {
            decision = done.join();
        } catch (CompletionException exception) {
            fail(bot, exception.getCause(), now);
            return;
        }

        lastResponse = Json.pretty(decision);
        if (bot.player().position().distanceTo(askedFrom) > STALE_DISTANCE) {
            bot.note("answer discarded, moved meanwhile");
            return;
        }

        session.observe(bot, decision);
        Answer.Choice choice = decision.choice(ACTION);
        Identifier id = Jev.id(choice.choice());
        Action action = askedOffer.get(id);
        if (action == null || !action.available(bot)) {
            bot.note("answer unusable: " + choice.choice());
            return;
        }

        bot.perform(id, action);
    }

    private void fail(Bot bot, Throwable cause, long now) {
        boolean transientFailure = cause instanceof TypeSafeException exception && exception.transient_();
        quietUntil = now + (transientFailure ? BACKOFF_TICKS : RETRY_TICKS);
        bot.note("reflex error: " + cause.getMessage());
        Jev.LOGGER.warn("Reflex of {} failed: {}", bot.name(), cause.getMessage());
    }

    String report() {
        return "request\n" + lastRequest + "\n\nresponse\n" + lastResponse;
    }
}
