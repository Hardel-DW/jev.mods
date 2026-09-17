package fr.hardel.jev.journal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Entry(long tick, Kind kind, String text) {
    public static final Codec<Entry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Codec.LONG.fieldOf("tick").forGetter(Entry::tick),
        Kind.CODEC.fieldOf("kind").forGetter(Entry::kind),
        Codec.STRING.fieldOf("text").forGetter(Entry::text)
    ).apply(builder, Entry::new));

    public enum Kind implements net.minecraft.util.StringRepresentable {
        DIRECTIVE, PLAN, OUTCOME, DISCOVERY, EVENT;

        public static final Codec<Kind> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
