<h1 align="center">Terran Vanguard</h1>

<p align="center">
  <img src="docs/screenshots/level-21-colossus-death.gif" width="640" alt="The Dust Colossus erupts under the ship, takes the Ion Beam and dies in its death throe">
</p>

<p align="center">
  <b>A late-90s style vertical shoot'em up.</b><br>
  The year is 2185. An alien fleet pours through a gate at the edge of the solar system,
  and one pilot flies against it, from the Moon to the streets of Earth and the red dust of Mars.
</p>

<p align="center">
  <a href="https://github.com/papegaaij/terran-vanguard/releases/latest"><b>Download the latest release</b></a>
  · Windows · Linux · macOS (Intel and Apple Silicon)
</p>

## Screenshots

<table>
  <tr>
    <td width="50%"><img src="docs/screenshots/level-07-brood-carrier.png" alt="Level 07, the Brood Carrier"></td>
    <td width="50%"><img src="docs/screenshots/level-11-harbour-kraken.png" alt="Level 11, the Harbour Kraken"></td>
  </tr>
  <tr>
    <td>Act I, Level 07: the Brood Carrier opens its core over the Moon.</td>
    <td>Act II, Level 11: the Harbour Kraken surfaces under the Atlantic convoy.</td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/level-14-siege-spire.png" alt="Level 14, the Siege Spire"></td>
    <td><img src="docs/screenshots/level-19-revenant-walker.png" alt="Level 19, the Revenant Walker"></td>
  </tr>
  <tr>
    <td>Act II, Level 14: the Siege Spire tears loose from the city and sweeps its beam.</td>
    <td>Act III, Level 19: the Revenant Walker burns the foundry floor.</td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/level-21-dust-colossus.png" alt="Level 21, the Dust Colossus"></td>
    <td><img src="docs/screenshots/hangar.png" alt="The hangar shop"></td>
  </tr>
  <tr>
    <td>Act III, Level 21: the Dust Colossus breaches, its spine trailing across the field.</td>
    <td>The hangar: buy, upgrade and fit weapons and parts between missions.</td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/briefing.png" alt="A mission briefing"></td>
    <td><img src="docs/screenshots/title-screen.png" alt="The title screen"></td>
  </tr>
  <tr>
    <td>Every mission opens with a voiced briefing and a tactical map.</td>
    <td>The title screen.</td>
  </tr>
</table>

## Features

- **Three acts, 21 levels** of a planned seven-act, ~50-level campaign: First Contact on the
  Moon, Homefront on Earth, Red Dust on Mars.
- **Bosses** that fill the screen: the Brood Carrier, the Gorgon Frigate, the Harbour Kraken, the
  Siege Spire, the Revenant Walker and the Dust Colossus, each with its own phases and death.
- **Rook**, your wingman from Act II on: he flies in the escort slot with the gun you buy him,
  takes hits, and talks back.
- **A Tyrian-style loadout**: front and rear guns, two wing mounts, generator, shield, armour,
  utility modules and a special, bought and upgraded in the hangar with the credits you earn;
  power draw, a regenerating shield and armour that does not regenerate.
- **The pre-rendered look** of the late 90s: CGI sprites and backdrops, a portrait play field
  with HUD side panels on a 16:9 screen.
- **Voiced radio chatter and briefings** that tell the story in flight, from first contact to
  the twist.
- Three difficulties, mission grades and high scores, keyboard and gamepad.

## The project

Everything about the game — story, levels, enemies, ship, systems, UI, art and audio — is
described in the living documentation under [design/](design/README.md), which also serves as
the implementation backlog. The game itself is being built with libGDX on Java 21
([roadmap](design/tech/roadmap/README.md)).

- Working rules for the documentation: [CLAUDE.md](CLAUDE.md)
- Open choices awaiting a decision: [design/concept-rounds/](design/concept-rounds/README.md)
- Validate the documentation: `python3 tools/check_docs.py`

Binary assets are stored with Git LFS; install `git-lfs` before cloning.

## Building and running

Requires JDK 21 (`JAVA_HOME` pointing at it) and the Git LFS files; Gradle comes with the wrapper.

```bash
./gradlew check                 # compile, format check, all tests
./gradlew :desktop:run          # start the game
./gradlew :desktop:packageLinuxX64   # bundle with its own runtime: desktop/build/construo/dist/
                                     # (release: design/tech/release/README.md)
```

The first start opens in borderless full screen; **Alt+Enter** or **F11** switch to a window
and back. On the title screen **Enter** (or A on a gamepad) starts Level 01 and **Esc**
(or B / Back) quits; in flight, arrows / WASD or the left stick move, **Space** / Z / A fire,
**Left Shift** / C / right bumper hold precision mode, and **Esc** / P / Start return to the
title. The display mode, monitor and window position are kept in `settings.properties` in the
platform's config directory (`~/.config/terran-vanguard/` on Linux, `%APPDATA%\Terran Vanguard\`
on Windows, `~/Library/Application Support/Terran Vanguard/` on macOS); add
`controls.auto-fire=true` there to fire without holding the button.

Options: `--version` prints the version and exits; `--bench <seconds>` flies Level 01, exits after that time and logs the frame count;
`--settings <file>` uses another settings file; `--difficulty easy|medium|hard` picks the
difficulty (medium by default, until the new-game menu exists); `--debug-speed <n>` (a testing
aid) runs game time n times faster, e.g. 4 to get through Level 01 in under a minute.

Placeholder art and audio come from the chosen concept files: `./gradlew :pipeline:importPlaceholders`
copies them and cuts the sprite frames into `assets/` (committed); the build packs the frames
into texture atlases (`:pipeline:packAtlases`, build output only).
