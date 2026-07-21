# Changelog

All notable changes to **Where Was I?** are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Everything below targets **Minecraft 1.21.1** on **NeoForge 21.1.x** and is **100% client-side**.

## [Unreleased]

### Added

- **Interactive timeline.** The journal (**J**) is now grouped into **collapsible
  sessions**, newest first — each header showing the session's time span, duration, main
  zone and a summary of what you did. Expanding a session draws a **vertical rail** of its
  events in chronological order; clicking any event reveals its coordinates and a
  **"Guide"** button that points the HUD arrow at it. "Today / Yesterday / date" day
  headings and per-event icons throughout.
- **Activity chapters (`SEGMENT`).** Every few minutes of play is condensed into one
  readable entry classifying the dominant activity — **mining / building / exploring /
  combat** — with the counts behind it. Idle stretches are skipped. Interval is
  configurable via `segmentMinutes` (default 5).
- **Biome discovery.** Logs a one-off event the first time you set foot in a biome the
  world has never journalled.
- **Per-action journal.** Detailed, coalesced capture of what you actually do:
  - **Blocks broken / placed**, grouped into runs (e.g. _"broke 24 stone"_).
  - **Interactions** with chests, furnaces, barrels, doors, buttons, levers, workstations
    and other container/menu blocks.
  - **Item moves** — picked up, dropped, and put in / taken from a storage container —
    detected from context-aware inventory diffing.
- **Situation report on logout.** Choosing **"Save and Quit to Title"** / **"Disconnect"**
  now opens a fill-in report (a recap of the session plus a free-text box) _before_ the
  game actually leaves; saving pins the note to the top of your next briefing. Toggle with
  `debriefOnLogout` (default on).
- **Logout position.** The briefing and timeline now surface where you last logged off,
  with the timeline's logout entry guidable like any other.
- **Mod icon** wired through `logoFile` in the mod metadata (in-game mod list) and the
  README headers.
- New configuration options: `segmentMinutes`, `briefingEveryJoin`, `debriefOnLogout`.

### Changed

- **The resume briefing now shows on every world join by default** (`briefingEveryJoin =
true`). The previous "only after N hours" behaviour still exists — set
  `briefingEveryJoin = false` and tune `briefingMinHoursSinceLast`.
- Session summaries carry a per-type breakdown (blocks mined / placed, items crafted, mobs
  killed) surfaced in the briefing and session headers.

## [1.0.0] — 2026-07-21

The first release: an automatic, effort-free play journal that greets you with a recap
when you come back.

### Added

- **Automatic capture.** Per-world / per-server sessions, position + dimension sampling,
  inventory diffs (first-ever acquisitions and big hauls), deaths (with cause), dimension
  changes and advancements — all interval-gated, no per-tick allocation.
- **Vanilla-stats trick.** Periodically issues the same `REQUEST_STATS` the vanilla
  Statistics screen uses and diffs the reply, so blocks mined, mobs killed, deaths and
  distance travelled are exact on singleplayer, vanilla servers and modpacks alike.
- **Resume briefing** on join: last session's age & length, main area with live
  distance/direction, top events by importance, deaths with coordinates, and the pinned
  note — with **"Guide"** buttons driving a home-grown HUD arrow (no minimap). Re-openable
  with **B**.
- **Zones.** Auto-detected hot spots (time spent in a 64×64 cell) offer to be named via a
  toast, plus a two-corner **claim** flow (**K**). Rename / merge / delete from the zones
  screen; named zones tag nearby events and power the briefing's "main area".
- **Timeline journal** (**J**) of recorded events with type and zone filters.
- **Notes.** Add a note tied to your position (**N**), pin one at a time, delete;
  the pinned note rides along in the briefing and (optionally) on the HUD.
- **100% client-side.** `@Mod(dist = Dist.CLIENT)` + `displayTest = IGNORE_ALL_VERSION`:
  works on any server (vanilla or modded) with nothing installed server-side, adds no
  blocks / items / recipes / commands, and stores everything as plain JSON under
  `.minecraft/wherewasi/`.

### Changed

- License changed from MIT to **All Rights Reserved**.

[Unreleased]: https://github.com/NokhXyr/WhereWasI/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/NokhXyr/WhereWasI/releases/tag/v1.0.0
