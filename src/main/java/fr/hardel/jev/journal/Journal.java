package fr.hardel.jev.journal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Journal {
    private static final int ENTRY_CAPACITY = 200;
    static final Codec<Journal> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Entry.CODEC.listOf().fieldOf("entries").forGetter(journal -> List.copyOf(journal.entries)),
        Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).fieldOf("places").forGetter(journal -> journal.places)
    ).apply(builder, Journal::new));

    private final Deque<Entry> entries;
    private final Map<String, BlockPos> places;

    Journal() {
        this(List.of(), Map.of());
    }

    private Journal(List<Entry> entries, Map<String, BlockPos> places) {
        this.entries = new ArrayDeque<>(entries);
        this.places = new LinkedHashMap<>(places);
    }

    void add(Entry entry) {
        if (entries.size() == ENTRY_CAPACITY) {
            entries.pollFirst();
        }

        entries.addLast(entry);
    }

    void place(String name, BlockPos pos) {
        places.put(name, pos.immutable());
    }

    /** Most recent first. */
    public List<Entry> entries() {
        return new ArrayList<>(entries).reversed();
    }

    public Map<String, BlockPos> places() {
        return Map.copyOf(places);
    }
}
