<!-- 🇬🇧 English version · 🇫🇷 [Version française](README_FR.md) -->
🇬🇧 **English version** · 🇫🇷 [Version française](README_FR.md)

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
A few seconds after you join a world — **once**, and only if your last session ended
a while ago (6 hours by default) — a briefing pops up:

- when your **last session** was and how long it ran,
- your **main area**, with the **live distance and direction** to it from where you
  are right now,
- the **top 3–5 events** of that session, with item icons,
- **deaths**, with coordinates,
- your **pinned note**, if you left one.

The zone and death entries have a **“Guide”** button that lights up a lightweight
on-screen arrow pointing you back — no minimap required. You can re-open the briefing
anytime with **B**.

### 🛰️ Automatic capture (the good part)
No buttons to press. While you play, the mod:

- tracks **sessions** (per world / per server, so journals never mix),
- samples **position + dimension** to learn where you spend your time,
- diffs your **inventory** to catch the first time you ever get an item, and big hauls,
- notes **deaths** (with cause), **dimension changes**, and **advancements**.

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
Press **J** for the full journal: every event, newest first, **grouped by day**, with
item icons and **filters** by event type and by zone. Smooth scrolling, no clutter.

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
| zones | `zoneThresholdMinutes` | 20 | minutes in a 64×64 cell before it offers to name a zone |
| briefing | `briefingEnabled` | true | show the briefing on join |
| briefing | `briefingMinHoursSinceLast` | 6 | only auto-show if the last session ended this long ago |
| briefing | `briefingDelaySeconds` | 3 | delay after joining before the briefing appears |
| hud | `hudPinnedNote` | true | draw the pinned note on the HUD |
| hud | `hudGuide` | true | draw the guide arrow on the HUD |
| hud | `hudCorner` | TOP_LEFT | which corner the HUD widgets attach to |

> Want the briefing every single time for testing? Set `briefingMinHoursSinceLast = 0`.

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

## 📜 License

**All Rights Reserved** © 2026 NokhXyr. You may download the mod and play with it,
but it may **not** be copied, modified, forked, decompiled or redistributed without
the author's written permission. See [LICENSE](LICENSE).
# WhereWasI
