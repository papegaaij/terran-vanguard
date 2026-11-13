---
title: Act 7 – Beyond the Gate
design: approved
implementation: in-progress
art: chosen
depends-on: [../act-6-jovian-storm, ../../world/vrell-space, ../../story/twist, ../../story/characters/the-choir, ../../story/characters/vorne, ../../enemies/bosses/vornes-chimera, ../../enemies/bosses/choir-heart, ../../player/generator, ../../ui/debrief]
updated: 2026-11-13
---

# Act 7 – Beyond the Gate

## Summary

Levels 43–50, the campaign's last act. The fleet splits at the **Tether Gate** beyond Neptune: the line ships and the
bombers hold the near side, and only the carrier *Steadfast*'s group follows Aegis Wing through, past the gate's
**Gate Wardens** and through **distorted space** into Vrell space. There the Choir, heard **afraid** for the first
time, speaks to Lancer; the wing burns a breeding reef, crosses a dead Vrell fleet **hollowed from the inside** (the
Silence's hint, never shown), runs the gauntlet of the Choir's elite **Seraphs**, ends Vorne fused with his lifeboat
(**Vorne's Chimera**), fights its old battles again in the Choir's memory (**the boss rush**) and kills the **Choir
Heart**. The Choir had held the gate open; it closes, the wing races back through, and on the near side the ring goes
dark. It was a lock, built to keep something out.

**Status: approved** (the Act 7 design phase, 2026-11-04; the user approved it on 2026-11-05, round 84). The eight level
documents, the four new units, both bosses and the echoes, the systems and the campaign end were drafted in the design
phase's waves from the gap analysis's recommended answers (Q1–Q12, the judgement calls J1–J16; a local working note,
`m11-dgap-gaps.md`); this README pulls them together (the intro and ending pages, the units, the density and haul
table, the threat profile, the music and the voice) as [Act 6's](../act-6-jovian-storm/README.md) did. There is **no
`data.yaml`** under the campaign or the enemies for Act 7 yet: every number here is a hand table taken from the level
drafts, and the build (M11, [roadmap](../../tech/roadmap/README.md#m11-parts)) turns them into data and measures them.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [level-43-the-tether-gate](level-43-the-tether-gate/README.md) | The gate's mouth, space, all from the front: Gate Wardens whose shell opens only while they fire (`WARDENS n / 16`, none may leave), the Vrell's swarms and hybrids between them, the ring's surface and its glyph stones, the gate transit's 1.5 s warp | approved | done | chosen |
| [level-44-crossing](level-44-crossing/README.md) | Inside the gate, space, every direction: distorted space turns the scroll up to 25° and twice reverses it, told 2 s ahead; Rift Skaters jumping along the current, three Coilwyrm chains; a 65 % kill ratio | approved | done | chosen |
| [level-45-hive-reef](level-45-hive-reef/README.md) | A living reef, ground and air, `destroy-targets`: eight spawning organs (one left alive fails the mission), spawning pits, Whirl Seed clusters, spore clouds, Threadcrawlers (`CRAWLERS`), the call organ cut loose (the Swarm Call) | approved | done | chosen |
| [level-46-graveyard-of-the-choir](level-46-graveyard-of-the-choir/README.md) | The breather: a dead Vrell fleet hollowed from inside, Husks harmless until hit, **the quiet** (24 s, the music out, a dying Leviathan, a starless patch), seed caskets (`CASKETS`), the warm hull's living core (the Mk VI) | approved | done | chosen |
| [level-47-seraph-gauntlet](level-47-seraph-gauntlet/README.md) | The Choir's inner sphere, air, every direction: the act's hardest regular waves, Choir Seraphs in pairs that jump and mirror the ship under the Heralds' rings (`SERAPHS n / 14`), the Choir's fear | approved | done | chosen |
| [level-48-vornes-end](level-48-vornes-end/README.md) | A Vrell citadel over gold wrecks, front and rear: swarms from behind, grown turrets, the reactor cores (`CORES n / 8`), then the mid-boss Vorne's Chimera in the spire well, who dies mid-sentence | approved | done | chosen |
| [level-49-echoes](level-49-echoes/README.md) | The Choir's memory: Luna, Mars and Ceres remembered in the spectral tint, then the boss rush: the echoes of the Brood Carrier, the Dust Colossus and the Iron Sovereign, refills and a checkpoint between them | approved | done | chosen |
| [level-50-choir-heart](level-50-choir-heart/README.md) | The hive core: every Vrell family in short waves, the final boss Choir Heart (membranes, arena, core), its last words, the closing gate raced as a scene, the ring dark on the near side; the campaign end | approved | done | chosen |

## Design

### Synopsis

November 2185. After the refit at Callisto the Coalition fleet burns for weeks to the Tether Gate beyond Neptune, the
moon-sized ring found dead in 2141 and awake since March. Okafor splits the fleet: the line ships and the bomber
carriers hold the near side, the way home, and she takes the *Steadfast*'s group through behind Aegis Wing, beyond the
reach of the bombers and the Lance's satellites (`NO COVER`). The Gate Wardens fall, the wing warps through the event
plane, where space itself drifts, and on the far side the Choir speaks to Lancer at its door, then more often, and
afraid. Past a breeding reef that is "*all we carry*", the wing crosses a fleet of dead Vrell ships emptied from the
inside, and in the quiet Varga understands that the Vrell "*are not singing to us. They are singing away from
something.*" In the inner sphere the Choir's elite fly and its voice says "*the silence comes*". In a citadel grown over
the gold wrecks of the Ascendancy, Vorne, fused with the lifeboat the Vrell grew for him, makes his last stand and dies
trying to warn them of what is coming. The Choir throws its memories at the wing, the battles it lost, and at the hive
core Aegis Wing destroys the Choir Heart. Its last words: "*…it is behind us… it has always been behind us…*". The
Choir held the gate open; the gate closes, Lancer and Rook race back through the closing event plane, and on the near
side the ring goes dark. The war is over. The gate was a lock.

### Story beats

The beats are the story documents' ([story](../../story/README.md), [twist](../../story/twist/README.md),
[the Choir](../../story/characters/the-choir/README.md#act-7-draft), [Vorne](../../story/characters/vorne/README.md),
[Okafor](../../story/characters/okafor/README.md#act-7-draft), [Varga](../../story/characters/varga/README.md#act-7-draft),
[Rook](../../story/characters/rook/README.md#act-7-draft), [Lancer](../../story/characters/lancer/README.md),
[the Vrell](../../story/factions/vrell/README.md)); the lines are the level and boss documents'.

- **The act intro** (J2): Okafor at the gate (the ring awake; the fleet split and why there is no cover past it), the
  Choir intercepted through the gate on page 3, a plea to "the small ones", and her order on page 4.
- **L43**: the crossing; Okafor stays on her bridge; the radio's noise begins (step 1); the Choir's first word to
  Lancer: "*…small bright one… why do you stand in the door…*" (fixed); the transit.
- **L44**: the event plane; the Choir's first "*it*" (it sees every light that crosses); Varga has never heard the
  Choir afraid.
- **L45**: the reef: "*…do not burn the brood… it is all we carry…*"; Varga's "*Carry? Carry where?*"; the call organ
  taken.
- **L46**: the quiet and the hollow hulls; "*…these were ours… we left them… we ran…*", and Varga's story line,
  "*They are not singing to us. They are singing away from something.*" Nobody says by what.
- **L47**: the Choir's fear rises ("*…we are running…*", then the fixed "*…the silence comes… it is behind us…*");
  Rook takes it literally ("*We're behind them. Aren't we?*"), and nobody answers.
- **L48**: Vorne's end, pulp at its peak, his one shout in the fusion's pain, and his fixed last line breaking off:
  "*Do you know what's coming after them, pilot? Do you have any idea what I was trying to—*". Rook's one line, no joke.
- **L49**: the Choir's grief: the echoes are what it lost; the Sovereign is Vorne's memory, now the Choir's; Varga's
  "*It remembers you whole*" gives the refills their reason.
- **L50**: the Heart, its five lines, its last words and the face-like frame that scatters; the gate closing; the race;
  "*The ring is dark… It's just stone again.*"; Okafor's "*It's over, Aegis. Stand down.*"
- **The ending pages** (Q8 a; no act outro): the war over, the gate a lock and the Silence named once, Rook's one line,
  Okafor's last order and the dark ring with a faint signal from beyond.

**The Silence's rule, Act 7**: the Choir fears it, calls it "*it*", says the word "*silence*" once (L47) and never
describes it; Varga names it once more, on her ending page; Vorne's last line breaks off before he can; nobody
describes it, and it is never shown.

**Missed captures** (Level 34's rule): if the call organ (L45) or the warm hull's core (L46) scrolls past untaken, the
*Steadfast*'s salvage tug, which crossed with her group, picks it up behind the wing; the Swarm Call and the Mk VI
reach the shop on their visits either way, and only the capture's line and the bonus are lost.

### Act intro and ending

Format: the [briefing screen](../../ui/briefing/README.md), as Acts 1–6. Order: Level 42's act end (Act 6's debrief and
outro), then this act's title card, the act intro below, the L43 mission briefing, the hangar visit before L43
(autosave), L43. A save from the `v0.4` release waiting at the `COMING SOON` hangar plays the intro **once** on Continue
(the `act-intro:7` flag, as Acts 3–6's; [saves](../../systems/saves/README.md#the-completed-campaign-act-7-draft)). Page 3
is **intercepted** through the gate ([briefing](../../ui/briefing/README.md#intercepted-transmissions-draft)): the
Choir's own pattern in the portrait frame, clean (it is close, J3); the briefing theme fades out over 1 s and track 16's
`choir` stem plays alone under it, track 3 back over 1 s on page 4 ([music](../../audio/music/README.md#act-7-music-draft)).

**Timeline** (J10, draft): Act 6 ends at Callisto in September 2185; the refit and the weeks of burn past Saturn and
Uranus put the fleet at the gate in **November 2185**.

**Title card** (J1; held 3.5 s over the act's still `ui/act-7-beyond-the-gate-still.png`, rendered by
`tools/art/act_stills.py act-7` from Level 43's production backdrop as Acts 2–6's were: **"the mouth"** (A.9 b, chosen
2026-11-05, round 84): the gate ring head-on and centred, its mouth filled with the event plane's violet-white, the
*Steadfast*'s group strung in a line toward it; its chrome lettering from `tools/art/act_titles.py`; M11 part B). Text (draft):

<!-- data: act-title-card -->
> ACT VII
> BEYOND THE GATE
> The Tether Gate · November 2185
<!-- /data -->

**Act intro** (four pages, each voiced and with its own image in `assets/ui/briefing/`). Texts (draft); three by
Commander Okafor, one by the Choir through the gate:

| Page | Speaker | Channel | Image |
|---|---|---|---|
| 1 | Commander Okafor (grim) | briefing | `act-7-the-gate`: the *Steadfast*'s bridge, the ring filling the forward viewports, its glyphs lit cold teal-white along the rim (Vrell space's glyphs), the fleet's running lights strung behind |
| 2 | Commander Okafor (neutral) | briefing | `act-7-the-split`: a CDF chart of the gate: the line ships and the bomber carriers on the near side, the *Steadfast*'s group's course through the ring as a dotted line, the Lance's relay at Neptune with its reach ending at the ring |
| 3 | The Choir | its own (intercepted through the gate) | `act-7-through-the-gate`: the event plane's violet seen through the ring's mouth, the Choir's pattern in the portrait frame |
| 4 | Commander Okafor (fierce) | briefing | `act-7-first-in`: Okafor at the comms board; through the window behind her the Stormhawks lining up on the ring's mouth |

<!-- data: act-briefing -->
> **Commander Okafor:** "Aegis, Okafor, from the bridge of the Steadfast. Weeks out from
> Callisto, and the Tether Gate fills every window. It was dead when we found it. It is awake
> now."
>
> **Commander Okafor:** "We split here. The line and the bombers hold this side: the way home.
> The Steadfast's group follows you through, and nothing else, so past the ring there are no
> bombers and no Lance."
>
> **The Choir:** "…small ones… stay behind your door… do not follow us in…"
>
> **Commander Okafor:** "All ships. We did not come this far to watch a door. Aegis, you go
> first; the Steadfast is right behind you. Mission briefing follows. Aegis Actual out."
<!-- /data -->

Page 2 is the reason for `NO COVER` ([specials](../../player/specials/README.md#act-7-specials-draft)); page 3 is the
Choir's line 1, heard first and asking nothing yet; page 4 answers it without naming it. L43's briefing carries the
mission (the Wardens, the transit); its Choir line is the act's first in play.

**The ending** (Q8 a; Act 7 has **no act outro**): Level 50 ends on the near side's closing lines
([Level 50](level-50-choir-heart/README.md)), then the [campaign end](../../ui/debrief/README.md#the-campaign-end-act-7-draft)
runs: the final debrief with the act summary `ACT VII COMPLETE · BEYOND THE GATE`, **the ending pages below** in the act
outro's place on track 27 *Ending*, the end card `CAMPAIGN COMPLETE`, the name entry, the credits on track 28, the main
menu. Four pages, each voiced and with its own image (the approved texts, rendered from the act's [data.yaml](data.yaml) `ending`, M11 part H, step H2; the images ART-H2's production):

<!-- data: act-ending-pages -->
| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-7-end-still`: the Steadfast's plot of the solar system, every Vrell contact from Neptune to Earth greyed out at once |
| 2 | Dr. Varga (grim) | `act-7-end-lock`: Varga's screen: the ring's glyphs laid out as a diagram, a lock's wards; beside it one file header of Vorne's flagged in red |
| 3 | Rook | `act-7-end-rook`: Rook on the Steadfast's flight deck beside two scorched Stormhawks, helmet under his arm |
| 4 | Commander Okafor | `act-7-end-dark-ring`: the final image: the dark ring from the near side, a listening buoy's small light before it, and on the buoy's screen a faint trace from beyond that is not the Choir's pattern |
<!-- /data -->

<!-- data: act-ending -->
> **Commander Okafor:** "Aegis, Okafor. When the Choir died, every Vrell swarm on our side of
> the gate fell still in the same minute, from Neptune to Earth. The war is over."
>
> **Dr. Varga:** "The gate was never a door, Commander. It was a lock, built to keep something
> out. The Vrell could not open it alone, so they found a hand on our side, and Vorne sold them
> his. Vorne's files called it the Silence. It is still out there."
>
> **Rook:** "Next time something knocks on that gate, Commander, we don't answer."
>
> **Commander Okafor:** "All ships, this is Okafor. Set course for home. One buoy stays at the
> ring, listening. Whatever is out there, it will not find us asleep. Aegis Actual out."
<!-- /data -->

Page 1 is the war's end (the Vrell on our side fall still with the Choir, told, never shown); page 2 is Varga's beat,
the lock, and the Silence named for the last time, never described; page 3 is Rook's one line (his near-side joke is
Level 50's, so this one differs); page 4 is the last order and the open ending's image: what the gate kept out is still
there, and somebody is listening. The Choir is silent on these pages: its last words are the Heart's. Lancer has no
face and no name here, as ever.

### Settings

[Vrell space](../../world/vrell-space/README.md#sub-locations-per-level): the gate approach (L43), inside the gate (L44),
a hive reef (L45), the dead fleet (L46), the Choir's inner sphere (L47), a Vrell-grown citadel (L48), the Choir's memory
(L49), the hive core (L50), in **one Vrell palette** with only the light changing per level (Q10 b, the world's light table) and every Vrell bullet larger,
brighter and outlined over the Vrell hues. All of Act 7 is `vacuum` (J12): no water, no weather.

### New mechanics

- **Window shields** (L43; Q2 a; [enemies](../../enemies/README.md#window-shields)): the Gate Warden's shell is
  invulnerable while closed and open from its line's telegraph to the beam's end; a `shield-breaker` hit or the EMP
  holds it open 3 s. The window is the Warden's alone (J4: no hex shields on new units).
- **Distorted space** (L44, the Heart's phase 2; Q1 a; [enemies](../../enemies/README.md#distorted-space)): the
  scroll's direction turns up to ±25° and twice reverses for 3–5 s, eased, told 2 s ahead by Rook and the HUD's
  [reversal chevron](../../ui/hud/README.md#act-7-additions-draft); units and bullets live in the drifting world
  frame, the ship's controls never rotate.
- **`teleport` and `mirror`** ([vocabulary](../../enemies/README.md#movement-pattern-vocabulary)): a told arrival ring,
  never inside 120 px (the Rift Skater) or 140 px (the Choir Seraph) of the ship; the Seraph then mirrors the ship's x
  for 2–3 s firing a spiral (Q3 a).
- **The gate transit** (L43's end, reversed in L50; Q11 a; [world](../../world/vrell-space/README.md#hazards--set-pieces)):
  a 1.5 s smooth warp toward the ring's centre and a chromatic shift to the event plane's violet, no flash.
- **Spore clouds and spawning pits** (L45; J13): clouds slow and dim but never hide a bullet, `area` burns them; pits
  open on a 2 s tell; the spawning organs (J16) fail the mission if one leaves alive.
- **The quiet** (L46; Q4 a): about 24 s in which nothing attacks, the music out, the Husks drifting harmless, a
  starless patch that never moves; then the Husks crack.
- **The boss rush** (L49; Q6 a; [bosses](../../enemies/bosses/README.md#boss-rush-l49-echoes)): three echoes in the
  spectral tint, one phase each, the armour, shield and special refilled and a checkpoint between them on every
  difficulty, the HUD's `ECHO n / 3`.
- **The leaving blink** (L43, L45, L47; [HUD](../../ui/hud/README.md#act-7-additions-draft)): one rule, the tracker
  blinks amber while a counted target is about to leave alive.
- **The campaign end** (Q12 a; [debrief](../../ui/debrief/README.md#the-campaign-end-act-7-draft)): the completed save,
  every mission open for replay, a completion mark in the high-score table.
- **Act 7's arsenal** (Q9 b; [Act 7 hand plan](../../player/generator/README.md#act-7-hand-plan-draft)): the
  [Bastion Shield](../../player/shields/README.md#bastion-shield-act-7) (L43), the
  [Vrell Swarm Call](../../player/specials/README.md#act-7-specials-draft) (captured in L45, from the L46 visit; its
  drones carry no FRIENDLY brackets) and the Mk VI "Choir Core" (30 MW, from the L47 visit); the [Choir Resonator](../../player/weapons/choir-resonator/README.md) as the one new weapon (kept in round 84, E.2 b;
  designed and approved in round 85): a front gun made from a Gate Warden's eye, lifted after L43 by the salvage tug and named by
  Varga in L44's briefing, in the shop from the L44 visit; its widening wave passes through the hulls it meets
  (`spread`, `piercing`). **The hand plan does not buy it** (round 85 Q.1 a, decided 2026-11-05): Levels 47 and 50 ask `beam`, which the
  plan's Ion Beam covers, so swapping the beam for the Resonator would only move the warnings from Levels 44 and 46
  to those two, and owning both before the Mk VI's L47 visit would push the Tesla Coil Pod's L5 and the Swarm Tail's
  L4 four visits later; E.1 a's warnings stand, and the Resonator is the player's choice (it clears L44's `spread`
  and L46's `piercing` for a player who flies it); the
  Airstrike and the Orbital Lance read `NO COVER` beyond the gate (J12).

### Factions present

The **Vrell** at home: the Choir's regulars, its breeding reef and citadel growth, its elite (the Choir Seraph under the
Heralds) and the hive itself; the hybrids (Chimera, Harrow) fly on. **Vorne** alone, the last of the Ascendancy, fused
with his lifeboat; Ascendancy craft appear only in his bays and in his memory (L49). The **Silence-touched** derelicts
(Husk), never the Silence. The Gate Wardens are the ring's own stone, not Vrell.

### Boss

| Boss | Level | HP (medium) | Phases | Paper | Bounty (Act 1 terms) |
|---|---|---|---|---|---|
| [Vorne's Chimera](../../enemies/bosses/vornes-chimera/README.md) (mid-boss) | 48 | 6,450 | 3: Ascendancy, Vrell, both with the capsule open | ≈ 67 s | 470 at 0.32 (15 % of budget(48)) |
| The [echoes](../../enemies/bosses/README.md#boss-rush-l49-echoes) (boss rush) | 49 | 10,320 · 6,300 · 8,500 | 1 each | ≈ 178 s the rush | 682 at 0.66 (≈ 42 % of budget(49)) |
| [Choir Heart](../../enemies/bosses/choir-heart/README.md) (act and final boss) | 50 | 19,900 | 3: membranes, arena, core | ≈ 162 s | 811 at 0.34 (24 % of budget(50)) |

The units an echo launched dissolve with it, unpaid. The Heart's death hands over to the closing gate, a scene with no
bar, no target and no fail (round 80's lesson).

### Units in the act

**New** (each its own document):

| Unit | Level | Tier | HP (medium) | Role |
|---|---|---|---|---|
| [Gate Warden](../../enemies/space/gate-warden/README.md) | 43 (50) | `medium` | 360 | the window shield; lines abreast with staggered windows; the 2 s leaving tell |
| [Rift Skater](../../enemies/space/rift-skater/README.md) | 44 (50) | `small` | 40 (beam era ×2.5, step B3's draft) | streams riding the drift, 3 jumps with a burst of 3 on landing |
| [Husk](../../enemies/space/husk/README.md) | 46 | `medium` | 150, cracks at 75 | a dead hull, harmless until hit, cracking into shards |
| [Choir Seraph](../../enemies/air/choir-seraph/README.md) | 47 (48, 50) | `medium` | 90 | the elite: jump, mirror, spiral, in pairs; the 0.8 s leaving tell |

**Returning** with an Act 7 note: the [Coilwyrm](../../enemies/air/coilwyrm/README.md) swirling with the drift (L44),
the [Threadcrawler](../../enemies/ground/threadcrawler/README.md) on the reef (L45), the
[Whirl Seed](../../enemies/air/whirl-seed/README.md) from the spawning organs (L45), the dying
[Leviathan](../../enemies/space/leviathan/README.md) (L46, J6: a set piece, its parts salvage). The rest of the Vrell
regulars, the hybrids, the reef's and citadel's turrets and, in Level 49, the remembered units of Acts 3 and 5 return as
built; the act's 34 units are counted in the [variety checklist](../../enemies/README.md#variety-checklist-per-act)
(all five tiers, 5 of 34 insectoid: the act passes).

**HP** (J11): the act's `reference_dps` runs 172 → 200, +4 a level (L43 172 … L50 200; the
[hand plan](../../player/generator/README.md#act-7-hand-plan-draft)'s table); a returning `medium` or larger unit takes
the act HP factor from them and a `small` unit the beam era's factor, ×2.5 from Level 43 (step B3's draft for the
Act 2+ floor, accepted by the user in round 89; ×2 before) ([enemies](../../enemies/README.md)). Level 49's
remembered units are scaled the same way from their own levels.

### Density and threat profile

Hand table from the level drafts (medium; the build measures them with `levelReport`; Levels 43 and 44 as built, step
B5's `levelReport` of 2026-11-05: the denser mixed waves of step B3b, 3.01 and 2.89 enemies on the screen; Levels 45 and
46 as built, step CD5's `levelReport` of 2026-11-06: the denser mixed waves of steps C2 and D2, 3.03 and 2.64 on the screen; Levels 47 and 48 as built, step EF5's `levelReport` of 2026-11-07: the denser mixed waves of steps E2 and F2, 3.00 and 2.77 on the screen; Levels 49 and 50 as built, step GH5's `levelReport` of 2026-11-08: the denser mixed waves of steps G2 and H2, 3.57 and 3.11 on the screen, the rates over the approaches). The act's curve is
**4 · 4 · 5 · 3 · 5 · 5 · 5 · 5**, the minimum **80 enemies a minute** (the breather may dip 20 % to 64), the levels
near 5 minutes ([campaign](../README.md#pacing-rules)); ≥ 60 % of waves from the front, ≥ 4 s calm after the last
enemy, the Act 2+ floor in every level.

| Level | Length (medium) | Enemies (a minute) | Front | Directions | Traits | Objectives | `bounty_scale` | Typical haul / budget |
|---|---|---|---|---|---|---|---|---|
| 43 The Tether Gate (built) | 268 s | 623 (≈ 142) | all | front | `forward`, `shield-breaker` | `WARDENS n / 16` | 0.19 | 12,016 / 12,001 (+0.1 %) |
| 44 Crossing (built) | 264 s | 648 (≈ 150) | 89 % (the side streams; the rear waves in the reversals) | all | `spread`, `rear` | kill ratio 65 % | 0.20 | 13,064 / 12,841 (+1.7 %) |
| 45 Hive Reef (built) | 278 s | 704 (≈ 155) | all | front | `anti-ground`, `area` | `ORGANS n / 8` (fail if one leaves); `CRAWLERS` 2 of 3 | 0.17 | 14,019 / 13,740 (+2.0 %) |
| 46 Graveyard of the Choir (built) | 268 s (the quiet 24 s) | 546 (≈ 125) | 78 % | all | `homing`, `piercing` | `CASKETS` 5 of 6 | 0.135 | 14,738 / 14,702 (+0.2 %) |
| 47 Seraph Gauntlet (built) | 266 s | 779 (≈ 179) | 90 % (the Seraphs' pairs from the front; sides and rear streams) | all | `beam`, `homing` | `SERAPHS n / 14` (3 may leave) | 0.17 | 15,740 / 15,731 (+0.1 %) |
| 48 Vorne's End (built) | 248 s + the fight (≈ 62 s, ≈ 5.2 min) | 549 (≈ 182; 32 ground) | 86 % (13 % rear) | front, rear | `rear`, `homing` | `CORES n / 8`; the mid-boss | 0.17 | 16,795 / 16,832 (−0.2 %) |
| 49 Echoes (built) | 287 s (≈ 90 s approach, the rush ≈ 178 s) | 241 on the approach (≈ 170) | 74 % (13 % rear) | all | `forward`, `rear`, `shield-breaker` (J9) | `KILLS n / 157`, then `ECHO n / 3` | 0.51 | 17,981 / 18,010 (−0.2 %) |
| 50 Choir Heart (built) | 427 s (≈ 182 s approach, ≈ 172 s Heart, ≈ 62 s scene) | 523 on the approach (≈ 172; 14 ground; step PT2 thinned the Last Chamber) | 88 % (5 % rear) | all | `beam`, `homing`, `area` (J9) | `KILLS n / 340`; the Heart | 0.22 | 19,096 / 19,271 (−0.9 %) |

The act's typical haul ≈ **123,900** against budgets of ≈ 123,100 (+0.6 %, every level as built: +0.1 %, +1.7 %, +2.0 %, +0.2 %, +0.1 %, −0.2 %, −0.2 % and +1.4 %; the drafts' sum was +0.8 %); the act factor **16.777** (1.6⁶) on every
payout. Level 48's secondary, scaled in its draft table, is paid unscaled as built (step F2), as the other levels'. The roster's "any balanced loadout" (L49) and "all" (L50) are not traits; J9 names them so
`TraitCoverageTest` can cover each level with one loadout. The hand plan's fit misses L44's `spread`, L45's
`anti-ground` (Rook's Mortar carries it) and L46's `piercing`: drafted as accepted warnings, as L39 and L40 (an open
question below).

### Music

The per-level cues are [music's *Cues per level (Act 7)*](../../audio/music/README.md#cues-per-level-act-7) (J8):
track 16 (the Act 7 A theme, the push: L43, L45, the L48 approach) and track 17 (the B theme, the strange places: L44,
L46, L47, the L50 approach), each with base, full and `choir` stems; L46 drops the music for the quiet; track 34
*Vorne's last stand* (L48); L49 the memory copies of tracks 4, 8, 12, 18 and 19; track 20 *Final boss* at full length
with its coda for the race (L50); track 27 *Ending* and track 28 *Credits* after Level 50. The beds are four
([sfx](../../audio/sfx/README.md#ambience-per-setting)): `vrell-gate` (L43, L44 with the `event-plane` layer),
`vrell-hive` (L45, L48, L50 with the heartbeat layer, L49's unmaking), `dead-fleet` (L46), `vrell-inner-sphere` (L47);
L49 the memory copies of `luna`, `mars-dust` and `belt-open`; L50's near side `belt-open`. The act's sounds are
[sfx's *Act 7 sounds*](../../audio/sfx/README.md#act-7-sounds-draft): real recordings from Freesound (CC0/CC-BY), the
tissue's sounds dry, no bubbly or popping sounds.

### Voice

Speakers (J3; [characters](../../story/characters/README.md), [voice](../../audio/voice/README.md#act-7-voices-draft)):
no new generic speakers. Okafor on the *Steadfast*'s bridge, Varga, Rook (every level, alive at the end), Vorne at L48
through the fused filter with the act's one shout (J7, the voice check's exception), and the Choir, far more than
before: sixteen lines, none repeated, on its own **clean** channel. The friendly radio's **noise** rises one step a
level from L43 to L47 and holds to L50, off on L50's near side; the subtitles are always clean.

## Concept art

Concept round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; decided 2026-11-05, round 84): the title card's still
a/b, Level 50's closing-gate scene and the ending pages' images; the gate transit and the one Vrell palette's light per level are in
[Vrell space](../../world/vrell-space/README.md#concept-art), the units' and bosses' sheets in their own documents.
Generator `tools/concept/act7_r84.py still closing ending`; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/act-7-still-r84-b.png](concept/act-7-still-r84-b.png) | Still B "the mouth": through the ring at the event plane's violet, the *Steadfast*'s group's lights strung toward it (960×540) | chosen (round 84 A.9 b) |
| [concept/act-7-still-r84-sheet.png](concept/act-7-still-r84-sheet.png) | Both stills and the title card `ACT VII / BEYOND THE GATE` over each (darkened, letterbox, chrome lettering) (sheet) | chosen (round 84 A.9: both stills under the title, b picked) |
| [concept/closing-gate-r84-a.png](concept/closing-gate-r84-a.png) | Closing gate A (the document's): the hive greying, the race through the dead chambers, the plane's rim contracting, the warp reversed, the near side with Neptune, the plane shrinking to a point, the glyphs going dark one by one; the luminance plot (storyboard) | chosen (round 84 A.10 a) |
| [concept/ending-pages-r84-a.png](concept/ending-pages-r84-a.png) | Ending pages A, the ideas as written: the plot with every Vrell contact greyed, Varga's lock diagram and Vorne's flagged file, Rook on the flight deck, the dark ring and the listening buoy (672×240 composition sketches) | chosen (round 84 A.11 a) |
| [concept/act-7-still-final-r89-a.png](concept/act-7-still-final-r89-a.png) | Production still (`tools/art/act_stills.py act-7`, M11 part B, step ART-B; round 89): A.9 b "the mouth" from Level 43's production backdrop (`backdrop_l43.py` `still_mouth`: the ring head-on, the event plane in its mouth, the *Steadfast*'s group's lights strung toward it), the title card over it with the chrome `ACT VII / BEYOND THE GATE` (`tools/art/act_titles.py`), 2× details (sheet) | chosen (round 89 F.6 accept) |

Round 95 (M11 part H, step ART-H2, 2026-11-07; production art, pending the user's review): the ending pages' four
images (A.11 a as written: `act-7-end-still`, `act-7-end-lock`, `act-7-end-rook`, `act-7-end-dark-ring`, 672×240 in
`assets/ui/briefing/`, `tools/art/briefing_images.py r95`) are on the
[briefing](../../ui/briefing/README.md#concept-art)'s sheet `briefing-images-final-r95-a`; the closing gate's
production kit (A.10 a) is [Level 50](level-50-choir-heart/README.md#concept-art)'s `closing-gate-final-r95-a`.

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Act data: the title card, the four intro pages (page 3 the Choir through the gate) and the `act-intro:7` flag
      for a `v0.4` save at the `COMING SOON` hangar (part B) (the act's [data.yaml](data.yaml), rendered above, and
      Act 6's `next_act` removed: step B2, 2026-11-05; the save's intro once: step B3; page 3 on the clean channel with
      track 16's motif: step B4; the full `check` green: step B5, 2026-11-05; round 89)
- [x] All 8 levels built from their drafts, each passing `DensityTest`, `ActFloorTest`, `PacingTest` (the end calm),
      `TraitCoverageTest` and `TypicalHaulTest` (±5 % of its budget) (Levels 43 and 44: M11 part B, steps B2–B5,
      2026-11-05; Levels 45 and 46: M11 parts C and D, steps C2, D2 and CD5, 2026-11-06; Levels 47 and 48: M11 parts E
      and F, steps E2, F2 and EF5, 2026-11-07; Levels 49 and 50: M11 part G, steps G2, G5 and G5b, and part H, steps H2 and FIXGH,
      2026-11-07/08; every level's last campaign pass is `ActPlaythroughTest` through L50, part I step I1, 2026-11-08)
- [x] Window shields, distorted space, `teleport`, `mirror`, spore clouds, the quiet, the gate transit, multi-arena
      levels with refills and checkpoints (part A, on fixtures) (built 2026-11-05: M11 part A and the transit's step B1; flown on fixtures; round 88)
- [x] The Bastion Shield, the Vrell Swarm Call and the Mk VI in the shop on their visits, and the Choir Resonator from
      Level 44 (its design, art and sounds first: step RES, round 85); the Act 7 balance plan and
      `reference_dps` 43–50; `ActPlaythroughTest`/`BalanceTest` through L50 (part A built their rows, the plan's visits 43–50 and `reference_dps` 43–50, 2026-11-05; part I step I1, 2026-11-08: every Act 7 level won first try on all difficulties, the time-to-kill sweep L46–L50 clean; the hard bound's miss is Act 6's deficit carried in, a pending check for round 96)
- [x] Vorne's Chimera (L48), the three echoes (L49) and the Choir Heart (L50) as their specs, each to the user's boss
      recipe (the Chimera: M11 part F, 2026-11-07, reworked in steps F5 and F6, 2026-11-07/08; the echoes in two stages: part G,
      steps G2, G5, G5b and G6; the Heart: part H, steps H1a, H1b, H2 and H4, 2026-11-08; the boss rush as L49's own
      climax; the fights themselves approved by the user in rounds 94 (the Chimera's rework and the rush) and 95 (the Heart), 2026-11-08)
- [x] The closing gate scene and the ending pages; the campaign end, the completed save, mission select all open
      (built 2026-11-08: part H steps H1b, H4 and ART-H2 for the scene, VOX-GH for the pages' voices, part A for the end
      screens and the save; part I step I3a pins L50 won in a campaign → the ending pages on track 27 → the campaign end and
      the completed save; round 95)
- [x] Music tracks 16, 17, 20 (full), 27, 28, 34 and the memory copies; the four beds and two layers; the voices
      (tracks 16 and 17 by section, the `vrell-gate` bed's stand-in and the `event-plane` layer, the intro's and
      Levels 43–44's voices: part B, 2026-11-05; the bed in round 89; track 16's duck and track 17's sparse drop, the
      `vrell-hive` and `dead-fleet` beds' stand-ins and Levels 45–46's voices: parts C and D, 2026-11-06; the beds in
      rounds 90 and 91; track 17's Choir statement as its own stem for Level 47, track 34 cut at K, the
      `vrell-inner-sphere` bed's stand-in, Levels 47–48's voices and Vorne's fused voice: parts E and F, 2026-11-07,
      rounds 92 and 93; track 20 at full length with its motif stem and coda: MUS-H, 2026-11-07, round 95; the five memory
      copies and the three memory beds: MUS-G and AUD-GH, 2026-11-07, round 94; Levels 49–50's voices: VOX-GH, 2026-11-08)
- [x] The variety checklist as built (34 units in the drafts; 36 with the spawning organ and the seed casket as
      built in parts C and D; 37 with the reactor core as built in part F; the enemies README's row 7 and its "Act 7
      passes it" item, M11 part I)
- [ ] The `v1.0` release: the whole campaign (part I)

## Open questions

None open: round 84's design questions, extra items and judgement calls were decided on 2026-11-05 (see *Decisions*).

## Decisions

- 2026-09-30: Act 7 has 8 levels, bringing the total to 50.
- 2026-09-30: Enemy variety pass: new units added to the level rows so the act passes the [variety checklist](../../enemies/README.md#variety-checklist-per-act).
- 2026-11-04 (Act 7 design phase, step m11-d-act; decided 2026-11-05, round 84): the act README written from
  the drafts as Act 6's: the roster rows replaced by the eight levels' Contents rows (the roster's L44 traits
  `spread`, `rear` and L48's `rear`, `homing` from the drafts); the intro (J1, J2: Okafor ×3, the Choir on page 3) and
  **the ending pages** in the act outro's place (Q8 a, Q12 a; the story step's beats); the mechanics, units, bosses,
  density and haul table, music and voice from the drafts. Trivia decided here: the missed captures go to the
  *Steadfast*'s salvage tug (Level 34's rule); Rook's ending line differs from his near-side joke; the Heart's
  checkpoints per phase, the Chimera's bullets fading at his death, the Seraph's HP 90, the secondaries' strictness and
  the plan's slack (no final money sink) stay as their documents draft them, not asked. The story's open question on
  the Silence is answered by the drafts: never shown, only hinted.
- 2026-11-04 (step m11-d-act): the cross-doc fixes the drafting steps reported applied (the Choir Heart's lines 1 and 3
  and bounty 811, the Chimera's line 6 and bounty 470, the rush at 0.66 and the dissolve rule, Level 43's fleet-split
  wording, the leaving tells and the HUD's shared blink, the variety row recounted to 34, the levels' beds and cues as
  D-audio's, the tissue's sounds dry).
- 2026-11-05 (step m11-art-r-b, decided 2026-11-05, round 84): concept art proposed: the title-card still a/b (a from Level 43's ring, J1; b "the mouth" chosen in round 84), the closing gate a/b, the ending pages a/b.
- 2026-11-05 (user, [round 84](../../concept-rounds/round-84/README.md) closed; page answers): **Questions Q1–Q9, Q11
  and Q12 as recommended and drafted** (Q9 b, the rest a): Q1 the scroll turns ±25° and twice reverses (b visual only, c
  reversals only rejected); Q2 the Gate Warden's shell invulnerable while closed (b hex pool, c mirror rejected); Q3 the
  Choir Seraph teleports and mirrors in pairs (b teleport only, c mirror only rejected); Q4 24 s of quiet with a still
  starless patch (b the patch moves, c radio only rejected); Q5 Vorne's Chimera an air craft grown from the lifeboat in
  three phases (b rooted in the citadel, c two stages rejected); Q6 the boss rush of the Brood Carrier, the Dust
  Colossus and the Iron Sovereign (b the Abyssal Maw, c two echoes rejected); Q7 the Heart in three phases (b a medley,
  c two phases rejected); Q8 the gate closes, the race back, the ring dark, everyone alive (b someone stays, c the gate
  stays open rejected); Q9 b the Bastion, the Swarm Call and the Mk VI (a everything, c the Bastion and the Mk VI only
  rejected); Q11 a 1.5 s smooth warp (b the 0.5 s inversion, c a cut to a still rejected); Q12 the end card, credits
  and Continue to mission select with every level open (b the L50 hangar again, c New Game+ rejected). **Q10 b** (not
  the recommendation): Vrell space is **one Vrell palette** for all eight levels (black-violet to violet to teal to
  mint, as `vrell-moods-r84-b.png`), only the light changing from level to level; option a's mood per level and c are
  rejected, and every level's look names the palette, not a mood colour (see the
  [world](../../world/vrell-space/README.md#palette--lighting)). Things that are not Vrell keep their own colours, lit by the Vrell
  light (the dead fleet's hulls in L46, the Ascendant's gold grafts on the Chimera, Level 49's remembered planets under
  the echo tint). **E.1 a** (the plan's trait warnings in Levels 44–46 accepted as hangar warnings, as L39 and L40);
  **E.2 b** (not the recommendation): the **Choir Resonator is kept** as Act 7's one new weapon, on sale from Level 44
  (a front spread/piercing gun made from captured Vrell tech); the "no new weapon" drafts are changed in this act, the
  weapons roster, the player and campaign READMEs and the roadmap; its design is step RES in round 85, and once designed
  it may cover Level 44's `spread` and Level 46's `piercing` (step RES decides); **E.3 a** (the race through the closing
  gate is a scene with no fail); **E.4 a** (the Heart's second phase all at once); **E.5 a** (the Seraph's arrival ring
  violet only). **Judgement calls J.1–J.16 all kept** as drafted. **Art:** A.9 b: the act's title still is **"the mouth"**
  (the gate ring head-on and centred, its mouth filled with the event plane's violet-white, the *Steadfast*'s group
  strung in a line toward it); A.10 a (the plane shrinks and goes out) and A.11 a (composition sketches) chosen, the
  alternatives rejected. **Music:** M.1 a and M.3 a and M.4 change requested (round 85 reworks them, see the music
  README); **voice:** V.1 a, V.2 b (see the voice README).
- 2026-11-05 (step RES, round 85; approved by the user, round 85 Q.2): the Choir Resonator's draft
  ([its document](../../player/weapons/choir-resonator/README.md)): captured from the Gate Wardens' eyes after L43
  (the salvage tug, no pickup), named by Varga in L44's briefing, in the shop from the L44 visit; the hand plan keeps
  the Ion Beam and does not buy it (L47 and L50 ask `beam`; the money holds no second front gun before the Mk VI);
  the user decided in round 85 (Q.1 a) that it does not.
- 2026-11-05 (user, [round 85](../../concept-rounds/round-85/README.md) closed): **Q.1 a** the hand plan keeps the Ion Beam
  and does not buy the Choir Resonator at Level 44; **Q.2 approve**: the Resonator's document and Level 44's added
  lines are approved; **A.1 a** "the bell" is its look. The Act 7 music re-takes (tracks 16, 34 and 27) and the
  Resonator's sounds go back to round 86 (see the music and sfx READMEs).
- 2026-11-06, round 89: F.6 accepted ("the mouth", the act still and title card). C.1 accepted (Levels 43 and 44 denser with more kinds of enemy; hard is much harder: on Level 43's hard the autopilot kills all 18 Wardens in 1 of 6 runs). C.3 accepted (Act 7's small units have 2.5× their base HP, and a reversal counts as a scripted pause in the pacing check).
- 2026-11-08 (user, [round 94](../../concept-rounds/round-94/README.md), [round 95](../../concept-rounds/round-95/README.md) and [round 96](../../concept-rounds/round-96/README.md) closed): Levels 49 and 50, the three echoes, the reworked Chimera (closing round 93's L.3 follow-up), the Choir Heart, the ending and the end screens accepted as built (Level 49 and 50 `art: chosen`); the two heartbeats (the Heart's and the hive's) are redone in round 97; the user plays Act 7 on the macM1 bundle (pending, their run); the `v1.0` release item stays open for that run, PT-FIX, the golden flights and the tag.
