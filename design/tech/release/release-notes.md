# Terran Vanguard @tag@ — Acts 1 to 5

A late-90s style vertical shoot'em up set in 2185. This release holds the first five of the
campaign's seven acts: thirty-five levels on easy, medium or hard, from Earth orbit and Luna
(Act 1, *First Contact*) over Earth's cities, oceans and arctic (Act 2, *Homefront*), the Mars
colonies (Act 3, *Red Dust*) and Jupiter's moon Europa (Act 4, *Deep Water*) to the asteroid belt
(Act 5, *The Belt*). Each act has its briefings, radio chatter and outro, the shop and the hangar
between the levels, and a boss at its end: the Brood Carrier, the Siege Spire, the Dust Colossus,
the Abyssal Maw and the Iron Sovereign. From Act 2 on Rook flies with you as your wingman.

New in this release, Act 5: seven levels in the belt, from the dock canyon of Ceres Hub as the
station turns sides, through a mined shipping lane, a refugee tug to escort and the tunnels of the
hollowed asteroid Kleopatra, to Ceres orbit; for the first time the enemy is human-built, with
shielded ships and walkers; the Tesla Coil Pod and the Swarm Tail in the shop, the Shield Mk V and
a captured Mk V "Helix" generator; and the Iron Sovereign, a battle station of rotating rings.
After Level 35 the campaign stops at Act 6, *Jovian Storm*, which follows in a later release. A
save from `v0.2` carries on: Continue opens Act 5's intro.

## Download

| System | File |
|---|---|
| Windows 10/11, 64-bit | `terran-vanguard-@version@-winX64.zip` |
| macOS, Apple Silicon (M1 and later): untested on Apple silicon, see below | `terran-vanguard-@version@-macM1.zip` |
| macOS, Intel | `terran-vanguard-@version@-macX64.zip` |
| Linux, 64-bit x86 | `terran-vanguard-@version@-linuxX64.zip` |

Unpack the zip anywhere and start the game from the `terran-vanguard-@version@` folder; it brings
its own Java runtime, nothing has to be installed. `README.txt` in that folder has the controls
and where the settings, saves and screenshots are kept.

## The game is not signed

- **Windows:** SmartScreen may say "Windows protected your PC". Choose **More info**, then
  **Run anyway** (only on the first start).
- **macOS:** the first start is refused ("cannot be opened because the developer cannot be
  verified"). Control-click the app, choose **Open** and confirm, or choose **Open Anyway** in
  System Settings → Privacy & Security. If macOS says the app is damaged, run once in Terminal:
  `xattr -dr com.apple.quarantine "/path/to/Terran Vanguard.app"`.
- **Linux:** if the game does not start, make it executable: `chmod +x terran-vanguard`.

The Apple silicon build is made the same way as the others but has not yet been started on a
real Mac with an M-series chip; if it will not open even after the steps above, please report it.

## Requirements

A 64-bit system with OpenGL 3.2 or newer (any graphics chip from the last ten years); keyboard
or gamepad. The game starts in borderless full screen; Alt+Enter or F11 switch to a window.

## Licence and credits

The game is open source under the Apache License 2.0 (`LICENSE.txt`). `CREDITS.md` lists every
third-party file (sound effects, fonts) with its author and licence.
