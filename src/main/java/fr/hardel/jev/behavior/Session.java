package fr.hardel.jev.behavior;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.sense.Perception;
import fr.hardel.jev.typesafe.Decision;
import fr.hardel.jev.typesafe.Instructions;
import fr.hardel.jev.typesafe.Question;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

public interface Session {
    String summary();

    Instructions instructions(Bot bot);

    Map<String, Perception> context(Bot bot);

    Set<Identifier> offered(Bot bot);

    Map<String, Question> questions(Bot bot);

    void observe(Bot bot, Decision decision);

    Outcome tick(Bot bot);
}
