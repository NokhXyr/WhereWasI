<!-- 🇫🇷 Version française · 🇬🇧 English version -->
🇫🇷 **Version française** · 🇬🇧 [English version](README.md)

# Where Was I?

### Le mod qui répond à la question.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.232-F16436)
![Environnement](https://img.shields.io/badge/côté-client_uniquement-4C9AFF)
![Licence](https://img.shields.io/badge/Licence-Tous%20droits%20r%C3%A9serv%C3%A9s-red)
[![Modrinth](https://img.shields.io/badge/Modrinth-télécharger-00AF5C?logo=modrinth&logoColor=white)](# "À FAIRE : coller l'URL du projet Modrinth")
[![CurseForge](https://img.shields.io/badge/CurseForge-télécharger-F16436?logo=curseforge&logoColor=white)](# "À FAIRE : coller l'URL du projet CurseForge")

> Tu fermes Minecraft un mardi soir. Tu y reviens trois semaines plus tard. Tu
> réapparais, tu regardes autour de toi, et une seule pensée te traverse :
> **« … j'en étais où, déjà ? »**

**Where Was I?** tient tout seul le journal de ta partie — **sans le moindre effort de
ta part** — et t'accueille avec un petit **briefing de reprise** à ta prochaine
connexion. Tu faisais quoi ? Elle est où, ta base ? Tu es mort de quoi ? Il le sait
déjà, parce qu'il prenait des notes en silence pendant tout ce temps.

Il est fait exactement pour le moment où *tu ne te souviens plus* : que tu sois sur un
monde vanilla à peine modé ou enseveli sous un modpack de 400 mods, la question est
toujours la même — sauf qu'il y a maintenant une réponse.

---

## Le problème → la réponse

Tous les autres mods de « notes » te tendent un carnet vierge en te disant *bon
courage, note tout toi-même*. Personne ne le fait. Tu joues, tu ne tiens pas un journal
intime.

**Where Was I?** prend le problème à l'envers. Tout repose sur la **capture
automatique** : il observe ce que tu fais vraiment et enregistre les moments qui
comptent, notés par ordre d'importance — ton premier diamant passe donc avant ta
centième pile de cobble. À ton retour, il te tend le récap que tu n'as jamais écrit.

---

## ✨ Fonctionnalités

### 📋 Briefing de reprise
Quelques secondes après avoir rejoint un monde — **une seule fois**, et seulement si ta
dernière session remonte à un moment (6 heures par défaut) — un briefing s'affiche :

- quand remonte ta **dernière session** et combien de temps elle a duré,
- ta **zone principale**, avec la **distance et la direction en temps réel** depuis
  l'endroit où tu te trouves,
- les **3 à 5 événements majeurs** de cette session, avec les icônes des objets,
- les **morts**, avec leurs coordonnées,
- ta **note épinglée**, si tu en as laissé une.

Les entrées de zone et de mort ont un bouton **« Guider »** qui allume une petite flèche
à l'écran pour t'y ramener — sans aucune minimap. Tu peux rouvrir le briefing quand tu
veux avec **B**.

### 🛰️ Capture automatique (le cœur du mod)
Aucun bouton à presser. Pendant que tu joues, le mod :

- suit tes **sessions** (par monde / par serveur, les journaux ne se mélangent jamais),
- échantillonne ta **position + dimension** pour savoir où tu passes ton temps,
- compare ton **inventaire** pour détecter la première fois que tu obtiens un objet, et
  les gros gains,
- relève les **morts** (avec la cause), les **changements de dimension** et les
  **progrès** débloqués.

**L'astuce des stats vanilla.** Voilà comment les chiffres restent *exacts* sur
n'importe quel serveur : toutes les deux minutes environ, le mod envoie discrètement la
même requête que l'écran **Statistiques** du jeu (`REQUEST_STATS`) et lit la réponse
renvoyée par le serveur. En comparant, on obtient le nombre précis de **blocs minés, de
mobs tués, de morts et la distance parcourue** — directement depuis la comptabilité du
serveur. C'est donc aussi juste en solo, sur un serveur vanilla ou dans un énorme
modpack.

### 🗺️ Zones (tes chantiers)
Passe assez de temps au même endroit (20 minutes dans une zone de 64×64 par défaut) et
le mod te propose — via un toast discret — de **nommer ce lieu**. Appelle-le *Base*,
*Ferme à fer*, *Cette grotte*. Les zones nommées taguent les événements alentour et
alimentent la « zone principale » du briefing. Tu les gères (renommer / fusionner /
supprimer) depuis l'écran des zones.

### 📖 Timeline
Appuie sur **J** pour le journal complet : tous les événements, du plus récent au plus
ancien, **regroupés par jour**, avec les icônes des objets et des **filtres** par type
d'événement et par zone. Défilement fluide, zéro fouillis.

### 📌 Notes épinglées
Appuie sur **N** pour griffonner une note rapide liée à l'endroit où tu te trouves.
Épingles-en une (une seule à la fois) et elle te suit dans le **briefing** et, si tu
veux, sur ton **HUD** — parfait pour *« reviens avec plus d'obsidienne »* ou *« le
portail est au NORD »*.

---

## 🌐 100% côté client

- **Fonctionne sur tous les serveurs.** Solo, multi vanilla, moddé — c'est pareil. Grâce
  à `displayTest = IGNORE_ALL_VERSION`, aucun serveur ne te refuse parce que tu l'as.
- **Rien à installer côté serveur.** Les serveurs n'ont pas besoin du mod (et ne peuvent
  pas savoir que tu l'as). Ton admin n'a rien à faire.
- **N'ajoute aucun bloc, objet, recette ou commande.** Il ne fait que lire ce qui se
  passe déjà.
- **Désinstallable sans risque.** Il est en `@Mod(dist = Dist.CLIENT)` — le code ne se
  charge même pas sur un serveur dédié, et le désinstaller arrête simplement le
  journal ; tes mondes ne sont pas touchés.
- **Tes données restent à toi.** Tout est en JSON brut sur ton disque, dans
  `.minecraft/wherewasi/`. Rien n'est envoyé nulle part.

---

## 📸 Captures d'écran

> _Captures à venir — dépose les images dans `docs/screenshots/` et elles apparaîtront ici._

<!-- 1) Le briefing de reprise (touche B) -->
![Briefing de reprise](docs/screenshots/briefing.png)

<!-- 2) Le journal / la timeline (touche J) -->
![Timeline](docs/screenshots/timeline.png)

<!-- 3) La flèche de guidage HUD + la note épinglée -->
![HUD](docs/screenshots/hud.png)

---

## 📥 Installation

1. Installe **[NeoForge 21.1.x](https://neoforged.net/)** pour **Minecraft 1.21.1**.
2. Télécharge **Where Was I?** sur Modrinth ou CurseForge (liens en haut), ou récupère
   le `.jar` depuis la page [Releases](https://github.com/lhybride59/WhereWasI/releases).
3. Glisse le `.jar` dans ton dossier `mods/`.
4. Lance le jeu. C'est tout — il se met à journaliser tout seul.

Nécessite Java 21 (fourni par les launchers modernes).

---

## ⌨️ Touches par défaut

Toutes les touches sont réassignables dans **Options → Commandes → Where Was I?**

| Action | Défaut |
|---|---|
| Ouvrir le journal / la timeline | **J** |
| Ajouter une note | **N** |
| Afficher le briefing de reprise | **B** |
| Gérer les zones | *non assignée* |
| Effacer la flèche de guidage | *non assignée* |

---

## ⚙️ Configuration

La config client se trouve dans `config/wherewasi-client.toml` (modifiable en jeu via les
écrans de config aussi). Valeurs par défaut :

| Section | Option | Défaut | Rôle |
|---|---|---|---|
| capture | `positionSampleSeconds` | 15 | fréquence d'échantillonnage position / temps de zone |
| capture | `statsPollSeconds` | 120 | fréquence de la requête de stats vanilla silencieuse |
| capture | `inventoryPollSeconds` | 10 | fréquence de comparaison de l'inventaire |
| capture | `bulkAcquireThreshold` | 64 | quantité obtenue d'un coup avant de logger un « gros gain » |
| zones | `zoneThresholdMinutes` | 20 | minutes dans une cellule 64×64 avant de proposer une zone |
| briefing | `briefingEnabled` | true | afficher le briefing à la connexion |
| briefing | `briefingMinHoursSinceLast` | 6 | ne l'afficher que si la dernière session remonte à autant d'heures |
| briefing | `briefingDelaySeconds` | 3 | délai après la connexion avant l'apparition du briefing |
| hud | `hudPinnedNote` | true | afficher la note épinglée sur le HUD |
| hud | `hudGuide` | true | afficher la flèche de guidage sur le HUD |
| hud | `hudCorner` | TOP_LEFT | dans quel coin fixer les widgets du HUD |

> Tu veux le briefing à chaque fois pour tester ? Mets `briefingMinHoursSinceLast = 0`.

---

## ❓ FAQ

**Ça marche sur les serveurs ?**
Oui — c'est côté client. N'importe quel serveur, vanilla ou moddé, sans rien installer de
leur côté.

**Ça va plomber mes perfs ?**
Non. La capture est basée sur des intervalles, sans allocation à chaque tick ; rien ne
tourne dans le chemin critique. Il est à l'aise dans un pack de 400 mods.

**Ça change le gameplay / ça ajoute des objets ?**
Aucun bloc, aucun objet, aucune recette, aucune commande. Il ne fait que lire les
données existantes.

**Mes données sont envoyées quelque part ?**
Jamais. C'est du JSON local dans `.minecraft/wherewasi/<monde>/`. Supprime le dossier pour
tout effacer.

**Pourquoi la première session sur un monde n'affiche pas grand-chose ?**
Les stats sont mesurées à partir d'une base prise quelques secondes après ta connexion, et
le premier relevé tombe ~2 minutes plus tard — laisse-lui une session pour chauffer.

---

## ⚠️ Limites connues

- **L'attribution craft vs ramassage est approximative.** La « première acquisition »
  vient de la comparaison d'inventaire : un objet se lit pareil que tu l'aies crafté,
  miné, échangé ou ramassé. Les comptes *exacts* par action (miné / crafté / tué)
  viennent du diff des stats vanilla, qui fait foi — mais les deux vues ne sont pas
  corrélées événement par événement.
- **Les stats ont jusqu'à un relevé de retard** (~2 min) et partent d'une base prise après
  la connexion : les toutes premières secondes d'une session ne sont donc pas attribuées.
- **Les jalons sont par session** (« premier diamant *de cette session* »), pas les tout
  premiers de l'histoire du monde.
- **La capture des progrès** repose sur un champ vanilla lu par réflexion ; si une future
  version de MC le renomme, cette seule fonction se désactive (tout le reste continue).
- **Le temps passé en zone** est échantillonné (~15 s de granularité) et plafonné par
  échantillon pour qu'une pause AFK ne déverse pas un énorme bloc de temps dans une cellule.

---

## 🧭 Feuille de route

- Un point d'extension public `ActivityDetector` (déjà ébauché dans `compat/`) avec des
  ponts maison : **FTB Quests** (quêtes terminées) et **minimaps** (waypoints).
- Un **compagnon décoratif côté serveur** optionnel pour que les serveurs puissent
  afficher le récap d'un joueur — toujours sans impact sur le gameplay.
- Notes multi-lignes et édition de notes.
- Clustering de zones plus malin (fusion automatique des cellules chaudes adjacentes).

---

## 📜 Licence

**Tous droits réservés** © 2026 NokhXyr. Tu peux télécharger le mod et y jouer, mais
il ne peut **pas** être copié, modifié, forké, décompilé ni redistribué sans
l'autorisation écrite de l'auteur. Voir [LICENSE](LICENSE).
