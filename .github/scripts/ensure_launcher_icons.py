#!/usr/bin/env python3
"""Ensure every Android application module has a launcher icon before Gradle builds."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
ANDROID = "http://schemas.android.com/apk/res/android"
ET.register_namespace("android", ANDROID)

# A clean, high-contrast launcher mark. The same reusable artwork is installed
# into each app module; app-specific artwork can replace these resources later.
FOREGROUND = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M54,13 C31.4,13 13,31.4 13,54 C13,76.6 31.4,95 54,95 C76.6,95 95,76.6 95,54 C95,31.4 76.6,13 54,13 Z"/>
    <path android:fillColor="#FF172554"
        android:pathData="M59,25 L35,59 L50,59 L45,83 L73,47 L57,47 Z"/>
</vector>
"""
LEGACY = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp" android:height="48dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FF2563EB"
        android:pathData="M54,4 C26.4,4 4,26.4 4,54 C4,81.6 26.4,104 54,104 C81.6,104 104,81.6 104,54 C104,26.4 81.6,4 54,4 Z"/>
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M59,25 L35,59 L50,59 L45,83 L73,47 L57,47 Z"/>
</vector>
"""
ADAPTIVE = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
"""

updated = 0
for module in ROOT.iterdir():
    manifest = module / "src/main/AndroidManifest.xml"
    gradle_files = [module / "build.gradle", module / "build.gradle.kts"]
    if not manifest.is_file() or not any(p.is_file() for p in gradle_files):
        continue

    tree = ET.parse(manifest)
    app = tree.getroot().find("application")
    if app is None:
        continue
    icon_attr = f"{{{ANDROID}}}icon"
    if icon_attr not in app.attrib:
        app.set(icon_attr, "@mipmap/ic_launcher")
        tree.write(manifest, encoding="utf-8", xml_declaration=True)

    res = module / "src/main/res"
    (res / "drawable").mkdir(parents=True, exist_ok=True)
    (res / "values").mkdir(parents=True, exist_ok=True)
    (res / "mipmap-anydpi").mkdir(parents=True, exist_ok=True)
    (res / "mipmap-anydpi-v26").mkdir(parents=True, exist_ok=True)
    (res / "drawable/ic_launcher_foreground.xml").write_text(FOREGROUND, encoding="utf-8")
    (res / "values/ic_launcher_colors.xml").write_text(
        '<resources><color name="ic_launcher_background">#FF2563EB</color></resources>\n',
        encoding="utf-8")
    (res / "mipmap-anydpi/ic_launcher.xml").write_text(LEGACY, encoding="utf-8")
    (res / "mipmap-anydpi-v26/ic_launcher.xml").write_text(ADAPTIVE, encoding="utf-8")
    updated += 1

print(f"Launcher icons ensured for {updated} Android application module(s).")
if updated == 0:
    raise SystemExit("No Android application modules with manifests were found.")
