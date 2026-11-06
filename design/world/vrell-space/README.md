---
title: Vrell space
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-7-beyond-the-gate, ../../enemies, ../../player/specials, ../belt]
updated: 2026-11-06
---

# Vrell space

## Summary

The Tether Gate and what lies beyond it: space that is alive — nebulae of spores, hive
structures the size of moons, and dead Vrell worlds that something emptied. Setting for Act 7,
ending with the Choir Heart. Its eight levels each get a sub-location and a light of their own, all in one Vrell palette; the gate transit,
distorted space, spore clouds, living terrain and the quiet are its hazards and set pieces (draft, decided 2026-11-05, round 84).

## Design

### Look & feel

Beautiful, alien and wrong. The Tether Gate is a vast ring of dark stone-like material with
glowing glyphs. On the far side: violet and teal nebulae, bioluminescent hive-reefs, living
bridges between asteroids, the Choir Heart pulsing at the centre. The further in, the more
signs of death: withered hives, dead hulls, silence. Touchstones: *Event Horizon*, H.R. Giger
(toned down), R-Type's organic stages, Darius's giant bio-bosses.

### Sub-locations

| Sub-location | Description |
|---|---|
| The Tether Gate | The ring itself: flying along its surface and through the event plane. |
| Spore nebula | Dense clouds of glowing spores; hive-reefs. |
| Hive-reef | Moon-sized living structures: flying over and inside them. |
| Dead worlds | Withered Vrell worlds and the dead fleets drifting before them — the hint of the Silence. Eerily quiet levels. |
| The inner sphere | The Choir's own ribbed shell of Vrell stone round its core, lit gold-white from within; its elites' ground. |
| The Choir Heart | The core organism of the hive mind; final battle. |

### Sub-locations per level

Act 7's eight levels in order ([the act](../../campaign/act-7-beyond-the-gate/README.md)); the level documents own the
sections and the timings, these are their settings (draft, step D-enemies-B, 2026-11-04, decided 2026-11-05, round 84).

| Level | Sub-location | What the level shows of it | Ground layer |
|---|---|---|---|
| 43 The Tether Gate | Gate approach | The moon-sized ring filling `deep` as the wing closes on it, its stone and glyphs; the CDF fleet's running lights behind at the start (the line ships that hold the gate's near side; only the *Steadfast*'s group, without bombers, crosses); in the last third the wing flies **along the ring's surface**; the level ends in the **gate transit** | the ring's surface, a strip in the last third with Level 43's **glyph stones** on it (before it, none) |
| 44 Crossing | Inside the gate | The **event plane**: the nebula at the palette's lightest (pale violet to mint) with dark violet streaks, its layers sliding against each other in **distorted space**; no surface, no stars | **none** (`NO GROUND`) |
| 45 Hive Reef | Hive reefs in the spore nebula | A moon-sized reef seen from just above it: teal-black and violet chitin plates, glowing mint-teal veins, **spawning pits** and the **spawning organs**; **spore clouds** drifting over; living bridges to the next reef on `low-air` | the reef, from t = 0 (a top-down ground level in space) |
| 46 Graveyard of the Choir | Dead fleet before a dead world | A field of the dead fleet's hulls (their own colours, lit by the Vrell light), hollowed from inside, pale crystal along their cracks; a withered world in the palette's darkest violet on `deep`; **the quiet** and its **starless patch**; the **dying Leviathan** drifting through | **none** (`NO GROUND`: the hulks are scenery on `deep` and `low-air`) |
| 47 Seraph Gauntlet | The inner sphere | The curved inside of the Vrell-stone shell far below and around, ribbed and lit gold-white from the core ahead; Choir Heralds' rings of light | **none** (`NO GROUND`) |
| 48 Vorne's End | The Vrell-grown citadel | Teal-black Vrell growth over the gold wreckage of what the Ascendancy brought through, grown into towers and decks; the arena of [Vorne's Chimera](../../enemies/bosses/README.md) at its heart | the citadel's grown decks |
| 49 Echoes | The Choir's memory | Each echo's own world as the Choir remembers it: **Luna** for the Brood Carrier, **Mars** for the Dust Colossus, **Ceres** for the Iron Sovereign, every one in the spectral tint and **dissolving into tissue** at its edges; between the echoes the memory melts into living tissue and the next one forms | per echo: its original arena's (the Colossus's breach floor; the boss documents decide) |
| 50 Choir Heart | The hive core | Dark tissue chambers round the Heart, lit only by its pulse; tissue walls the boss reshapes (the [boss document](../../enemies/bosses/README.md)'s arena); after the Heart, the **closing gate** (the ending's scene) | the Heart's tissue floor and walls (the boss document's) |

### Parallax layers

| Layer | Contents |
|---|---|
| deep | Nebula clouds, distant hive-reefs, strange stars; the Tether Gate's ring (L43); the event plane's bright nebula (L44); a withered world and **the starless patch** (L46; the world in the palette's darkest violet); the inner sphere's far shell (L47); the remembered skies (L49). |
| ground | Hive surface: chitin plates, glowing veins, spawning pits, living turrets (L45); the ring's surface (L43's last third); the citadel's grown decks over gold wreckage (L48); the echoes' arenas (L49); the Heart's tissue (L50). |
| low-air | Tendrils, floating spore sacs, small organisms drifting; living bridges (L45); dead hulls and debris (L46); the Heralds' light rings (L47). |
| air | Player's plane (`space` in vacuum, as in [the belt](../belt/README.md#vacuum-rules)). |
| high-air | Spore clouds and tendrils passing over the player; the dying Leviathan's pass (L46). |

### Media per level

*Draft, judgement call **J12**, step D-enemies-B (2026-11-04), decided 2026-11-05, round 84.*

| Levels | Medium | Rules |
|---|---|---|
| 43–50 | **vacuum** (Vrell space has no air and no water) | The belt's [vacuum rules](../belt/README.md#vacuum-rules): explosions flash short and throw debris in a ring, wrecks do not burn; no weather. Vrell matter (spores, ichor, spawn) spreads and fades; it never rises as smoke. |

- **Specials:** every Act 7 level is **beyond the gate**: the Airstrike and the Orbital Lance read **`NO COVER`**, and
  the Sonar Pulse `NO WATER` (the [specials](../../player/specials/README.md#act-7-specials-draft)' setting list; the
  hangar warns from the L43 visit).
- **Ground-only weapons** (the Bomb Rack and every bomb delivery) read **`NO GROUND`** in L44, L46 and L47, the levels
  without a ground layer (Level 30's rule; the hangar warns); L43 has its ring strip, L49 and L50 their arenas.
- **Sounds** play as everywhere (the game's convention: no silent vacuum); L46's quiet drops the music, not the sounds
  ([music](../../audio/music/README.md)).

### Palette & lighting

- **One Vrell palette for all eight levels** (Q10 **b**, decided 2026-11-05, round 84; the user's pick over a mood
  colour per level): round 06's black-violet, violet, teal and pale mint, as in `vrell-moods-r84-b.png`. Vrell stone,
  tissue, glyphs, nebulae and light are all in it. Only the **light** changes from level to level: its source, its
  brightness and how it moves (the per-level table below). No level gets a colour of its own (no pink reef, grey
  hulks, pale bone or inverted plane).
- **What is not Vrell keeps its own colours, lit by the Vrell light**: the dead fleet's hulls in L46, the gold
  wreckage under Level 48's citadel and the Ascendant's gold grafts on the Chimera, the CDF fleet's lights, and Level
  49's remembered planets (Luna, Mars, Ceres) under the echo tint of the spectral grade (the
  [bosses](../../enemies/bosses/README.md#boss-rush-l49-echoes)' tint).
- No clear sun: light comes from bioluminescence and the nebula glow, so the top-left key
  light becomes a soft violet glow (keep the direction for consistent sprites).
- Danger: Vrell bullets are the same hues as the background here — make them larger, brighter
  and outlined (see [art-direction](../../art-direction/README.md) and the
  [bullet rules](../../enemies/README.md#bullet-readability-rules)).
- Dead worlds: the withered world and hulls of L46 are lit by a single cold white light; the palette around them is unchanged.
- **Smooth haze** (the Act 5 lesson, the 2026-10-30 dither audit, as Act 6's): every nebula, spore cloud, glow, haze
  and shadow layer is a smooth gradient of **≥ 32 steps**, no ordered dither, drawn once (not stacked), and every glow
  fades to 0 inside its sprite's canvas (no clipped edges).

#### Light per level (Q10 b)

*Decided 2026-11-05, round 84 (Q10 **b**, not the recommendation): one Vrell palette, each level keeping only its own
light, with a readability check (the ship, Rook, the Vrell's units and their larger, brighter, outlined bullets laid
over the level's brightest and darkest band; the sheet is `vrell-moods-r84-b.png`).*

| Level | Light | Readability note |
|---|---|---|
| 43 | The gate's dark stone and cold teal-white glyphs (Vrell stone and glyphs, in the palette) under the violet and teal nebula; the fleet's white running lights behind | The [Gate Warden](../../enemies/space/gate-warden/README.md)s are the gate's own stone: the ring is kept a step darker and less saturated than the units, and its glyphs never pulse in the colour or rhythm of a Warden's `laser-line` telegraph |
| 44 | The event plane: the palette at its lightest, pale violet to mint with dark violet streaks, layers sliding; the act's one light field | The brightest band stays below a bullet core's brightness; the bullets' dark outline and the ship's dark hull carry the read; the reversal chevron's violet-white sits on its dark outline ([HUD](../../ui/hud/README.md#act-7-additions-draft)) |
| 45 | Teal-black and violet chitin, glowing mint-teal veins and the pits' glow; spore clouds violet | Veins pulse slowly and are never bullet-sized dots; the reef is a step darker than the units on it; the clouds tint, never cover, the play plane (*Spore clouds* below) |
| 46 | One cold white-violet light from the top-left on the dead fleet's hulls (their own colours); the world beyond in the palette's darkest violet; the starless patch; pale crystal at the hulls' cracks | The Husks are a step darker and higher in contrast than the scenery hulks (the dead hulls are paler and flatter), so the living threat reads; their crystal shards keep the dark outline against the cold white |
| 47 | The inner sphere's Vrell-stone ribs lit gold-white from the core ahead: the act's richest light | The [Choir Seraph](../../enemies/air/choir-seraph/README.md)'s ivory over the shell: the shell is kept darker and in the palette, the Seraph's gold-white glow and the bullets' outline carry it; warp-in rings violet, never gold |
| 48 | Teal-black Vrell growth (palette) over the dull gold wreckage (its own colour), the Chimera's gold grafts and teal tissue | Gold-on-gold (Act 6's rule): the wreckage is a step paler and less saturated than the boss's gold; teal bullets over teal growth are the larger, brighter Vrell bullets |
| 49 | The earlier acts' scenes (Luna, Mars, Ceres: their own colours) under the echo **spectral tint** (desaturated toward a cold violet-white), their edges dissolving into the palette's dark tissue | The echoes in the bosses' [spectral tint](../../enemies/bosses/README.md#boss-rush-l49-echoes) (their own colours desaturated ≈ 40 % toward violet-white), a step brighter than their tinted worlds, and their bullets as built; the tint never reaches the bullets |
| 50 | The palette's near-black violet tissue lit by the Heart's slow red-violet pulse (≤ 1 Hz, smooth, never a flash) | The pulse's brightest stays below the bullets' cores; the walls the boss moves keep a lit edge so the field's shape reads in the dark ([Act 4](../../campaign/act-4-deep-water/README.md)'s darkness lesson) |

### Hazards & set pieces

*Draft, step D-enemies-B (2026-11-04), decided 2026-11-05, round 84; the levels place them, the
[enemies](../../enemies/README.md) own the units' rules. The spore clouds' numbers the game reads live once in
[data.yaml](data.yaml) (M11 part A, step A2b); a level places its clouds (`spores`).*

| Hazard / set piece | Levels | Rules (draft) |
|---|---|---|
| **Gate transit** (Q11 a, signature set piece) | L43's end | After the last enemy and **≥ 4 s of calm**, the ring's event plane fills the field ahead; the ship and Rook fly in formation, scripted (no input, no hits; the [wingmen](../../player/wingmen/README.md#rook-in-act-7-draft)' rule). Then a **1.5 s warp**: the whole image bends smoothly toward the ring's centre (a radial pull, strongest at the centre) while its colours shift in one smooth grade to the event plane's violet. The brightness changes in one direction only: **no frame-to-frame flashing and no colour inversion** (a photosensitivity rule). The level ends inside it (the debrief over the violet); Level 44 opens in the same violet, the warp unbending over 1.0 s on the far side. A renderer post-effect. |
| **Distorted space** (Q1 a) | L44; the [Choir Heart](../../enemies/bosses/README.md)'s phase 2 | Inside the gate the scroll's **direction turns** sideways and twice **reverses**. The vector's limits and how the ship, units, bullets and pickups ride it are the enemies' [*Distorted space*](../../enemies/README.md#distorted-space); the level sets its profile; each reversal is told 2 s ahead by Rook's call and the [HUD's reversal chevron](../../ui/hud/README.md#act-7-additions-draft). **The backdrop:** every layer slides along the scroll's direction at its own parallax factor (`deep` least), so in a drift the layers slide against each other, and in a reversal they ease to a stop and run back together; the event plane also warps slowly and smoothly (no flicker). The screen never rotates. **Easy** flies a gentler profile: at most **±15°** and **one reversal a minute**; hard as medium. |
| **Spore clouds** (J13) | L45 | Soft clouds of glowing violet spores, 120–200 px across, drifting with the scroll on `high-air`; at most two on the screen. While the ship's centre is inside one it flies at **0.7 of its speed and acceleration** (the [Siren](../../enemies/naval/siren/README.md)'s slow at 0.7, easing in and out over 0.3 s; Rook the same), and the backdrop under the cloud dims a step. A cloud **never hides bullets**: bullets, the ship, Rook and pickups are always drawn above it (the [bullet rules](../../enemies/README.md#bullet-readability-rules)); a play-plane unit inside one shows under a veil of **at most 25 %**, its tells and glow at full. An **`area` hit** (a blast whose radius touches it, Rook's Mortar too) or the **Plasma Arc** burns a cloud away in **1 s** (an ember edge eating inward); other shots pass through. A cloud pays nothing. Never two clouds overlapping, and none resting over the ship's band (the bottom 140 px) longer than 2 s. The sketch's "hide bullets" is dropped (J13: hiding bullets is unfair, Act 6's lesson). |
| **Living terrain** | L45 | The reef breathes: **spawning pits** are the reef's mouths, scenery that cannot be shot. A pit tells **2 s**: its lips glow and part; then it opens and a level-authored wave rises out of it (Skitters, Mote Swarms; 0.5 s from the ground's scale to the play plane, harmless while rising), and it closes 3 s later. A pit whose opening would put a rising unit **within 120 px of the ship** holds closed until it would not. The **spawning organs** are Level 45's own `destroy-targets` targets on the reef ([Hive Node](../../enemies/ground/hive-node/README.md) rules, not hardened; the level's document, J16), releasing [Whirl Seed](../../enemies/air/whirl-seed/README.md#act-7-the-hive-reefs-organs-draft) clusters and Skitters while on the screen. |
| **The quiet** (Q4 a) | L46 | About **24 s** (Level 46's t=120–144) in which nothing attacks: the music drops out for a graveyard ambience, [Husk](../../enemies/space/husk/README.md)s drift across harmlessly (they fire only once damaged, so the screen stays busy for the pacing rules), the hulls show they were **hollowed from inside** (pale crystal along the cracks), and the **starless patch** sits on `deep`. Then the Husks crack and the Vrell's remnants attack. Varga's one line plays in it (the level's). The **starless patch**: a soft-edged region (about a quarter of the field's width) where no star and no nebula shows; it is **the only thing in the level that does not scroll** (it holds its place on the screen for the whole level while the stars drift into it and go out). It never moves, grows or shows anything: the Silence is **hinted, never shown**. Smooth edge (≥ 32 steps). |
| **The dying Leviathan** (J6) | L46 | A [Leviathan](../../enemies/space/leviathan/README.md#act-7-the-dying-leviathan-level-46-draft) drifts through the dead fleet during the quiet, on `high-air`: a set piece, not a fight. Its parts are scenery that pays salvage (the unit's document). |
| **Hollowed hulls** | L46 | The dead fleet's hulls on `deep` and `low-air` are scenery (no hit boxes), torn open from the inside outward, pale crystal along the edges; no fire, no light inside. |
| **The memory** | L49 | The echoes' worlds form and dissolve (*Sub-locations per level*); the fights and their refills are the [bosses](../../enemies/bosses/README.md)'. |
| **The closing gate** | after the Heart (L50) | The ending's scene (Q8): the level's and the story's documents. |

The numbers stay in this prose until the build (M11's parts move them into a `data.yaml` here, as the Jovian world's).

### Natives

- Vrell: every kind, including the inner sphere's elites ([Choir Seraph](../../enemies/air/choir-seraph/README.md)) and the
  returning ones ([Coilwyrm](../../enemies/air/coilwyrm/README.md#act-7-crossing-draft) in L44,
  [Threadcrawler](../../enemies/ground/threadcrawler/README.md#act-7-the-hive-reef-draft) in L45,
  [Whirl Seed](../../enemies/air/whirl-seed/README.md#act-7-the-hive-reefs-organs-draft) in L45); hive-structures as
  terrain.
- Silence-touched derelicts: the [Husk](../../enemies/space/husk/README.md)s (L46), the hint's carriers.
- Ascendancy: the faction is broken; only Vorne remains, fused with his lifeboat into
  [Vorne's Chimera](../../enemies/bosses/README.md) (L48), its bays the last Ascendancy craft.

### Music mood

Alien and overwhelming, then desolate. Choir voices, reversed textures, the main theme
transformed; the dead worlds nearly silent (L46's quiet drops the music for an ambience bed); the final battle all
themes at once. The tracks are the [music](../../audio/music/README.md)'s.

### Chosen scene

The look of this setting is set by the chosen parallax scene in art direction: Vrell space (round 06) —
[sheet](../../art-direction/concept/scene-vrell-space-r06-a.png), [scroll loop](../../art-direction/concept/scene-vrell-space-r06-a.gif).
The concept files live in [art-direction](../../art-direction/README.md).

## Concept art

The chosen scene (round 06, in [art direction](../../art-direction/README.md#concept-art)) sets the look; see *Chosen
scene* above.

Concept round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; decided 2026-11-05, round 84): Q10's moods per level with
the readability windows, one echo in the spectral tint over Level 49's memory, and Q11's gate transit, a and b each.
Levels 43–47's backdrops are step ART-R-A's stand-ins (`tools/concept/enemies_r84.py`) so the units' sheets and these
agree. Generator `tools/concept/act7_r84.py moods echo transit`; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/vrell-moods-r84-b.png](concept/vrell-moods-r84-b.png) | Moods B, one Vrell palette (Q10 b): the same scenes through round 06's violet and teal, each keeping only its light (sheet) | chosen (round 84 Q.10 b) |
| [concept/vrell-echo-r84-a.png](concept/vrell-echo-r84-a.png) | Echo A: the Brood Carrier's production hull in the spectral tint as worded (desaturated about 40 % toward violet-white, a step brighter, translucent edges fading to 0 inside its canvas), its bullets as built, over Luna remembered; the memory melting into tissue and Mars forming between echoes (sheet) | chosen (round 84 A.8 a) |
| [concept/gate-transit-r84-a.png](concept/gate-transit-r84-a.png) | Transit A (Q11 a): the ring's mouth ahead, the formation, the 1.5 s warp bending toward the centre while grading to violet, the debrief over the violet, Level 44 opening; the mean luminance per frame rising one way only (storyboard) | chosen (round 84 Q.11 a) |
| [concept/gate-transit-r84-a.gif](concept/gate-transit-r84-a.gif) | Transit A at 1×, from the formation to Level 44's first second | chosen (round 84 Q.11 a) |

Round 88 (M11 part A, step ART-A): a **stand-in** spore cloud for the systems build (Level 45's own art is part C's),
by `python3 tools/art/act7_gear.py spore` (held in `build/art-held/sprites/` until `SpriteUse` names the root):
`spore-cloud-veil` (normal, the palette's dark violet at most 25 %), `spore-cloud_0..7` (additive, a 4 s loop) and
`spore-cloud-burn_0..9` (the 1 s ember edge eating inward), 176×176 for a cloud of radius 64 (scaled per cloud).
Prompts: [concept/prompts.md](concept/prompts.md#spore-cloud-final-r88-a).

| File | What | Status |
|---|---|---|
| [concept/spore-cloud-final-r88-a.png](concept/spore-cloud-final-r88-a.png) | The veil over a checker, the cloud's eight frames, the burn's ten; 1× over Level 45's reef in the one Vrell palette: the ship inside a cloud, a second cloud burnt away (sheet) | chosen (round 88 F.6 accept) |
| [concept/spore-cloud-final-r88-a.gif](concept/spore-cloud-final-r88-a.gif) | The same, 1×, 3 s | chosen (round 88 F.6 accept) |

Round 90 (M11 part C, step ART-C, 2026-11-06; production art, pending the user's review): the spore cloud's **production
art** replacing the stand-in, same sprite keys and sizes (the game's `SporeLooks` draws it unchanged), by
`python3 tools/art/act7_reef.py spore` (the stand-in's part in `act7_gear.py` is retired): eight soft billows rolling
through a 4 s loop with fine pale motes, the veil at most 25 %, the burn's ragged ember edge with sparks drifting off.
Prompts: [concept/prompts.md](concept/prompts.md#spore-cloud-final-r90-a).

| File | What | Status |
|---|---|---|
| [concept/spore-cloud-final-r90-a.png](concept/spore-cloud-final-r90-a.png) | The veil, the cloud's eight frames over it and the burn's ten over Level 45's production brood fields; 1×: the ship inside a cloud, a second cloud burnt away, a Vrell bullet above them (sheet) | chosen (round 90 F.4 accept) |
| [concept/spore-cloud-final-r90-a.gif](concept/spore-cloud-final-r90-a.gif) | The same, 1×, 3 s | chosen (round 90 F.4 accept) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Open questions

None open: round 84's items were decided 2026-11-05 (see *Decisions*).

## Decisions

- 2026-09-30: Vrell space as Act 7; signature: transit through the Tether Gate.
- 2026-10-01: Linked the chosen scene from art direction; `art: chosen`.
- 2026-11-04 (Act 7 design phase, step D-enemies-B; decided 2026-11-05, round 84): the sub-locations per level
  (an inner-sphere sub-location added for L47), the media per level (J12: all `vacuum`, beyond the gate), the moods per
  level (Q10 a), the hazards and set pieces (the gate transit Q11 a, distorted space Q1 a, spore clouds J13, living
  terrain, the quiet Q4 a, the dying Leviathan J6) drafted.
- 2026-11-04 (decided by me, trivia): the sketch's "spore clouds hide bullets" and the transit's "colours invert
  briefly" are replaced (unfair, and a photosensitivity risk); spore clouds slow to 0.7 (the Siren's slow block, the
  gap analysis's "thrust −30 %"), veil at most 25 %, burnt in 1 s; spawning pits are unshootable scenery with a 2 s
  tell and a 120 px rule (as the teleport's distance); the starless patch holds still on the screen (the one thing
  that does not scroll) and never changes; the dying Leviathan passes on `high-air` during the quiet; L49's worlds
  Luna, Mars and Ceres (the echoes' own acts); `NO GROUND` in L44, L46 and L47; distorted space at easy ±15° and one
  reversal a minute.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the fleet split at the gate in the Level 43 row (the story step's
  wording), the glyph stones on the ring's surface named, Level 49's tint worded as the bosses' spectral tint (own
  colours desaturated ≈ 40 % toward violet-white).
- 2026-11-05 (step m11-art-r-b, decided 2026-11-05, round 84): concept art proposed: the moods per level a/b, the echo a/b, the gate transit a/b.
- 2026-11-05 (user, round 84 closed; page answers): **Q10 b** (not the recommendation): one Vrell palette (black-violet,
  violet, teal, mint) for all eight levels, only the light changing; a mood per level (a) and c rejected, the sheet
  `vrell-moods-r84-a.png` rejected. The per-level mood wording (the pink reef, grey hulks, pale bone, the inverted event
  plane) is rewritten here and in the Level 43–50 documents; things that are not Vrell keep their own colours, lit by
  the Vrell light. **Q1 a**, **Q4 a** and **Q11 a** as drafted (b and c rejected). **A.8 a**: the echoes in the
  spectral tint, soft transparent edges (b "afterimage" rejected). **Q11 a**: the transit sheet and clip a chosen, b
  rejected.
- 2026-11-05 (M11 part A, step A2b): the spore clouds' numbers in [data.yaml](data.yaml) (`spores`: the slow 0.7 eased
  over 0.3 s, the burn 1 s, at most 2, the veil 25 %, the drift at the `high-air` factor (1.7, or the level backdrop's),
  a radius of 60–100 px, the ship's band 140 px for at most 2 s), read and checked by the content (trivia: the radius
  range from the rule's 120–200 px across; accepted by the user in round 88).
- 2026-11-06, round 88: F.6 (the spore cloud's art) accepted; the user's note on F.6 and F.7, "I do see a very hard line in the background. Is that a bug in the capture?", was a capture-rig artefact, not a game bug (part A's rig mixed Level 43's and 44's tiles on Level 42, meeting at a hard seam; Level 44's own backdrop has no seam). The rig's `drift-capture-r88-a` and `spore-capture-r88-a` are rejected (rig seam; superseded by round 89's Level 44 captures) and moved to `concept/rejected/`; the chevron design is kept. C.4 accepted (the Bomb Rack's blasts burn a spore cloud like any other blast). C.7 accepted (breaking Level 46's quiet has no special look or sound: the Husks simply wake). Round 89: C.5 accepted (the gate ring's glyphs are teal-white, as this document says, not crimson as the act's brief said).
- 2026-11-06, round 90: F.4 accepted (the spore cloud and its burn over the brood fields, at 1:1).
