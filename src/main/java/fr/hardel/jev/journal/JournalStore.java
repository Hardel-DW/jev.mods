package fr.hardel.jev.journal;

import com.mojang.serialization.Codec;
import fr.hardel.jev.Jev;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public final class JournalStore extends SavedData {
    private static final Codec<JournalStore> CODEC = Codec.unboundedMap(Codec.STRING, Journal.CODEC).xmap(JournalStore::new, store -> store.journals);
    private static final SavedDataType<JournalStore> TYPE = new SavedDataType<>(Jev.id("journals"), JournalStore::new, CODEC, DataFixTypes.LEVEL);
    private final Map<String, Journal> journals;

    private JournalStore() {
        this(Map.of());
    }

    private JournalStore(Map<String, Journal> journals) {
        this.journals = new HashMap<>(journals);
    }

    public static JournalStore of(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Journal journal(String bot) {
        return journals.computeIfAbsent(bot, _ -> new Journal());
    }

    public void note(String bot, long tick, Entry.Kind kind, String text) {
        journal(bot).add(new Entry(tick, kind, text));
        setDirty();
    }

    public void place(String bot, String name, BlockPos pos) {
        journal(bot).place(name, pos);
        setDirty();
    }
}
