package fr.hardel.jev.mind;

import fr.hardel.jev.Jev;
import fr.hardel.jev.JevBuiltInRegistries;
import fr.hardel.jev.JevConfig;
import fr.hardel.jev.behavior.Arguments;
import fr.hardel.jev.behavior.Behavior;
import fr.hardel.jev.behavior.Outcome;
import fr.hardel.jev.behavior.Session;
import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Brain;
import fr.hardel.jev.journal.Entry;
import fr.hardel.jev.journal.Journal;
import fr.hardel.jev.journal.JournalStore;
import fr.hardel.jev.strategist.Brief;
import fr.hardel.jev.strategist.CodexClient;
import fr.hardel.jev.strategist.Plan;
import fr.hardel.jev.strategist.Strategist;
import fr.hardel.jev.typesafe.TypeSafeClient;
import fr.hardel.jev.typesafe.TypeSafeKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** The two thinkers of one bot wired to its journal: the strategist picks a behavior, the reflex plays it, outcomes wake the strategist. */
public final class Mind implements Brain {
    private static final long CHECK_IN_TICKS = 1800;
    private static final int JOURNAL_LINES = 30;

    private final Bot bot;
    private final JournalStore journals;
    private final Strategist strategist;
    private final @Nullable Reflex reflex;
    private @Nullable Session session;
    private String behaviorName = "none";
    private String lastOutcome = "none";
    private String directive;

    public Mind(Bot bot) {
        this.bot = bot;
        JevConfig config = JevConfig.get();
        journals = JournalStore.of(bot.player().level().getServer());
        strategist = new Strategist(new CodexClient(config.strategistModel(), config.strategistEffort()));
        reflex = TypeSafeKey.find(config.typesafeEndpoint(), JevConfig.directory()).map(key -> new Reflex(new TypeSafeClient(config.typesafeEndpoint(), key))).orElse(null);
        directive = journal().entries().stream().filter(entry -> entry.kind() == Entry.Kind.DIRECTIVE).map(Entry::text).findFirst().orElse("");
        if (reflex == null) {
            bot.note("no TypeSafe key, reflexes off");
        }

        strategist.wake("joined the world", true);
    }

    private Journal journal() {
        return journals.journal(bot.name());
    }

    @Override
    public void tick(Bot bot) {
        long now = bot.player().level().getGameTime();
        if (session != null) {
            Outcome outcome = session.tick(bot);
            if (outcome != Outcome.RUNNING) {
                finish(outcome, now);
            } else if (reflex != null) {
                reflex.tick(bot, session);
            }
        }

        if (session != null && strategist.quietFor(now) >= CHECK_IN_TICKS && !strategist.thinking()) {
            strategist.wake("check in", false);
        }

        Plan plan = strategist.tick(now, this::brief);
        if (plan != null) {
            adopt(plan, now);
        }
    }

    private void finish(Outcome outcome, long now) {
        lastOutcome = behaviorName + " " + outcome.name().toLowerCase() + ": " + session.summary();
        journals.note(bot.name(), now, Entry.Kind.OUTCOME, lastOutcome);
        bot.note(lastOutcome);
        session = null;
        behaviorName = "none";
        strategist.wake(outcome.name().toLowerCase(), true);
    }

    private void adopt(Plan plan, long now) {
        Identifier id = Jev.id(plan.behavior());
        Behavior behavior = JevBuiltInRegistries.BEHAVIOR.getValue(id);
        if (behavior == null) {
            bot.note("strategist named an unknown behavior " + plan.behavior());
            strategist.wake("unknown behavior", false);
            return;
        }

        if (!plan.reasoning().isEmpty()) {
            journals.note(bot.name(), now, Entry.Kind.PLAN, plan.reasoning());
        }

        begin(id, behavior, Arguments.parse(plan.arguments()), now);
    }

    private void begin(Identifier id, Behavior behavior, Arguments arguments, long now) {
        session = behavior.begin(bot, arguments);
        behaviorName = id.getPath();
        journals.note(bot.name(), now, Entry.Kind.PLAN, behaviorName + " " + arguments.json());
        bot.note("begin " + behaviorName + " " + arguments.json());
    }

    private Brief brief() {
        Journal journal = journal();
        List<String> recent = journal.entries().stream().limit(JOURNAL_LINES).map(entry -> entry.kind().getSerializedName() + ": " + entry.text()).toList();
        String doing = session == null ? "nothing" : behaviorName + ", " + session.summary();
        return new Brief(bot.name(), directive, doing, lastOutcome, bot.perceive(), journal.places(), recent);
    }

    @Override
    public String report() {
        String head = "behavior: " + behaviorName + (session == null ? "" : " (" + session.summary() + ")") + "\ndirective: " + directive + "\nlast outcome: " + lastOutcome
            + "\nstrategist: " + (strategist.thinking() ? "thinking" : "idle") + "\nlast plan: " + strategist.lastPlan();
        return reflex == null ? head : head + "\n\n" + reflex.report();
    }

    @Override
    public void directive(String text) {
        directive = text;
        journals.note(bot.name(), bot.player().level().getGameTime(), Entry.Kind.DIRECTIVE, text);
        strategist.wake("directive", true);
    }

    @Override
    public boolean behave(Identifier id, String argumentsJson) {
        Behavior behavior = JevBuiltInRegistries.BEHAVIOR.getValue(id);
        if (behavior == null) {
            return false;
        }

        begin(id, behavior, Arguments.parse(argumentsJson), bot.player().level().getGameTime());
        return true;
    }

    @Override
    public void replan() {
        strategist.wake("asked by the player", true);
    }
}
