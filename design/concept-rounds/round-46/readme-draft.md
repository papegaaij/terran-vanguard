<!--
Draft of the new top section of README.md (step README-CAP, pending the user's review).
It replaces the README from its first line down to (not including) "## Building and running";
the paragraphs about design/, CLAUDE.md, concept rounds and Git LFS move under "## The project".
Image paths are relative to the repository root. The images are in Git LFS: GitHub renders LFS
images in a README (served from media.githubusercontent.com).
Note: the repository has no published release yet (`gh release list` is empty, no `v*` tag), so the
Download link leads to an empty page until the first `v*` tag lets CI publish the bundles.
-->

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
