# Sharp Remote (Android, Java)

A replacement for the Sharp **076K0VK011** TV remote that uses the phone's built-in
IR blaster (Xiaomi / Redmi / Poco / Honor and others). No internet or Wi-Fi needed.

## Features
- All buttons from the original remote, in the same layout
- Three built-in Sharp code sets, plus a **Which set?** button that finds the one your TV uses
- Volume, channel and arrow buttons repeat while held
- **Edit** mode: tap any button to type its code, test it, or run **Find code…**,
  which tries all 256 commands one by one until the TV reacts. Useful for buttons
  the built-in sets don't know, such as FAVORITE.
- Your codes are saved on the phone

## Build the APK

### Option A: Android Studio (computer)
1. Open this folder in Android Studio (Ladybug or newer).
2. Let it sync, then choose **Build › Build App Bundle(s) / APK(s) › Build APK(s)**.
3. Copy `app/build/outputs/apk/debug/app-debug.apk` to your phone and install it.

### Option B: GitHub (works from a phone, no computer)
1. Create a new repository on github.com and upload all files from this folder
   (keep the folder structure, including `.github/workflows/build.yml`).
2. Open the **Actions** tab. The "Build APK" run starts on its own (about 3–5 minutes).
3. Open the finished run and download **SharpRemote-apk** under *Artifacts*.
   Unzip it and install `app-debug.apk` (allow "install unknown apps" when asked).

## First use
1. Open the app and tap **Which set?**, then **Send** while pointing at the TV.
   Pick the set that opens the TV menu.
2. Dimmed buttons have no code in that set yet. Tap **Edit**, tap the button,
   then **Find code…**.

## Code sources
- Flipper-IRDB: `TVs/Sharp` (Sharp_LC-RC1-16, Sharp_TV2, Aquos)
- probonopd/irdb: `codes/Sharp/TV`
"# Sharpremote" 
