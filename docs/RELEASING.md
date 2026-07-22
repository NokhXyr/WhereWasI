# Releasing

Builds are automated by [`.github/workflows/release.yml`](../.github/workflows/release.yml).
**Pushing a `vX.Y.Z` tag builds the mod on GitHub and attaches the jar to a matching
GitHub Release** — automatically, with **no secrets and no external accounts**
(it uses the built-in `GITHUB_TOKEN`).

## Cutting a release

1. **Bump the version.** Edit `mod_version` in [`gradle.properties`](../gradle.properties)
   (the `neoforge.mods.toml` and the jar name read it). Commit and push to the
   **default branch**.
2. **Tag it** — the annotated tag's message becomes the GitHub Release notes:
   ```bash
   git tag -a vX.Y.Z -m "What changed in this version…"
   git push origin vX.Y.Z
   ```
   Use the same version you set (e.g. `v1.0.1`). The **annotated tag message is the
   changelog** shown on the release.
3. **Done.** The `Release` workflow builds `wherewasi-1.21.1-X.Y.Z.jar` and publishes a
   GitHub Release for the tag with the jar attached. Follow it under the **Actions** tab.

> Prefer writing the notes in the GitHub UI? Push a lightweight tag (`git tag vX.Y.Z`) —
> the workflow still creates the release (notes default to *"See commit history."*), then
> edit the release body on GitHub afterwards.

## Build locally
`./gradlew build` produces the jar under `build/libs/` (`wherewasi-1.21.1-1.0.0.jar`).

## Manual run
Actions → **Release** → **Run workflow**. Run it **from a tag** to (re)publish that
release; run it from a branch and it only builds (a CI smoke test), attaching nothing.

## Notes
- **No secrets required** — the workflow authenticates with the automatic `GITHUB_TOKEN`.
- The workflow must live on the **default branch** for a tag push to trigger it.
