<!-- 🇬🇧 English version · 🇫🇷 [Version française](README_FR.md) -->
🇬🇧 **English version** · 🇫🇷 [Version française](README_FR.md)

<p align="center">
  <img src="src/main/resources/icon.png" width="160" alt="Where Was I? icon">
</p>

# Where Was I?

### The mod that answers the question.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.232-F16436)
![Environment](https://img.shields.io/badge/side-client--only-4C9AFF)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)
[![Modrinth](https://img.shields.io/badge/Modrinth-download-00AF5C?logo=modrinth&logoColor=white)](# "TODO: paste the Modrinth project URL here")
[![CurseForge](https://img.shields.io/badge/CurseForge-download-F16436?logo=curseforge&logoColor=white)](# "TODO: paste the CurseForge project URL here")

> You close Minecraft on a Tuesday. You come back three weeks later. You spawn in,
> look around, and the only thought in your head is: **“…where was I, again?”**

**Where Was I?** keeps an automatic journal of your playthrough — with **zero effort
from you** — and greets you with a short **resume briefing** the next time you log
in. What were you doing? Where's your base? What did you die to? It already knows,
because it was quietly taking notes the whole time.

It's built for the moment you *don't* remember: whether you're on a lightly-modded
vanilla+ world or buried in a 400-mod kitchen-sink modpack, the question is always
the same — and now there's an answer.

---

## The problem → the answer

Every other "notes" mod hands you a blank notepad and says *good luck, write it all
down yourself*. Nobody does. You're playing the game, not keeping a diary.

**Where Was I?** flips that around. The whole point is **automatic capture**: it
watches what you actually do and records the moments that matter, scored by
importance, so your first diamond outranks your hundredth stack of cobblestone. When
you return, it hands you the recap you never wrote.

---

## ✨ Features

### 📋 Resume briefing
A few seconds after you join a world — **every time**, by default — a briefing pops up:

- when your **last session** was and how long it ran,
- where you **logged off** last time (coordinates),
- your **main area**, with the **live distance and direction** to it from where you
  are right now,
- the **top 3–5 events** of that session, with item icons,
- **deaths**, with coordinates,
- your **pinned note**, if you left one.

The zone and death entries have a **“Guide”** button that lights up a lightweight
on-screen arrow pointing you back — no minimap required. You can re-open the briefing
anytime with **B**.

### 🚪 Situation report on logout
Hit **“Save and Quit to Title”** or **“Disconnect”** and the mod steps in first with a
short **situation report**: a recap of the session you just played, plus a text box to
jot down where you're at and what's next. Save it — it's pinned to the top of your next
briefing — and *then* the game leaves as usual. Toggle it with `debriefOnLogout`.

### 🛰️ Automatic capture (the good part)
No buttons to press. While you play, the mod:

- tracks **sessions** (per world / per server, so journals never mix),
- samples **position + dimension** to learn where you spend your time,
- diffs your **inventory** to catch the first time you ever get an item, and big hauls,
- notes **deaths** (with cause), **dimension changes**, and **advancements**,
- logs your **actions in detail** — blocks broken and placed (grouped into runs, e.g.
  *"broke 24 stone"*), **interactions** (chests, furnaces, doors, workstations…), and
  **item moves** (picked up, dropped, put in / taken from a container),
- discovers each **new biome** you set foot in,
- splits every stretch of play into **activity chapters** — mining / building /
  exploring / combat — every few minutes.

**The vanilla-stats trick.** Here's how the numbers stay *exact* on any server: every
couple of minutes the mod silently sends the very same request the vanilla
**Statistics** screen uses (`REQUEST_STATS`) and reads the reply the server sends back.
Diffing that gives precise **blocks mined, mobs killed, deaths and distance travelled**
— straight from the server's own bookkeeping, so it's just as accurate in singleplayer,
on a vanilla server, or in a giant modpack.

### 🗺️ Zones (your build sites)
Spend enough time in one spot (20 minutes in a 64×64 area, by default) and the mod
offers — via a discreet toast — to **name that place**. Call it *Base*, *Iron Farm*,
*That Cave*. Named zones tag nearby events and power the briefing's "main area". Manage
them (rename / merge / delete) from the zones screen.

### 📖 Timeline
Press **J** for the full journal — **grouped into collapsible sessions**, newest first.
Each session header shows its span, duration, main zone and a summary of what you did;
expand it to reveal a **vertical rail** of that session's events in order. Click any event
to open its coordinates and a **“Guide”** button that points the HUD arrow straight at it.
Filter the whole history by event type or by zone.

### 📌 Pinned notes
Press **N** to jot a quick note tied to where you're standing. Pin one (only one at a
time) and it rides along in your **briefing** and, optionally, on your **HUD** — perfect
for *"come back with more obsidian"* or *"portal is NORTH."*

---

## 🌐 100% client-side

- **Works on every server.** Singleplayer, vanilla multiplayer, modded — all the same.
  Thanks to `displayTest = IGNORE_ALL_VERSION`, servers never reject you for having it.
- **Nothing to install server-side.** Servers don't need the mod (and can't tell you
  have it). Your admin doesn't have to do anything.
- **Adds no blocks, items, recipes or commands.** It only reads what's already happening.
- **Safe to remove.** It's `@Mod(dist = Dist.CLIENT)` — the code never even loads on a
  dedicated server, and uninstalling just stops the journaling; your worlds are untouched.
- **Your data stays yours.** Everything is plain JSON on your disk under
  `.minecraft/wherewasi/`. Nothing is sent anywhere.

---

## 📸 Screenshots

> _Screenshots coming soon — drop the images in `docs/screenshots/` and they'll appear here._

<!-- 1) The resume briefing (press B) -->
![Resume briefing](docs/screenshots/briefing.png)

<!-- 2) The journal / timeline (press J) -->
![Timeline](docs/screenshots/timeline.png)

<!-- 3) The HUD guide arrow + pinned note -->
![HUD guide](docs/screenshots/hud.png)

---

## 📥 Installation

1. Install **[NeoForge 21.1.x](https://neoforged.net/)** for **Minecraft 1.21.1**.
2. Download **Where Was I?** from Modrinth or CurseForge (links above), or grab the
   `.jar` from the [Releases](https://github.com/lhybride59/WhereWasI/releases) page.
3. Drop the `.jar` into your `mods/` folder.
4. Launch the game. That's it — it starts journaling on its own.

Requires Java 21 (bundled with modern launchers).

---

## ⌨️ Default keybinds

All keys are rebindable in **Options → Controls → Where Was I?**

| Action | Default |
|---|---|
| Open the journal / timeline | **J** |
| Add a note | **N** |
| Show the resume briefing | **B** |
| Mark a zone corner (claim) | **K** |
| Manage zones | *unbound* |
| Clear the HUD guide arrow | *unbound* |

---

## ⚙️ Configuration

Client config lives at `config/wherewasi-client.toml` (editable in-game via mod-config
screens too). Defaults:

| Section | Option | Default | What it does |
|---|---|---|---|
| capture | `positionSampleSeconds` | 15 | how often position / zone-time is sampled |
| capture | `statsPollSeconds` | 120 | how often the silent vanilla-stats request is sent |
| capture | `inventoryPollSeconds` | 10 | how often the inventory is diffed |
| capture | `bulkAcquireThreshold` | 64 | items gained at once before logging a "big haul" |
| capture | `segmentMinutes` | 5 | minutes between activity-chapter summaries |
| zones | `zoneThresholdMinutes` | 20 | minutes in a 64×64 cell before it offers to name a zone |
| briefing | `briefingEnabled` | true | show the briefing on join |
| briefing | `briefingEveryJoin` | true | show it on every join (when false, the delay below applies) |
| briefing | `briefingMinHoursSinceLast` | 6 | when `briefingEveryJoin` is off: only auto-show if the last session ended this long ago |
| briefing | `briefingDelaySeconds` | 3 | delay after joining before the briefing appears |
| briefing | `debriefOnLogout` | true | open a fill-in situation report when you leave a world |
| hud | `hudPinnedNote` | true | draw the pinned note on the HUD |
| hud | `hudGuide` | true | draw the guide arrow on the HUD |
| hud | `hudCorner` | TOP_LEFT | which corner the HUD widgets attach to |

> The briefing shows on **every** join by default (`briefingEveryJoin = true`). Prefer it
> only after a long break? Set `briefingEveryJoin = false` and tune `briefingMinHoursSinceLast`.

---

## ❓ FAQ

**Does it work on servers?**
Yes — it's client-side. Any server, vanilla or modded, no install needed on their end.

**Will it hurt my performance?**
No. Capture is interval-based with no per-tick allocation; there's nothing running on
the hot path. It's happy in a 400-mod pack.

**Does it change gameplay / add items?**
No blocks, no items, no recipes, no commands. It only reads existing data.

**Is my data uploaded anywhere?**
Never. It's local JSON under `.minecraft/wherewasi/<world>/`. Delete the folder to wipe it.

**Why does the first session on a world not show much?**
Stats are measured from a baseline taken a few seconds after you join, and the first
poll lands ~2 minutes in — so give it a session to warm up.

---

## ⚠️ Known limitations

- **Craft vs. pickup is approximate.** "First acquisition" comes from inventory diffs,
  so an item reads the same whether you crafted, mined, traded for or found it. The
  *exact* per-action counts (mined / crafted / killed) come from the vanilla-stats diff,
  which is authoritative — but the two views aren't cross-correlated per event.
- **Per-action logging is client-side and approximate.** Blocks broken are counted when
  you *start* breaking them, placements when you right-click, and item moves (pickup /
  drop / store / take) come from inventory diffs — so those entries are grouped estimates,
  not a server-authoritative ledger. Actions behind another mod's custom screen may be missed.
- **Stats lag by up to one poll** (~2 min) and are measured from a post-join baseline, so
  the very first seconds of a session aren't attributed.
- **Milestones are per-session** ("first diamond *this session*"), not first-ever across a
  world's whole history.
- **Advancement capture** relies on one reflected vanilla field; if a future MC release
  renames it, that single feature disables itself (everything else keeps working).
- **Zone dwell time** is sampled (~15 s granularity) and capped per sample so an AFK gap
  doesn't dump a huge block of time into one cell.

---

## 🧭 Roadmap

- A public `ActivityDetector` extension point (already stubbed in `compat/`) with
  first-party bridges: **FTB Quests** (quest completions) and **minimaps** (waypoints).
- An optional **decorative server-side companion** so servers can surface a player's own
  recap — still zero gameplay impact.
- Multi-line notes and note editing.
- Smarter zone clustering (auto-merge adjacent hot cells).

---

## 🚀 Releasing

Maintainers: builds are automated with GitHub Actions (no secrets needed — it uses the
built-in `GITHUB_TOKEN`). To cut a release, bump `mod_version` in `gradle.properties`,
commit, then push an **annotated tag** (its message becomes the release notes):

```bash
git tag -a vX.Y.Z -m "Release notes…"
git push origin vX.Y.Z
```

The `Release` workflow builds the mod and attaches `wherewasi-1.21.1-X.Y.Z.jar` to a new
GitHub Release for that tag.

---

## 📜 License

**All Rights Reserved** © 2026 NokhXyr. In short: **download & play** it (from
official sources), **put it in modpacks** (with credit), and **make videos/streams**
about it — but **don't** re-upload it, ship modified versions, or reuse its code or
assets. Full terms in [LICENSE](LICENSE).

See [CHANGELOG.md](CHANGELOG.md) for the full history.
