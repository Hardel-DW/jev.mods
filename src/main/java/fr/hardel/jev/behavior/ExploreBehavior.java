package fr.hardel.jev.behavior;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.sense.Perception;
import fr.hardel.jev.strategist.Schema;
import fr.hardel.jev.typesafe.Decision;
import fr.hardel.jev.typesafe.Instructions;
import fr.hardel.jev.typesafe.Question;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExploreBehavior implements Behavior {
    private static final String ANY = "any";
    private static final int TICKS_PER_MINUTE = 1200;
    private static final double DANGER_THRESHOLD = 0.7;

    private record Heading(String wanted, String facing, String timeLeft) implements Perception {
    }

    @Override
    public String description() {
        return "Walk and look around to discover terrain, resources, structures and animals. Pick a heading and a duration.";
    }

    @Override
    public Schema parameters() {
        return Schema.object()
            .enumeration("direction", "Rough heading to keep.", true, ANY, "north", "south", "east", "west")
            .integer("minutes", "How long to explore before reporting back, 1 to 10.", true);
    }

    @Override
    public Session begin(Bot bot, Arguments arguments) {
        String direction = arguments.string("direction", ANY);
        int minutes = Math.clamp(arguments.integer("minutes", 3), 1, 10);
        return new Exploring(direction, bot.player().level().getGameTime() + (long) minutes * TICKS_PER_MINUTE);
    }

    private static final class Exploring implements Session {
        private final String direction;
        private final long until;

        private Exploring(String direction, long until) {
            this.direction = direction;
            this.until = until;
        }

        @Override
        public String summary() {
            return "exploring " + (direction.equals(ANY) ? "anywhere" : direction + "ward");
        }

        @Override
        public Instructions instructions(Bot bot) {
            return new Instructions(
                "Explore the Minecraft world on foot to discover what is around: terrain, trees, caves, water, animals, structures.",
                "Choose the next control input for this instant.",
                List.of(
                    "Keep roughly the wanted heading in context.heading.wanted; the current facing is context.heading.facing.",
                    "Read terrain: walk into clear or step_up footing, jump_forward over a step_up, turn away from blocked, drop, water and hazard.",
                    "Turn to look around now and then, especially when nothing interesting is visible.",
                    "Do not repeat an input that made no progress; if stuck, turn or jump."
                ));
        }

        @Override
        public Map<String, Perception> context(Bot bot) {
            long left = Math.max(0, until - bot.player().level().getGameTime());
            String timeLeft = left > TICKS_PER_MINUTE ? "minutes" : left > 200 ? "under_a_minute" : "seconds";
            return Map.of("heading", new Heading(direction, Compass.facing(bot.player().getYRot()).word(), timeLeft));
        }

        @Override
        public Set<Identifier> offered(Bot bot) {
            return ActionSet.locomotion();
        }

        @Override
        public Map<String, Question> questions(Bot bot) {
            Instructions danger = new Instructions("Stay alive while exploring.", "Is the player in immediate danger?",
                List.of("Hostile creatures close by, low health, standing next to lava or a drop."));
            return Map.of("danger", new Question.Noul(danger, "A threat needs a reaction now.", "Nothing threatens the player right now."));
        }

        @Override
        public void observe(Bot bot, Decision decision) {
            if (decision.noul("danger").noul() >= DANGER_THRESHOLD) {
                bot.note("danger felt");
            }
        }

        @Override
        public Outcome tick(Bot bot) {
            return bot.player().level().getGameTime() >= until ? Outcome.DONE : Outcome.RUNNING;
        }
    }
}
