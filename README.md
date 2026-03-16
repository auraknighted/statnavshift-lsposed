# StatNav BurnIn (LSPosed)

LSPosed module for AOSP-based ROMs (those that don't include status bar or navigation bar burn-in protection) that applies tiny periodic shifts to the status bar and navigation bar to help mitigate OLED burn-in.

> Español: see [README.es.md](README.es.md).

## What it does

- Hooks `com.android.systemui` through LSPosed.
- Applies subtle padding/pixel shifts every minute to:
  - `PhoneStatusBarView` see: [.java android source code file](https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/src/com/android/systemui/statusbar/phone/PhoneStatusBarView.java)
  - `NavigationBarView` see: [.java android source code file](https://android.googlesource.com/platform/frameworks/base/+/0d210f6/packages/SystemUI/src/com/android/systemui/statusbar/phone/NavigationBarView.java)
  - `NavigationBarFrame` see: [.java android source code file](https://android.googlesource.com/platform/frameworks/base/+/7516354f0637/packages/SystemUI/src/com/android/systemui/navigationbar/NavigationBarFrame.java)
- Uses very small offsets (about `0.7px` to `3px`) and short animation duration.
- Resets view position when detached.

## Recommended LSPosed scope

This module declares a recommended scope directly in the manifest so LSPosed can suggest which target app(s) to enable:

- `com.android.systemui` (recommended)
- `android` (framework, optional fallback on some ROMs, probably)

## Requirements

- Android 8.1+ (`minSdk 27`), tested up to Android 16 (`maxSdk 36`)
- LSPosed enabled, use [JingMatrix LSPosed](https://github.com/JingMatrix/LSPosed/releases)
- Restart SystemUI (or reboot device) after enabling the module/scope

## Local build

```bash
gradle :app:assembleRelease
```

Release APK output is in `app/build/outputs/apk/release/` (`app-release.apk`, signed with debug keystore for easy testing installs).

## GitHub Actions artifacts

This repository includes a workflow that builds APKs on every push/PR and uploads them as downloadable artifacts. 
