package fr.hardel.jev.behavior;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.sense.Perception;
import fr.hardel.jev.strategist.Schema;
import fr.hardel.jev.typesafe.Decision;
import fr.hardel.jev.typesafe.Instructions;
import fr.hardel.jev.typesafe.Question;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GatherWoodBehavior implements Behavior {
    private static final int STALL_TICKS = 6000;

    private record Progress(int logsCollected, int logsWanted) implements Perception {
    }

    @Override
    public String description() {
        return "Collect logs from nearby trees by hand or with an axe. Needs trees in sight or close by.";
    }

    @Override
    public Schema parameters() {
        return Schema.object().integer("count", "How many logs to add to the inventory, 1 to 32.", true);
    }

    @Override
    public Session begin(Bot bot, Arguments arguments) {
        int wanted = Math.clamp(arguments.integer("count", 8), 1, 32);
        return new Gathering(wanted, logs(bot.player()), bot.player().level().getGameTime());
    }

    private static int logs(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ItemTags.LOGS)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static final class Gathering implements Session {
        private final int wanted;
        private final int startLogs;
        private int bestLogs;
        private long lastProgress;

        private Gathering(int wanted, int startLogs, long now) {
            this.wanted = wanted;
            this.startLogs = startLogs;
            this.bestLogs = startLogs;
            this.lastProgress = now;
        }

        @Override
        public String summary() {
            return "gathering wood, " + (bestLogs - startLogs) + " of " + wanted + " logs";
        }

        @Override
        public Instructions instructions(Bot bot) {
            return new Instructions(
                "Collect logs from trees.",
                "Choose the next control input for this instant.",
                List.of(
                    "Logs are the blocks named like oak_log or birch_log in vision.blocks; walk toward the nearest ones.",
                    "When a log is under the crosshair and reachable (vision.crosshair), choose break; otherwise turn, look up or down and step until it is.",
                    "After breaking, walk over the dropped item to pick it up.",
                    "Read terrain: avoid blocked, drop, water and hazard footing; jump_forward over a step_up."
                ));
        }

        @Override
        public Map<String, Perception> context(Bot bot) {
            return Map.of("progress", new Progress(bestLogs - startLogs, wanted));
        }

        @Override
        public Set<Identifier> offered(Bot bot) {
            return ActionSet.with(ActionSet.locomotion(), "break");
        }

        @Override
        public Map<String, Question> questions(Bot bot) {
            return Map.of();
        }

        @Override
        public void observe(Bot bot, Decision decision) {
        }

        @Override
        public Outcome tick(Bot bot) {
            int logs = logs(bot.player());
            long now = bot.player().level().getGameTime();
            if (logs > bestLogs) {
                bestLogs = logs;
                lastProgress = now;
            }

            if (bestLogs - startLogs >= wanted) {
                return Outcome.DONE;
            }

            return now - lastProgress > STALL_TICKS ? Outcome.FAILED : Outcome.RUNNING;
        }
    }
}
