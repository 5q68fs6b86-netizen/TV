# Phase 0 gold pin — 2026-07-23

## Outcome summary

| Layer | Status | Notes |
|------|--------|--------|
| Official APK URL | **PINNED** | `FongMi/Release@fongmi` leanback-arm64 |
| `libmpv.so` / `libplayer.so` | **PINNED** | present arm64; JNI listed below |
| `is.xyz.mpv.MPVLib` | **PINNED** | package kept; methods match `libplayer` JNI |
| `androidx.media3.mpvplayer` | **R8-obfuscated in release** | package path **absent** in DEX; only keep-name `toggleGeneralStats` expected |
| App contract `MpvUtil` / `MpvPlayerEngine` | **PINNED from source** | `FongMi/TV@fongmi` via GitHub API |

**Phase 1 blocker (Java module):** cannot vendor clean `androidx.media3.mpvplayer` sources from the official **release** APK alone. Need one of: private `lib-*.aar`, debug/unminified APK, R8 mapping, or internal media mpvplayer source.

Native gold is sufficient to rebuild `libplayer`/`libmpv` acceptance line.

## Official package

```
source_url = https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback-arm64_v8a.apk
meta_json  = https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback.json
versionName = 5.5.6
versionCode = 556
apk_sha256  = 3d915e996cb10659c3840bf4c5e1b67edc85fda309529cbf398cd461507b89c3
audit_run   = https://github.com/5q68fs6b86-netizen/TV/actions/runs/30016893103
```

`FongMi/TV` GitHub Releases: **empty** (do not use `source_repo=FongMi/TV`).

## Native pin (arm64-v8a)

From audit artifact `api/libplayer.nm.txt` / `native-symbols.md`:

```
libmpv.so    sha256 = eec7ec1ee5aaf50a13310e633c498ff00b851a046a8940cc146ad2c0f715e8c3
libplayer.so sha256 = 8deb4df4005d9dede8fdac93b44dd3596efe7e3215f98a968e7ff7a421808be1
libplayer BuildID   = 37df6a831dfab5295539bcb92fe16f786859bee9
```

`libplayer.so` exports (`Java_is_xyz_mpv_*`):

```
create, init, destroy, command
attachSurface, replaceSurface, detachSurface
setOptionString
getPropertyInt, getPropertyDouble, getPropertyBoolean, getPropertyString, getPropertyByteArray
setPropertyInt, setPropertyDouble, setPropertyBoolean, setPropertyString
observeProperty
grabThumbnail
```

**Not present** (vs self-hosted v0.0.3 extended JNI): `getPropertyNode`, `commandNode`, `grabThumbnailFast`, etc.  
Gold media3.mpvplayer must target **this** surface, not v0.0.3 extras.

`libmpv.so` NEEDED includes `libvulkan.so` (Vulkan linked).

## Java pin

### `is.xyz.mpv.MPVLib` (kept, decompiled)

Native methods align with `libplayer` nm. Extra managed APIs: observers, `isAvailable()`, `setLibraries()`, `event*` callbacks, `grabThumbnail` native.

### `androidx.media3.mpvplayer` (release)

- DEX: **no** `androidx/media3/mpvplayer` path string.
- ProGuard (`FongMi/TV@fongmi` `proguard-rules-media.pro`):

```
-dontnote androidx.media3.mpvplayer.MpvPlayer
-keepclassmembers class androidx.media3.mpvplayer.MpvPlayer {
  boolean toggleGeneralStats();
}
```

→ class **name/package not kept**; only method `toggleGeneralStats` kept. Jadx cannot emit `androidx.media3.mpvplayer.*`.

### App-layer contract (source, not APK)

`MpvUtil` / `MpvPlayerEngine` on `FongMi/TV@fongmi` require compile-time:

```
MpvPlayer.isAvailable()
new MpvPlayer.Builder(ctx).setDecode(decode).setConfig(cfg).build()
player.setDecode(int)          // Engine returns false (no rebuild)
player.setSubtitleOptions(MpvPlayerConfig)
player.addSubtitle(...)
MpvPlayerConfig.Builder + VIDEO_OUTPUT_GPU_NEXT
```

## CI

Workflow: `.github/workflows/audit-fongmi-gold-apk.yml`  
Default `source_url` = FongMi/Release leanback-arm64.  
Gates: `libplayer` + `libmpv` + `is.xyz.mpv`; media3 package **or** R8 keep-name `toggleGeneralStats`.

## Not gold

| Artifact | Why |
|----------|-----|
| `xixu-me/fongmi-tv-actions-builder` APKs | Build FongMi/TV **without** private `lib-*.aar` |
| `fish2018/webhtv` vendored mpvplayer | API/JNI drift; not FongMi release |
| `mpv-android-lib-v0.0.3.aar` | Rollback only; extended JNI; not media3.mpvplayer |

## Phase 1 prerequisites (pick one channel)

1. Obtain unobfuscated `lib-mpvplayer*.aar` / `lib-*.aar` matching FongMi private libs (preferred).
2. Or debug/unsigned build with minify off + same natives.
3. Or R8 `mapping.txt` for 5.5.6 to reverse `MpvPlayer`/`MpvPlayerConfig` from release DEX.
4. Natives: rebuild against **this** `libplayer.nm` + `FongMi/mpv@fongmi` (Phase 3).

Do **not** start app-layer delete of self-hosted `MpvPlayer` until (1–3) yields a compiling `androidx.media3.mpvplayer` module.
