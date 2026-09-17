---
name: commit
description: Règles de commit, Conventional Commits en 70 caractères max. À charger avant d'écrire un message de commit ou de commiter.
---

# Écrire un commit
Un commit suit https://www.conventionalcommits.org/en/v1.0.0/ en anglais. Le sujet dit ce que le commit change, en une ligne courte. Le diff dit le reste.

## Le sujet
`type: description` ou `type(scope): description`, 70 caractères maximum, sujet compris.
Description en minuscules, à l'impératif, sans point final. "fix: prevent racing of requests", pas "Fixed the racing of requests.".
Le scope est un mot, le domaine touché, `region`, `chunk`, `entity`, `poi`, `net`, `test`, `docs`. On l'omet quand le type suffit.
Un `!` après le type ou le scope marque un changement cassant, `refactor(region)!: drop the ring queue`.

## Les types
- `feat` une capacité nouvelle.
- `fix` un bug corrigé.
- `refactor` du code déplacé ou réécrit sans changer le comportement.
- `perf` du code plus rapide sans changer le comportement.
- `docs` docs/ ou roadmap/ seulement.
- `test` les tests seulement.
- `build` Gradle, dépendances, versions.
- `chore` le reste, config, scripts, nettoyage sans code.
- `revert` l'annulation d'un commit, le sujet reprend celui d'origine.

## Le corps
Le corps est optionnel et rare. Il sert quand le pourquoi ne se lit pas dans le diff, une ligne ou deux après une ligne vide, phrases complètes, pas de tiret exotique.
Un changement cassant l'explique dans un footer `BREAKING CHANGE: ` en plus du `!`.

## Un commit, un sujet
Si le sujet a besoin d'un "and", le diff porte deux changements, on fait deux commits. Un fix qui traîne un refactor se coupe en deux.

## Avant de commiter
Lire `git status` et `git diff --staged`, jamais deviner.
Ne commiter que ce que l'utilisateur a autorisé, /commit est cette autorisation pour le diff en cours, rien d'autre. Pas de ligne d'attribution.
