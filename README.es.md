# StatNav BurnIn (LSPosed)

Módulo LSPosed para ROM basadas en AOSP (aquellas que no incluyen protección contra el quemado de la barra de estado o la barra de navegación) que aplica pequeños desplazamientos periódicos a la barra de estado y a la barra de navegación para ayudar a mitigar el quemado de las pantallas OLED.

> English: ver [README.md](README.md).

## Qué hace

- Hookea `com.android.systemui` mediante LSPosed.
- Aplica desplazamientos sutiles de padding/píxeles cada minuto en:
  - `PhoneStatusBarView` ver: [archivo .java de código fuente](https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/src/com/android/systemui/statusbar/phone/PhoneStatusBarView.java)
  - `NavigationBarView` ver: [archivo .java de código fuente](https://android.googlesource.com/platform/frameworks/base/+/0d210f6/packages/SystemUI/src/com/android/systemui/statusbar/phone/NavigationBarView.java)
  - `NavigationBarFrame` ver: [archivo .java de código fuente](https://android.googlesource.com/platform/frameworks/base/+/7516354f0637/packages/SystemUI/src/com/android/systemui/navigationbar/NavigationBarFrame.java)
- Usa offsets mínimos (aprox. `0.7px` a `3px`) y animaciones cortas.
- Restaura la posición de la vista al desmontarse.

## Scope recomendado en LSPosed

Este módulo declara en el manifest el scope recomendado para que LSPosed muestre qué app(s) activar:

- `com.android.systemui` (recomendado)
- `android` (framework, fallback opcional en algunas ROMs)

## Requisitos

- Android 8.1+ (`minSdk 27`), probado hasta Android 16 (`maxSdk 36`)
- LSPosed activo, utiliza [JingMatrix LSPosed](https://github.com/JingMatrix/LSPosed/releases)
- Reiniciar SystemUI (o reiniciar el dispositivo) tras activar módulo/scope

## Build local

```bash
gradle :app:assembleRelease
```

El APK de release queda en `app/build/outputs/apk/release/` (`app-release.apk`, firmado con debug keystore para facilitar instalaciones de prueba).

## Artifacts de GitHub Actions

Este repositorio incluye un workflow que compila APKs en cada push/PR y los sube como artifacts descargables.
