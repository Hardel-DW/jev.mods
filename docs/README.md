# L'objectifs de Jev :
Il doit finir le jeu peut importe de quel maniéres.

## Comment ?
Dans le jeu de bases il faut :
> Obtenir du bois, obtenir de la pierre, faire la table de craft, le four, cuire, faire les outils, aller en grotes pour le diamant et faire la pioches en diamant, et aller dans le nether pour la forteresses et les blaze rod, les combiner a des enderpearl, aller dans le stronghold pour battre le dragon.  

Mais c'est étapes sont trop spécifiques nous ont veut que le bots ne fasses pas ça d'une maniére aussi linéare et prévisible, moi je veux qu'ils joue et prenne sont temps et découvre et s'adapte au monde en permettant de tout faire.
- Se balader, a pied, en bateau, en cheval etc... trouver des structures et les exploiter
- Faire des farm7
- Combattre ou esquiver des monstres
- Amméliorer sont stuff avec table d'enchantment, enclume, trims
- Faire ça maison, poser sont lit
- Cultiver, Manger...
- Aller en grottes, trouver des resources.

L'objectifs c'est qu'ils joues de la maniéres dont il a envie, ont lui fourni simplement la connection entre lui et le jeu, ont lui donne le contextes du jeu et doit prendre des décisions, sont objectifs final c'est le dragon mais ont ne doit pas lui donner un chemin a suivre spécifiques. Ont doit biensur l'aider car je penses qu'ils ne sait pas du tout comment faire.
Je connais pas du tout c'est capcités s'ils sait ce qu'est minecraft, ou les différente mécaniques du jeu.

## Le bute du projet
Essayer Jev, comprendre comment il fonctionne, comprendre comment l'utiliser, s'il a des connaisses globales sur le modne, les jeux vidéos, la culture POP etc..., comprendre comment l'utiliser, trouver des maniéres de l'utiliser.
Et surtout arriver a ce qu'ils joue de maniére autonomes et changer sont objectifs en cours de routes, si je lui dit de faire une maison il doit changer ses plans pour faire une maisons etc... donc qu'ils soit pilotables par moi en temps réel donc un goal définisable.

## Développement
Je ne sais pas encore comment l'utiliser donc je vais apprendre, peut être faire plusieurs instances avec des classifications différente, ou alors des niveau de classification donc des Jev en pyramide. Des Jev qui prenne des décisions plus précises en dessous et transmette a un jev qui regroupes plusieurs sous décision ?

Pour le controle du personnages, ont va utiliser un mods server qui va le controler, mais réellement promprement Path Finding, réel comportement d'un joueurs donc un vrai saut, un vrai jump, un vrai possage de blocs etc...

Ont va s'inspirer de Twitch Play et Studio que j'ai pu faire.
- Dans Twitchplay y'a une simulations des touches
- Dans Studio, y'a tout un module IA qui permet de donner du contextes.

Ont va surtout reprendre le projet de Studio, le bot doit avoir le contextes auditives donc les bruit avec la positions exactes de l'origine des bruit et quel bruit, et le contextes visuelle, les blocs qui voit, les informations.
Ou alors si pas possible ont va faire autrement, avec un envoie de contextes basés sur le serveur comme dans Studio.
- Donc envoyer l'infos sur un blocs précis
- Envoyer les coordonées des mobs visibles.
- Envoyer les craft pattern etc...
- Envoyer la poistion des items dans l'inventaire et ce que c'est
- Envoyer la position des blocs, l'état des blocs comme l'avancés d'un four...
- Envoyer le contextes visuelle.

Idéalement il doit être aussi autonomes qu'un humains et voir ce qy'ils voit a l'écran et pas d'avantage secret. Sauf pour l'auditives donc pour faire comme un humains qui connait la position a partir du son ont va envoyer l'origine du son, la coordinées et qui l'emet.
Idem les game event ont peut lui envoyer.

## Fonctionnement
Couches en 3 étapes :
- Le code observation du monde, mémoire, inventaire, calculs, craft, physique du joueur. Donc déterministe ou mathématique
- Le LLM (Le stratéges) Appelé rarement. Il reçoit un résumé de la partie.
- Jev (Les réfléxes) a chaque tick, il choisit parmi les primitives.  Il reçoit l'intention mais il n'a pas besoin de savoir que le but final est le dragon.

1. Le code déclenche le stratéges par des événement algorythmiques, avec max 1 a 2 apelle par minute.
2. Le LLM décide de la suite. Il répond en 5 à 30 secondes, pendant lesquelles Jev continue de jouer. mais le code doit vérifier que la décision a encore un sens à son arrivée
3. La mémoire Ni Jev ni le LLM ne se souviennent de rien entre deux appels. Il faut un journal structuré coordonnées de la base, du village, des grottes, ce qui a été tenté et échoué. Le LLM le lit, Jev n'en reçoit qu'un extrait pertinent.
4. Jev Permet le temps réel.
5. Ne pas laisser le LLM écrire les questions Jev. Questions fixes par système, le LLM ne remplit que des paramètres à valeurs fermées. On pourra assouplir plus tard.
6. Pas Jev partout, les LLM et Jev, ont des forces et faiblesses différente.

LLM sur GPT 6 Luna ou GPT 6 Terra. En medium sur l'abonnement mensuelle de Codex.