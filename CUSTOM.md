# What this fork changes

This is a customised fork of [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv). Debloating,
restoring, the tools and themes work exactly as the [README](README.md) describes. Only the reference
launcher (`launcher/`) is different, set up for one TV:

**Tested on:** SHARP 2K Smart TV (Skyworth-built, device `umeda`, MediaTek), Android 11 / Google TV,
build `RTM9.241015.157`, about 1.4 GB RAM.

## Added features

| Feature | What it does |
|---|---|
| **Now Playing card** | A frosted card under the clock with the album art, title and artist of whatever is playing (Spotify and any app with a media session), and Prev / Play-Pause / Next. It updates the moment the track changes and hides when nothing plays. Left from Prev selects the art and title; OK opens the playing app. |
| **Wi-Fi popup** | The network pill opens a glass popup: the current network, nearby Wi-Fi sorted by signal, and Network Settings. On Android 10 and newer, Android does not let apps join a network, so choosing one opens Wi-Fi settings. A configured `vpn_app` still takes the pill as before. |
| **Compact stats** | Memory used (%) and free storage as two small icons at the top right, above the pills. Read every 10 seconds, only while Home is on screen. CPU is left out where Android hides it (Android 8 and newer on many TVs). |

The Now Playing card and the Wi-Fi popup are based on
[mosami11-11/cleanyourtv-custom](https://github.com/mosami11-11/cleanyourtv-custom) (MIT).

## Configuration in `config.xml`

- Dock of five: YouTube, Bilibili ([BT](https://github.com/chinasoul/BT), relabelled with `app_labels`),
  Spotify, YouTube Music, Netflix, with five tiles per row (`columns` 5).
- `cinematic_home` on, `cinematic_dots` off: the focused dock app's artwork tints the top of the screen.
- `icon_only_buttons` on with `wifi_label_width` 200: Settings is a round gear, the network pill keeps
  its label.
- `tile_menu` on: hold OK on a tile to Move it or open App info.
- `show_stats` on (the compact stats above).

Change the dock to your own apps before you build: package names come from
`tools/tv.py 'pm list packages'`.

## Setup

Build and install as usual (`launcher/build-and-install.sh`, or `build-and-install.ps1` on Windows), then
give the Now Playing card access to media sessions, once; it survives reinstalls:

```sh
tools/tv.py "cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaNoteService"
```

## Permissions compared with the original

Still no `INTERNET` permission, no network features and no analytics. Added:

- `CHANGE_WIFI_STATE`, for the Wi-Fi popup's scan.
- `MediaNoteService`, a notification listener that only unlocks the list of media sessions. It reads no
  notification.

## Keeping up with the original

```sh
git remote add upstream https://github.com/Gathin23/cleanyourtv   # once
git fetch upstream
git rebase upstream/main
git push --force-with-lease
```

A rebase stops where the original changed the same lines (`config.xml`, `MainActivity.java`); resolve and
continue with `git rebase --continue`.
