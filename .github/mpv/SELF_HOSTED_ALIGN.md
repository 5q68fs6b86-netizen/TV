# Self-hosted MPV — FongMi behaviour align (not media3.mpvplayer)

## Boundary

| Claim | Status |
|-------|--------|
| Runtime type `androidx.media3.mpvplayer.MpvPlayer` | **No** — still `com.fongmi.android.tv.player.mpv.MpvPlayer` |
| Behaviour vs FongMi `MpvUtil` / `MpvPlayerEngine` | **Target** — setDecode no rebuild, rebuild no-op, vulkan pre-init only |
| Native gold | Phase 0 pin in `libplayer.nm.txt` + FongMi/mpv-android@fongmi |

## Done in app layer (Phases A–C)

1. **Engine** (`MpvPlayerEngine`):
   - `setDecode` → `player.setDecode`; **always return false**
   - `rebuild()` → return same `player` (no release+new)
   - `applyDolbySetting` never requests rebuild
2. **Hot decode** (`MpvPlayer.applyDecodeHot`):
   - Only `hwdec` option/property
   - **No** `stop` / `loadfile` as success path
   - Limited Surface rebind only when hwdec still reports `no`
3. **Release**:
   - `destroy` off main looper under `NATIVE_LOCK`
   - Caller **awaits** destroy (5s timeout) before returning from `handleRelease`
   - `createNative` refuses double-create; force-destroy if stuck

## Phase D — `MpvOptions` (done)

- New `MpvOptions.java`: pre-init / post-init funnel used by `MpvPlayer.initialize()`
- Vulkan: only `gpu-api` + `androidvk`; `vo` from `isMpvGpuNext()` only
- Demuxer budget can scale with `PreloadSetting`; default UA from `Setting`/`PlayerHelper`
- HDR: optional `target-colorspace-hint` only (not product guarantee)

## Phase E — progress

### App (done on this branch)

- Track list via `track-list/count` + `track-list/N/*` string/int properties (**no** `getPropertyNode` at runtime)
- `observeProperty("track-list", MPV_FORMAT_NONE)` with NODE fallback for old AAR
- `handleEndFile(reason,error,errorString)` gold-style; node payload only as optional decode of v0.0.3 events
- Instrumentation header check no longer uses `MPVNode`

Still **compiles against** v0.0.3 `EventObserver` (`event(int, MPVNode)` / `eventProperty(..., MPVNode)`) until a gold classes AAR is the default dependency.

### Native CI (skeleton)

- Workflow: `.github/workflows/build-mpv-fongmi-native.yml`
- Sources: `FongMi/mpv-android@fongmi` + `FongMi/mpv@fongmi`
- Gate: `nm` symbol **names** ⊇ `.github/mpv/libplayer.nm.txt`
- Output: jni AAR + lock snippet artifact (full Kotlin classes packaging TBD)

Default app still loads `app/libs/mpv-android-lib-v0.0.3.aar` until gold AAR is published and swapped.

## Still open

| Phase | Work |
|-------|------|
| E2 | Finish gold AAR with `is.xyz.mpv` classes; swap `app/libs`; archive v0.0.3 |
| F | Device matrix + instrumentation |

## Product copy

> MPV 引擎行为对齐 FongMi（软硬解热切 / 生命周期 / Vulkan 选项），**不是** media3.mpvplayer 同源模块。

## Rollback

`app/libs/mpv-android-lib-v0.0.3.aar` remains the known-good extended-JNI AAR until Phase E lands a gold-nm AAR. Do not mix two `is.xyz.mpv` AARs.
