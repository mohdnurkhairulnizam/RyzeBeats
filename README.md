# RyzeBeats

RyzeBeats is a local-first MP3 player with a Minimal Hi-Fi interface. This repository contains a browser player and an Android Capacitor wrapper.

## Web

```bash
cd web
pnpm install
pnpm run check
pnpm run build
```

The browser player accepts MP3 files through the local file picker.

## Android

```bash
cd android-app
pnpm install
pnpm run check
pnpm run build
pnpm cap:sync
```

On Android, the player requests audio access and scans the device media library. Release signing files are intentionally excluded from this repository.
