# Releasing

Publishing is automated by [`.github/workflows/publish.yml`](../.github/workflows/publish.yml),
which runs the [mc-publish](https://github.com/Kir-Antipov/mc-publish) action.
**Creating a GitHub Release publishes the mod to Modrinth, CurseForge and the GitHub
Release in one shot** — the Release body becomes the changelog on both platforms.

---

## One-time setup

### 1. Create the project pages
Create the mod's project on **Modrinth** and **CurseForge** first — nothing can publish to
a project that doesn't exist yet. Upload the icon (`src/main/resources/icon.png`) on each.

### 2. Add the four repository secrets
Repo → **Settings → Secrets and variables → Actions → New repository secret**.
`GITHUB_TOKEN` is provided automatically by Actions — you do **not** create it.

| Secret | What it is | Where to get it |
|---|---|---|
| `MODRINTH_ID` | The Modrinth project id (or slug) | Modrinth project page → **Settings → General**, or the `<slug>` in `modrinth.com/mod/<slug>` |
| `MODRINTH_TOKEN` | A Modrinth Personal Access Token with the **Create versions** scope | modrinth.com → **avatar → Settings → PATs → Create a PAT**. Tick **Create versions** (plus **Read**). |
| `CURSEFORGE_ID` | The numeric CurseForge project id | CurseForge project page → right sidebar, **Project ID** |
| `CURSEFORGE_TOKEN` | A CurseForge API token | **[legacy.curseforge.com/account/api-tokens](https://legacy.curseforge.com/account/api-tokens)** → **API Tokens** → Generate |

> Tokens live only in GitHub Secrets — never commit them to the repo.

---

## Cutting a release

1. **Bump the version.** Edit `mod_version` in [`gradle.properties`](../gradle.properties)
   (the `neoforge.mods.toml` picks it up via `${mod_version}`). Commit and push to the
   **default branch**.
2. **Draft a GitHub Release.** Repo → **Releases → Draft a new release**:
   - **Tag**: `vX.Y.Z`, matching the version you just set (e.g. `v1.0.1`). Let GitHub create
     the tag on publish.
   - **Title**: a short name — it becomes the version's display name on the platforms.
   - **Description**: your release notes — this body becomes the **changelog** shown on
     Modrinth and CurseForge.
3. **Publish** the release. The `Publish` workflow starts automatically: it builds the jar
   and uploads it (with your notes as the changelog) to Modrinth, CurseForge and the GitHub
   Release. Follow it under the **Actions** tab.

Game version (1.21.1), loader (NeoForge) and dependencies are **auto-detected** from the
mod's metadata, so you don't set them per release. If a build ever lands on the wrong game
version, uncomment `game-versions` / `loaders` in the workflow.

### Manual run
The workflow also exposes a **Run workflow** button (Actions → Publish → Run workflow) for
re-running a build. A manual run has no release context, so it uses the jar's own version
and ships no changelog — use the Release flow for real publishes.

---

## Nothing publishes until…
- the two **project pages exist** on Modrinth and CurseForge, **and**
- the **four secrets** above are set, **and**
- the workflow file is on the **default branch** (so the Release trigger can find it).
