<!-- =================================================================== -->
<!-- SHORT DESCRIPTION  ·  Modrinth "Summary" field  ·  max 255 chars    -->
<!-- =================================================================== -->

Never ask "where was I?" again. This client-side mod auto-journals your playthrough — sessions, deaths, milestones, zones — and greets you with a recap when you log back in. Works on any server, nothing to install server-side.

<!-- =================================================================== -->
<!-- LONG DESCRIPTION  ·  Modrinth "Description" (Markdown)              -->
<!-- =================================================================== -->

# Where Was I?
### The mod that answers the question.

You close Minecraft on a Tuesday. Life happens. Three weeks later you load the world
back up, spawn in, look around… and there it is, the universal thought:

> **“…where was I, again?”**

Maybe it's a **400-mod kitchen-sink pack** and you can't remember which of your six
half-built machines you were wiring up. Maybe it's a quiet **vanilla+ singleplayer
world** and you have no idea which direction your base is. Either way, the question is
the same — and **Where Was I?** is the answer.

It keeps an **automatic journal** of your playthrough — zero effort from you — and
shows you a short **resume briefing** the next time you log in. It was taking notes the
whole time, so you don't have to.

---

## 📋 A briefing when you return
A few seconds after you rejoin a world — once, and only after a real break — you get a
recap: when you last played and for how long, your **main base** (with live distance +
direction to it), your session's **biggest moments**, where you **died**, and any note
you **pinned**. One click on **Guide** puts an arrow on your HUD pointing you home. No
minimap needed.

## 🛰️ Capture that just happens
No buttons, no diary-keeping. It quietly tracks sessions, samples where you spend your
time, watches your inventory for first-time item pickups and big hauls, and logs
deaths, dimension changes and advancements — each scored by importance, so your first
diamond outranks your hundredth stack of cobble.

**The clever bit:** every couple of minutes it silently fires the same request the
vanilla Statistics screen uses and reads the reply. That means **blocks mined, mobs
killed, deaths and distance** are the server's own exact numbers — just as accurate in
singleplayer, on a vanilla server, or in a massive modpack.

## 🗺️ Zones for your build sites
Hang around one spot long enough and it discreetly offers to **name the place** — Base,
Iron Farm, That Cave. Named zones organize your journal and power the briefing's "main
area." Rename, merge and delete them whenever.

## 📖 A timeline you'll actually read
Press **J** for the full journal: every event, newest first, grouped by day, with item
icons and filters by type and zone.

## 📌 Pinned notes
Press **N** to drop a quick note where you're standing. Pin one and it follows you into
the briefing and onto your HUD — *"come back with obsidian,"* *"portal is NORTH."*

---

## 🌐 100% client-side — works everywhere
- **Any server.** Vanilla or modded, it just works; servers never reject you for it.
- **Nothing to install server-side.** Your admin does nothing; servers can't even tell.
- **No blocks, items, recipes or commands.** It only reads what already happens.
- **Safe to remove** and **private** — everything is plain JSON on your own disk, sent
  nowhere.

---

## 📸 Screenshots
<!-- TODO: upload these to the Modrinth gallery and/or embed them here -->
<!-- 1) The resume briefing (key B) -->
<!-- 2) The journal / timeline (key J) -->
<!-- 3) The HUD guide arrow + a pinned note -->
*Gallery coming soon.*

---

## ❓ Quick FAQ
**On a server?** Yes — client-side, any server, nothing to install for them.
**Performance?** Interval-based capture, no per-tick cost. Fine in huge packs.
**Changes gameplay?** No blocks/items/commands. Read-only.
**My data?** Local JSON under `.minecraft/wherewasi/`. Delete it to wipe it.

---

## 🔗 Links
- **Source & issues:** https://github.com/lhybride59/WhereWasI
- Requires **NeoForge** for **Minecraft 1.21.1**. License: All Rights Reserved.

<!-- =================================================================== -->
<!-- PUBLISHING METADATA (not part of the description text)              -->
<!--                                                                     -->
<!-- Modrinth categories: Utility, Game Mechanics                        -->
<!-- Environment:  Client = Required   ·   Server = Unsupported          -->
<!-- Loaders: NeoForge   ·   Game version: 1.21.1                        -->
<!-- =================================================================== -->
