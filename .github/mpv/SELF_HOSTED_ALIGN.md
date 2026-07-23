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

## Still open

| Phase | Work |
|-------|------|
| D | `MpvOptions` central init (optional polish) |
| E | Drop `MPVNode`/`getPropertyNode`; gold AAR from FongMi/mpv-android+mpv; archive v0.0.3 |
| F | Device matrix + instrumentation |

## Product copy

> MPV 引擎行为对齐 FongMi（软硬解热切 / 生命周期 / Vulkan 选项），**不是** media3.mpvplayer 同源模块。

## Rollback

`app/libs/mpv-android-lib-v0.0.3.aar` remains the known-good extended-JNI AAR until Phase E lands a gold-nm AAR. Do not mix two `is.xyz.mpv` AARs.
