package fr.hardel.jev.typesafe;

import java.util.List;
import java.util.Map;

/** The three primitives of the System One API, serialised as is: a closed choice, a calibrated yes or no, a level on a rubric. */
public sealed interface Question permits Question.Choice, Question.Noul, Question.Score {

    record Choice(String type, Instructions instructions, Map<String, String> criteria) implements Question {
        public Choice(Instructions instructions, Map<String, String> criteria) {
            this("choice", instructions, criteria);
        }
    }

    record Noul(String type, Instructions instructions, Map<String, String> criteria) implements Question {
        public Noul(Instructions instructions, String whenTrue, String whenFalse) {
            this("noul", instructions, Map.of("true", whenTrue, "false", whenFalse));
        }
    }

    record Score(String type, Instructions instructions, List<String> criteria) implements Question {
        public Score(Instructions instructions, List<String> levels) {
            this("score", instructions, levels);
        }
    }
}
