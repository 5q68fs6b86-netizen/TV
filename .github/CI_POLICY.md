# CI policy — heavy work on GitHub Actions only

This VPS is **2 cores / 3.8G RAM**. Do not run heavy builds or APK unpacking locally.

## Must use Actions

| Work | Workflow |
|------|----------|
| Phase 0 gold APK/AAR audit (unpack, nm, javap) | `audit-fongmi-gold-apk.yml` |
| Native libmpv + AAR | `build-mpv-lib.yml` |
| Signed test APK | `build-test-apk.yml` |
| Release APK | `build-release.yml` / `main.yml` |
| Compile gate | `compile-check.yml` |

## Allowed on VPS

- Edit Java/Kotlin/Gradle/YAML
- `git`, small `rg`
- `gh workflow run` / `gh run watch` / download **report** artifacts
- Optional: `./gradlew :app:compileLeanbackArm64_v8aDebug*` with `--max-workers=2`

## Forbidden on VPS

- NDK / `buildall.sh` / full libmpv builds
- Multi-flavor `assemble*Release`
- Downloading full FongMi APKs to unzip locally
- Docker heavy APK diff jobs

## Phase 0 trigger

`FongMi/TV` has **no** GitHub Release assets. Official APKs live in
[FongMi/Release](https://github.com/FongMi/Release) `apk/` (see `.github/mpv/GOLD_SOURCE.md`).

```bash
# Canonical gold (default source_url in the workflow)
gh workflow run audit-fongmi-gold-apk.yml \
  --ref feat/mpv-hot-decode-dolby-hdr \
  -f source_url='https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback-arm64_v8a.apk'

# Optional: another public release repo with assets
gh workflow run audit-fongmi-gold-apk.yml \
  -f source_url='' \
  -f source_repo=owner/repo \
  -f asset_name_regex='.*leanback.*arm64.*\\.apk$'
```

Local handoff notes (gitignored under `/docs` on this machine):

- `/root/TV/docs/HANDOFF_FONGMI_MPV.md`
- `/root/TV/docs/FONGMI_MPV_REDO_PLAN.md`
- `/root/TV/docs/CI_ONLY_NOW.md`
- `/root/TV/.github/mpv/GOLD_SOURCE.md` (tracked)
