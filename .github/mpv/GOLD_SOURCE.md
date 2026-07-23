# FongMi gold MPV source (Phase 0)

## Why not FongMi/TV GitHub Releases?

`FongMi/TV` has **zero** GitHub Release assets (`releases/latest` → 404).
Private `app/libs/lib-*.aar` is gitignored (`lib-*.aar`) and not published.

## Official download (canonical)

| Field | Value |
|------|--------|
| Repo | [FongMi/Release](https://github.com/FongMi/Release) `@fongmi` |
| Leanback arm64 APK | `https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback-arm64_v8a.apk` |
| Meta | `https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback.json` |
| Recorded version | name `5.5.6`, code `556` (from leanback.json, 2026-07-23 probe) |
| Approx size | ~91 979 932 bytes |

Also present: `leanback-armeabi_v7a.apk`, `mobile-arm64_v8a.apk`, `mobile-armeabi_v7a.apk`.

## App-layer contract (public source, FongMi/TV@fongmi)

Pinned via GitHub API (not raw.githubusercontent from VPS):

- `MpvUtil.java` — uses `androidx.media3.mpvplayer.MpvPlayer` + `MpvPlayerConfig`
- `MpvPlayerEngine.java` — `setDecode` → `player.setDecode`; `return false`; `rebuild()` no-op

Required API surface (must appear in gold APK):

```
MpvPlayer.isAvailable()
MpvPlayer.Builder(Context).setDecode(int).setConfig(MpvPlayerConfig).build()
MpvPlayer.setDecode(int)
MpvPlayer.setSubtitleOptions(MpvPlayerConfig)
MpvPlayer.addSubtitle(...)
MpvPlayerConfig.Builder:
  setDefaultUserAgent / setHlsHttpPersistent
  addConfigDirectory / addAndroidFontConfig / addAndroidDefaults
  addTlsCaFileFromAsset
  addPreInitStringOption / addPostInitStringOption
  addAndroidSubtitleOptions / addDiskCacheOptions
  VIDEO_OUTPUT_GPU_NEXT
```

## How to audit (CI only)

```bash
gh workflow run audit-fongmi-gold-apk.yml \
  --ref feat/mpv-hot-decode-dolby-hdr \
  -f source_url='https://raw.githubusercontent.com/FongMi/Release/fongmi/apk/leanback-arm64_v8a.apk'

gh run watch
# download artifact fongmi-gold-audit-leanback-arm64_v8a.apk
```

Gate: `HAS_MEDIA3_MPVPLAYER=yes` + `libplayer.so` / `libmpv.so` + `api/libplayer.nm.txt`.

## Not gold

- `xixu-me/fongmi-tv-actions-builder` APKs: build FongMi/TV **without** private `lib-*.aar` → almost certainly **missing** media3.mpvplayer (use only as negative control).
- `fish2018/webhtv` vendored `androidx.media3.mpvplayer`: API drift; **do not** treat as FongMi gold.
- Local `mpv-android-lib-v0.0.3.aar`: rollback only; not media3.mpvplayer.

## VPS rule

Do **not** download/unpack the ~92 MB APK on this host. Use the workflow + report artifacts only.

## Phase 0 result (2026-07-23)

See **[PHASE0_GOLD_PIN.md](./PHASE0_GOLD_PIN.md)** and **[libplayer.nm.txt](./libplayer.nm.txt)**.

- APK SHA256 `3d915e99…89c3` (5.5.6 / 556)
- Natives + `is.xyz.mpv.MPVLib` **OK**
- `androidx.media3.mpvplayer` **R8-obfuscated** in release → need private AAR / mapping / debug for Phase 1
