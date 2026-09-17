package fr.hardel.jev.typesafe;

import java.util.List;

public record Instructions(String goal, String task, List<String> rules) {
}
