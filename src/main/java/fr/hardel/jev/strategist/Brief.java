package fr.hardel.jev.strategist;

import fr.hardel.jev.sense.Perception;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

/** Everything the strategist gets to decide from, rebuilt on every call: no conversation, no hidden state. */
public record Brief(String bot, String directive, String doing, String lastOutcome, Map<String, Perception> senses, Map<String, BlockPos> places, List<String> journal) {
}
