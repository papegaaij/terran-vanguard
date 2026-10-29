---
title: Act 5 – The Belt
design: approved
implementation: in-progress
art: chosen
depends-on: [../act-4-deep-water, ../../world/belt, ../../story/twist, ../../story/characters/vorne, ../../enemies/bosses/iron-sovereign, ../../player/generator]
updated: 2026-10-29
---

# Act 5 – The Belt

## Summary

Levels 29–35. Vorne's broadcast splits the solar system, and the Jovian Ascendancy fights in the open
beside the Vrell to take the asteroid belt and cut Earth off from the outer system. Aegis Wing flies into
Ceres Hub as it turns sides and frees Rook from a Helix prison tender, then clears a mined shipping lane,
escorts a tug of refugees, raids a refinery inside a hollowed asteroid, crosses a leech field that drains
the generator, salvages a Helix generator from a ship graveyard and breaks the Iron Sovereign, the
battle station holding Ceres. For the first time the enemy is human-built: shielded Talons, plated
Gunships, minelayers, drone trains and fortified stations, in gunmetal, charcoal and gold.

**Status: approved (Act 5 design phase, 2026-10-27), approved by the user in concept round 60.** The
seven level documents, the units, the boss and the systems were drafted in the design phase's first three
waves from the gap analysis's recommended answers (Q1–Q12, the judgement calls J1–J3; a local working
note, `m9-dgap-gaps.md`); this README pulls them together (the act intro and outro, the units and their HP
factors, the density and haul table, the threat profile, the music and the voice) as
[Act 4's](../act-4-deep-water/README.md) did. There is **no `data.yaml`** under the campaign or the enemies
for Act 5 yet: every number here is a hand table marked draft, taken from the level drafts, and the build
(M9, [roadmap](../../tech/roadmap/README.md#m9-parts)) turns them into data and measures them.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [level-29-betrayal-at-ceres](level-29-betrayal-at-ceres/README.md) | Ceres Hub's dock canyon: the station turns sides at t=60, the wing breaks the clamps at Dock 9 and Rook returns; Talon, SAM Nest, Asteroid Mite; the "Hold fire" secondary | approved | in-progress | final |
| [level-30-minefield](level-30-minefield/README.md) | The shipping lane, space only: Minelayers from behind lay lines with one gap, Gilded Gunships with Darts, Rail Serpents; the Gunship convoy secondary; Rook back in the slot | approved | done | final |
| [level-31-mining-rig-escort](level-31-mining-rig-escort/README.md) | The open belt: escort the refugee tug *Bellwether* (three sections, hurt only by hooked attacks) to the Vesta relay; Hornet salvos, Shard Drone links, Buzzsaw ricochets, rear Dart streams; the `CLEAN HULL` secondary | approved | in-progress | final |
| [level-32-deep-core](level-32-deep-core/README.md) | Inside the hollowed asteroid Kleopatra: tunnels with two route-choice forks (one safer, one richer), Sentinel Towers in the galleries, Strider pairs that topple, blast-door caches, the refinery's vents; no Airstrike or Lance; the `TOWERS DOWN` secondary | approved | done | final |
| [level-33-leech-field](level-33-leech-field/README.md) | The Vrell-grown rock field: Void Leeches from behind and the sides drain the generator, Rail Bunkers on the rocks, prey Talon wings, the Choir's one Act 5 line; the `LEECHES` purge secondary | approved | done | final |
| [level-34-salvage-run](level-34-salvage-run/README.md) | The breather: a ship graveyard with Warden Tanks in Ascendancy livery on the hulls, six containers and three hidden crates, the Mk V "Helix" salvaged from a freighter's hold; the `CONTAINERS` secondary | approved | in-progress | proposed |
| [level-35-iron-sovereign](level-35-iron-sovereign/README.md) | Ceres orbit: the escort screen, the outer hull and the mass-relay flash, then the Iron Sovereign's rotating rings; Vorne's offer, Callisto, the act end and the release's end naming Act VI | approved | in-progress | proposed |

## Design

### Synopsis

Ten days after the reveal over Europa, Chairman Silas Vorne speaks on every channel in the solar system: the
Jovian Ascendancy governs itself, the Vrell are its partners, and the belt's stations are invited to choose
the side that will still exist. **Ceres Hub**, the belt's biggest dock and the mining consortium's seat, is
still voting when Aegis Wing flies in; a station block turns, half the garrison goes with it, and the wing
fights down the dock canyon. A loyalist dock controller points it to Dock 9, where a Helix prison tender holds
**Rook** and his Ember; the wing breaks the clamps and Rook flies again. The wing clears the mined shipping
lane out of Ceres, escorts the mining tug *Bellwether* and its four hundred refugees to the Vesta relay, and
raids **Kleopatra**, an old consortium mine the Ascendancy has hollowed into the refinery that feeds its
battle station. It crosses a rock field the Vrell have grown over, where leeches drain the Stormhawks'
generators and feed on Ascendancy Talons too, and salvages a ship graveyard, where a Helix freighter's hold
gives up a Mk V generator. Then it climbs to Ceres orbit and breaks the **Iron Sovereign**, whose captured
nav core points to Vorne's headquarters on **Callisto**.

### Story beats

The beats are the story documents' ([story](../../story/README.md), [twist](../../story/twist/README.md),
[Vorne](../../story/characters/vorne/README.md#act-5-draft), [Rook](../../story/characters/rook/README.md#act-5-captivity-and-return-draft),
[Varga](../../story/characters/varga/README.md#act-5-draft), [the Choir](../../story/characters/the-choir/README.md),
[the Ascendancy](../../story/factions/jovian-ascendancy/README.md)); the lines are the level documents'.

- **The act intro is Vorne's broadcast** (Q9 a): Okafor opens it ("we hear it together", Act 4's last
  promise), Vorne speaks on the enemy channel (pages 2–3), Okafor answers and orders the wing to Ceres.
- **L29**: Ceres Hub turns sides at t=60 (the lights go white → red); the mutineers' flight lead, **Harker**,
  speaks on the enemy channel, and Rook knew him from his convoy years. Three pairs of Ceres Wing Stormhawks
  **waver** (a white IFF light, guns silent, Q10 b): the "Hold fire" secondary. Ceres Hub Control, a loyalist,
  points the wing to Dock 9; the clamps break and Rook's Ember launches from the tender: "*Miss me? Don't answer
  that. Just shoot.*" (Q8 a). The loaner Bulwark returns to base. Vorne is not on the radio in this level.
- **L30**: Rook back in the slot; his only word on the captivity is a joke.
- **L31**: the *Bellwether*'s captain tells how Helix bought half of Ceres with Vrell cures ("*Lung rot, bone
  loss. Gone in a week. Nobody asked where it came from.*").
- **L32**: the refinery that feeds the Sovereign; the *Bellwether*'s crew gave the wing the shaft charts.
- **L33**: the alliance is uneasy: the leeches feed on Ascendancy Talons too. **The Choir speaks once**, of "the
  one who opened", with indifference; Varga answers it and names Vorne.
- **L34**: the breather; the wing salvages the Mk V "Helix" generator (captured Helix tech) from a freighter.
- **L35**: Vorne announces his station on the enemy channel, taunts and cracks without shouting; when the
  Sovereign falls he offers Lancer a price ("*Come to Callisto. Name your price.*"), and Varga reads Callisto out
  of the nav core. Ceres Hub's lights come back white.
- **The act outro**: Varga sees the cures on Vorne's face ("*He looks forty. He was sixty when I left Helix.*"),
  her Act 5 beat; Rook has one line; Okafor's answer names Callisto.

### Act intro and outro

Format: the [briefing screen](../../ui/briefing/README.md), as Acts 1–4. Order: the L28 debrief with the act
summary → Act 4's outro (which ends on "*Whatever comes over the radio next, we hear it together*") → this act's
title card → the act intro below → the L29 mission briefing → the hangar visit before L29 (autosave) → L29. A save
at the L29 hangar from the `v0.2` release plays the intro **once** on Continue (the `act-intro:N` flag, as Acts
3–4's). The intro continues from Act 4's outro, which already told the cruisers' rise, the hull code and Rook's
loss, so it opens on the broadcast arriving and does not repeat them. Pages 2–3 are **intercepted** (the enemy
channel's amber frame and steady interference, [briefing](../../ui/briefing/README.md#intercepted-transmissions-draft);
the enemy voice filter, [voice](../../audio/voice/README.md#act-5-voices-draft-round-60)) and play track 12's motif
stem alone ([music](../../audio/music/README.md#act-5-music-draft-round-60)). After the Sovereign falls the act ends
in Level 28's order ([Level 35](level-35-iron-sovereign/README.md#act-end)): the wreck and the closing lines, the
debrief with the act summary `ACT V COMPLETE · THE BELT`, the four outro pages with track 24 *act complete* under
page 1, then the release's end, which moves from Level 28 to Level 35 (`v0.3`): `ACTS I-V COMPLETE … TO BE
CONTINUED / ACT VI - JOVIAN STORM. CALLISTO. COMING SOON.`, and the hangar before L36 reads `ACT VI - JOVIAN STORM:
COMING SOON`.

**Timeline** (draft): the wing leaves Europa orbit after the reveal (July 2185); Ceres is about ten days at full
burn from Jupiter at this season; the broadcast reaches the transport on the way and the wing arrives in **August
2185**, the morning after it.

**Title card** (held 3.5 s over the act's still, `ui/act-5-the-belt-still.png`, rendered by
`tools/art/act_stills.py act-5` from Level 29's production backdrop as Acts 2–4's were: Ceres's curve under the
belt's sunlit dust band, the hub's crust and docking arms, the dock canyon's mouth with its white lights, a black
and gold Helix block among them; its chrome lettering `ui/act-5-the-belt-title.png` from `tools/art/act_titles.py`;
M9 part B). Text (draft):

<!-- data: act-title-card -->
> ACT V
> THE BELT
> Ceres · August 2185
<!-- /data -->

**Act intro** (four pages, each voiced and with its own image in `assets/ui/briefing/`, as Act 4's). Texts (draft,
decided in round 60); two by Commander Okafor, two by Chairman Vorne on the enemy channel. The title card and the pages
live in the act's [data.yaml](data.yaml) (`levels`, `title_card`, `briefing` with `channel: enemy` on pages 2–3; M9
part B, step B2) and are rendered here; page 3's Vorne wears his own `smug` expression (M9 part H, step H2: the
expressions `smug` and `rage` are his only, voiced as neutral and grim). The images are production
art straight away (part B, `tools/art/briefing_images.py`); Vorne's portrait on pages 2–3 is his production
briefing portrait (proposed in round 60):

| Page | Speaker | Channel | Image |
|---|---|---|---|
| 1 | Commander Okafor (grim) | briefing | `act-5-all-channels`: the transport's ready room, every screen showing the same amber feed under the Ascendancy crest, the pilots standing round the table, Rook's chair empty |
| 2 | Chairman Vorne (neutral) | enemy (intercepted) | `act-5-broadcast`: Vorne at a gilded lectern under the Ascendancy crest (a rising sun over a ringed planet), the amber frame and the rolling interference bands |
| 3 | Chairman Vorne (smug) | enemy (intercepted) | `act-5-stations-choose`: a CDF map of the belt's stations, the first of them turning from white to red as they answer, Ceres Hub still white at the centre |
| 4 | Commander Okafor (fierce) | briefing | `act-5-burn-for-ceres`: the transport's drive at full burn, the belt's dust band ahead, Ceres a pale disc with the hub's lights; Aegis Wing's Stormhawks on their rails, Rook's rail still empty |

<!-- data: act-briefing -->
> **Commander Okafor:** "Ten days out of Europa, and he is on every channel at once: every
> station in the belt, every ship, every screen on Earth. I said we would hear it together,
> Aegis. Listen."
>
> **Vorne:** "Citizens of the solar system. My name is Silas Vorne. From this hour the Jovian
> Ascendancy governs itself, and it does not stand alone: the Vrell are our partners. The old
> order is ending. I merely opened the door."
>
> **Vorne:** "No Ascendancy city has been touched. Ask yourselves why — then ask who you would
> rather stand beside. To the stations of the belt: choose the side that will still be here."
>
> **Commander Okafor:** "He did not ask Earth. He asked the belt, and if the stations choose
> him, every ship we have is cut off from Jupiter. We burn for Ceres, and we keep listening for
> Rook. Mission briefing follows. Aegis Actual out."
<!-- /data -->

Vorne names himself and the Ascendancy, declares its independence and the partnership in the open, and invites
the stations to choose; he never mentions the cruisers or the hull code (Act 4 told them) and never says why the
Vrell wanted the door (Act 6, L41). Page 3's image keeps Ceres white: the hub turns in Level 29, not before.
Okafor's "keep listening for Rook" keeps Act 4's promise without a lead (the wing learns he is at Dock 9 in the
level).

**Act outro** (four pages after the L35 debrief, each voiced and with its own image; track 24 under page 1). The
closing radio already has Okafor's kill line, Vorne's offer, the answer and Varga's "*Callisto*", so the outro does
not repeat them; it carries the five beats the act hands over (the fall, the data, Vorne's face, Rook, Callisto).
Texts (draft, decided in round 60), with the `next_act` that names Act 6 on the release's end (`ACT VI - JOVIAN STORM.
CALLISTO. COMING SOON.`):

<!-- data: act-outro-pages -->
| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-5-outro-ceres`: Ceres Hub from orbit, the Sovereign's broken rings drifting apart over the pole, the dock canyon's lights white again |
| 2 | Dr. Varga (grim) | `act-5-outro-face`: Varga's screen: the recruitment feed paused on Vorne's face beside a 2179 Helix review photo of him at sixty, the nav core's route plot to Callisto in a strip below |
| 3 | Rook | `act-5-outro-ember`: Rook's Ember back on its rail on the transport's flight deck, the Helix clamps' scorch marks on its hull, Rook on the ladder with his helmet under his arm |
| 4 | Commander Okafor (fierce) | `act-5-outro-jupiter`: The transport's forward view, Jupiter ahead with Callisto a dark cratered disc beside it, every rail on the flight deck filled |
<!-- /data -->

<!-- data: act-outro -->
> **Commander Okafor:** "The Iron Sovereign is scrap over Ceres, and the hub's lights are white
> again. Half the belt still flies his colours. But every station watched a Coalition wing break
> his station, and they will remember it."
>
> **Dr. Varga:** "The nav core says Callisto, and so did he. I saw his face on that feed,
> Commander. He looks forty. He was sixty when I left Helix. The cures the Bellwether's people
> told us about: he took them himself."
>
> **Rook:** "Back in the slot, Commander, and staying there. Wherever we fly next, I'll keep one
> eye on what's below us. Learned that one the hard way."
>
> **Commander Okafor:** "Vorne made his offer on an open channel. Here is our answer, on every
> channel: we are coming to Callisto. Rest, refit, rearm. Aegis Actual out."
<!-- /data -->

Page 2 is Varga's Act 5 beat (her document's sample line, read low and level, not as a revelation); page 3 is Rook's
one more line his document promises (a callback to Level 26's "*something's below u—*"); page 4 keeps Okafor's Act 4
promise (what the wing sees, it says on every channel) and hands over to [Act 6](../act-6-jovian-storm/README.md).

### Settings

[The belt](../../world/belt/README.md), one sub-location per level (its *Sub-locations per level* table): Ceres Hub's
dock canyon (L29); the shipping lane out of Ceres (L30, space only); the open rock field (L31); the tunnels and the
refinery inside the hollowed asteroid Kleopatra (L32); the Vrell-grown leech field (L33); the ship graveyard (L34);
Ceres orbit and the Iron Sovereign (L35). Every level is in vacuum ([vacuum rules](../../world/belt/README.md#vacuum-rules):
`space` is the `air` layer, explosions flash and throw debris, no water or weather); the belt is drawn a step lighter
than Act 4 (Q11 b).

### New mechanics

Each is designed in the level or document that introduces it; the systems are M9's part A (the
[roadmap](../../tech/roadmap/README.md#m9-parts)).

| Level | Mechanic | Where it is designed |
|---|---|---|
| L29 | **Enemy hex shields** (Q1 a: a light HP pool over the hull that every weapon wears down and that regrows 2 s after the last hit; `shield-breaker` empties it, the EMP strips it) and the Talon's squad leader; the SAM Nest's red-gold **homing diamond**; **rock riding** (the Asteroid Mite); the **turning station** (dormant guns wake at t=60); Rook's **rescue halt** (the clock stops while the clamp guns stand) and the escort swap (Q8 a); the wavering mutineers and the inverted "Hold fire" secondary (Q10 b); the **enemy channel** on the radio and the intercepted briefing pages | [Level 29](level-29-betrayal-at-ceres/README.md), [enemy shields](../../enemies/README.md#enemy-shields), [Talon](../../enemies/air/talon/README.md), [SAM Nest](../../enemies/ground/sam-nest/README.md), [rock riding](../../enemies/README.md#rock-riding), [wingmen](../../player/wingmen/README.md#rooks-rescue-and-return-l29l30), [HUD](../../ui/hud/README.md#act-5-additions-draft), [briefing](../../ui/briefing/README.md#intercepted-transmissions-draft) |
| L30 | The act's one **space-only** level (`NO GROUND`, J3); **mine lines** laid from behind (Q3 a: clusters of 4 with one gap, a shot clears its cluster, C1 a; the proximity blast, C2 a); the Gilded Gunship's frontal plating and its **Dart Drone** launches (Q6 b); the Rail Serpent, a machine chain that **splits**; destructible drifting rocks | [Level 30](level-30-minefield/README.md), [mine lines](../../enemies/README.md#mine-lines), [Minelayer](../../enemies/space/minelayer/README.md), [Gilded Gunship](../../enemies/air/gilded-gunship/README.md), [Dart Drone](../../enemies/air/dart-drone/README.md), [Rail Serpent](../../enemies/air/rail-serpent/README.md) |
| L31 | The **mining tug escort** (Q4 a: a `huge` ally of three sections, hurt only by hooked attacks, D1 a, D2 a); **air links** between Shard Drones (the escort exception); the Hornet's homing salvos at the tug; the Buzzsaw's **ricochet** off rocks; the radio's `{section}` placeholder | [Level 31](level-31-mining-rig-escort/README.md), [mining tug](../../allies/mining-tug/README.md), [air links](../../enemies/README.md#air-links), [Hornet](../../enemies/air/hornet/README.md), [Shard Drone](../../enemies/space/shard-drone/README.md), [Buzzsaw Drone](../../enemies/air/buzzsaw-drone/README.md) |
| L32 | **Tunnels** inside rock on `Terrain` with **two route-choice forks** (Q5 a: one safer, one richer; a `branch: left\|right` radio trigger); blast doors, mining charges, the Sentinel's all-round sweep, the Strider's **topple**; the Airstrike and the Orbital Lance read `IN ROCK` | [Level 32](level-32-deep-core/README.md), [Sentinel Tower](../../enemies/ground/sentinel-tower/README.md), [Strider](../../enemies/ground/strider/README.md), [topple](../../enemies/README.md#topple), [specials](../../player/specials/README.md#act-5-specials-draft) |
| L33 | **Generator drain** (Q2 a: −2 MW a latched leech, to −6; over the load the shield's regrowth stops first, then the fire rate falls; the HUD's drain gauge); the Void Leech latching from behind and the sides and feeding on Talons; the Rail Bunker's `laser-line` on asteroid surfaces (J3) | [Level 33](level-33-leech-field/README.md), [generator](../../player/generator/README.md#enemy-drain-effects), [Void Leech](../../enemies/space/void-leech/README.md), [Rail Bunker](../../enemies/ground/rail-bunker/README.md) |
| L34 | Salvage: cargo containers, hidden crates, the freighter's clamps and the **Mk V "Helix" salvage** (Q12 c); the Warden Tank back in Ascendancy livery (J1) | [Level 34](level-34-salvage-run/README.md), [Warden Tank](../../enemies/ground/warden-tank/README.md) |
| L35 | A boss of **counter-rotating rings** over a halted arena (Q7 a: batteries fire only in the arc facing the ship, a ring locks when its guns fall, the core opens last); the **mass-relay flash**; Vorne's taunts on the enemy channel; the act end and the release's end naming Act VI | [Level 35](level-35-iron-sovereign/README.md), [Iron Sovereign](../../enemies/bosses/iron-sovereign/README.md) |

**Equipment new in the act** (Q12 c, the middle arsenal; designed in [weapons](../../player/weapons/README.md),
[generator](../../player/generator/README.md#act-5-hand-plan-draft), [shields](../../player/shields/README.md),
[armor](../../player/armor/README.md), [systems](../../player/systems/README.md) and
[wingmen](../../player/wingmen/README.md); all drafts): from the L29 visit the
[Tesla Coil Pod](../../player/weapons/tesla-coil-pod/README.md) (the `shield-breaker` trait; strongly rewarded only
from L37), the Shield Mk V, Composite IV and the Engine Mk IV; from L30 the
[Swarm Tail](../../player/weapons/swarm-tail/README.md) (`rear`, `homing`); the **Mk V "Helix" generator** (22 MW)
salvaged in L34 and in the shop from the L35 visit. The Plasma Arc, Time Dilation, the Hunter drone and the Bastion
Shield move to Act 6. The loaner Bulwark flies until the rescue in L29; Rook flies from the rescue on. In vacuum the
Harpoon Torpedoes, the Depth Charges and the Sonar Pulse read `NO WATER` and the pressure hull does nothing; the
Bomb Rack reads `NO GROUND` in L30. The trait pacing table makes `shield-breaker` available from L29.

### Factions present

The **Jovian Ascendancy** openly, in gunmetal/charcoal and gold with red running lights (Q11 b, round 60): its own machines (Talons,
Gilded Gunships, Hornets, Minelayers, Rail Serpents, Dart and Shard Drones, Buzzsaws, SAM Nests, Sentinel Towers,
Rail Bunkers, Striders, the Iron Sovereign), Act 3's Warden Tank repainted, and the Ceres garrison that turned with
the hub. The **Vrell** fly beside them (Skitters, Mote Swarms, Needlers, Stingers, Asteroid Mites, Void Leeches) and
never fire at them, but the leeches feed on Ascendancy ships too (L33). Loyalists: Ceres Hub Control, the
*Bellwether*, the wavering Ceres Wing pilots.

### Boss

Act boss [Iron Sovereign](../../enemies/bosses/iron-sovereign/README.md) (L35; Q7 a, the top-down rings: three
counter-turning rings of 6, 4 and 3 batteries round a reactor core; **8,960 HP** (the batteries 3,960, the core
5,000 with a hex shield of 80) from the proposed reference DPS 144; paper ≈ 145 s, par 150 until measured; a bounty
of **457 in Act 1 terms** at Level 35's scale 0.56, 24 % of budget(35)). **No mid-boss** in Act 5 (the campaign's
pacing rule lists Acts 1, 2, 3, 6 and 7); the Rail Serpent and the Strider are the act's large units, the Dock 9
rescue halt its mid-level set piece.

### Units in the act

New (15, the [variety checklist](../../enemies/README.md#variety-checklist-per-act)'s Act 5 row): the
[Talon](../../enemies/air/talon/README.md), [SAM Nest](../../enemies/ground/sam-nest/README.md) and
[Asteroid Mite](../../enemies/space/asteroid-mite/README.md) (L29); the
[Minelayer](../../enemies/space/minelayer/README.md), [Gilded Gunship](../../enemies/air/gilded-gunship/README.md),
[Rail Serpent](../../enemies/air/rail-serpent/README.md) and [Dart Drone](../../enemies/air/dart-drone/README.md)
(L30; the Dart is the act's `tiny` Ascendancy popcorn, Q6 b); the [Hornet](../../enemies/air/hornet/README.md),
[Shard Drone](../../enemies/space/shard-drone/README.md) and [Buzzsaw Drone](../../enemies/air/buzzsaw-drone/README.md)
(L31); the [Sentinel Tower](../../enemies/ground/sentinel-tower/README.md) and [Strider](../../enemies/ground/strider/README.md)
(L32); the [Void Leech](../../enemies/space/void-leech/README.md) and [Rail Bunker](../../enemies/ground/rail-bunker/README.md)
(L33); the Iron Sovereign (L35). Returning: Skitter and Mote Swarm (every level), Needler (L32, L34), Stinger (L33,
L34) and the [Warden Tank](../../enemies/ground/warden-tank/README.md) in Ascendancy livery (L34; the roster's Crawler
Tank dropped, J1). Level-owned, not roster units: the clamp guns and the wavering Ceres Wing Stormhawks (L29), the
[mining tug](../../allies/mining-tug/README.md) (L31, an ally).

Where the units appear (by the level drafts):

| Unit | L29 | L30 | L31 | L32 | L33 | L34 | L35 |
|---|---|---|---|---|---|---|---|
| Talon (and leader) | ● | ● | ● | ● | ● | ● | ● |
| Skitter, Mote Swarm | ● | ● | ● | ● | ● | ● | ● |
| Asteroid Mite | ● | ● | ● | | ● | ● | |
| SAM Nest | ● | | ● | ● | | | ● |
| Dart Drone | | ● | ● | ● | | | ● |
| Minelayer | | ● | | | | | |
| Gilded Gunship | | ● | ● | | | | ● |
| Rail Serpent | | ● | | | | | ● |
| Hornet, Shard Drone | | | ● | | | | ● |
| Buzzsaw Drone | | | ● | | | | |
| Sentinel Tower, Strider | | | | ● | | | |
| Needler | | | | ● | | ● | |
| Stinger | | | | | ● | ● | |
| Void Leech | | | | | ● | | |
| Rail Bunker | | | | | ● | | ● |
| Warden Tank (livery) | | | | | | ● | |
| Iron Sovereign | | | | | | | ● |

Only the returning `medium` and larger units get the act HP factor from the
[balancing basis](../../enemies/README.md#balancing-basis), against the proposed reference DPS **120 · 124 · 128 ·
132 · 136 · 140 · 144** for L29–L35 (judgement call J2: Level 29 holds Level 28's 120 with the Ion Beam raised to
L4 at the L29 visit, then +4 a level; the [Act 5 hand plan](../../player/generator/README.md#act-5-hand-plan-draft);
**part A of M9 writes them into `enemies/data.yaml` and measures them with `levelReport`**). HP at medium; a new
unit's first level takes no factor; a hex shield comes on top and scales with the hull:

| Level | Units with the act HP factor (HP at medium) |
|---|---|
| L29 | none (every unit new, `small` or `tiny`) |
| L30 | none (the Minelayer 110, the Gunship 88, the Rail Serpent's engine 150 at their first level) |
| L31 | Gilded Gunship 88 → 91 (L30, ×128/124; shield 12) |
| L32 | none (the Sentinel Tower's emitter 120 and the Strider 410 at their first level) |
| L33 | none (the Void Leech and the Rail Bunker 100 at their first level) |
| L34 | Warden Tank 84 → 127 (L19, ×140/92: hull 97, turret 30) |
| L35 | Gilded Gunship → 102 (×144/124; shield 14), Hornet 96 → 108 (×144/128; shield 9), Rail Serpent engine → 174 (cars 19, turret cars 28, tail 23; ×144/124), Rail Bunker → 106 (×144/136) |

The `tiny` units (Skitter, Mote Swarm, Asteroid Mite, Dart Drone) keep 1 HP; the `small` ones take the
**beam-era ×2** by level number (Talon 32 and its leader 48, Shard Drone 28, Buzzsaw 20, Void Leech 12, SAM Nest 20,
the Needler and the Stinger as in Acts 3–4). In vacuum the Ion Beam is back at full reach and damage (Act 4's
water factors end), so **`BeamEraHpTest` is re-measured in part A** before these HP are final.

### Density and threat profile

The act's minimum is **70 enemies a minute** at medium (`DensityTest`; the campaign's Act 5 rule; a breather may dip
20 % to 56). Every draft was written to the **Act 2+ floor** (`ActFloorTest`, `PacingTest`) from the start: a wave
about every 2.5–3 s, at most three stretches over 2 s with nothing on the screen, ≥ 2.5 enemies on the screen and
≥ 10 enemy shots a minute, with **firing groups** (the Talon pairs, the Gunships' rings, the SAM salvos, the Hornets,
the bunkers' lines) doing the shot work and the Vrell popcorn and the Ascendancy's Darts carrying the count (Q6 b;
Darts launched by Gunships count, the Sovereign's do not). **The band: 86–114 a minute at medium** (≈ 86 for the
breather L34, ≈ 114 for the L35 approach), not a target but what each level's cadence gives.

| Level | Rating | Draft | Notes | `bounty_scale` (hand estimate) |
|---|---|---|---|---|
| [L29](level-29-betrayal-at-ceres/README.md) | 4 | 350 in ≈ 186 s (172 s of clock + the 14 s halt), **≈ 113** | 19 ground targets; the halt's four waves of a typical halt counted; the wavering Stormhawks not counted | **0.44** |
| [L30](level-30-minefield/README.md) | 3 | 300 in 178 s, **≈ 101** | ≈ 48 launched Darts counted (J4; ≈ 85 without); a Rail Serpent train counts as one; no ground | **0.45** |
| [L31](level-31-mining-rig-escort/README.md) | 4 | 344 in 184 s, **≈ 112** | ≈ 30 launched Darts and 6 SAM Nests counted | **0.40** |
| [L32](level-32-deep-core/README.md) | 4 | 323–332 in 186 s by route, **≈ 104–107** | the safer route 332, the richer 323; each branch keeps the floor on its own | **0.42** |
| [L33](level-33-leech-field/README.md) | 4 | 326 in 178 s, **≈ 110** | 63 leeches; 8 hardened Rail Bunkers the plan's fit cannot kill (counted at 0 % in the haul) | **0.55** |
| [L34](level-34-salvage-run/README.md) | 2 | 266 in 186 s, **≈ 86** | the breather by threat, not by count: lighter groups, 18 tanks | **0.45** |
| [L35](level-35-iron-sovereign/README.md) | 5 | 175 in the 92 s approach, **≈ 114** | the approach only (24 Gunship Darts counted); the boss fight and the wreck come on top, no wave after t=83.5 | **0.56** |

**The economy.** Budget(n) = 700 × 1.07^(n−1): **4,654 · 4,980 · 5,329 · 5,702 · 6,101 · 6,528 · 6,985** for
L29–L35. Data is in Act 1 terms, the code pays each × the act factor **6.5536** (1.6⁴), bounties also × the level's
scale; with the hand scales above the typical hauls come to **4,662 (+0.2 %) · 4,945 (−0.7 %) · 5,342 (+0.3 %) ·
5,663 (−0.7 %, the four routes' mean; the safer 5,401 at −5.3 %, the richer 5,928 at +3.9 %) · 6,099 (−0.0 %) ·
6,511 (−0.3 %) · 7,025 (+0.6 %)**, every one within ±5 % (L32's routes within ±8 %), and the perfect runs at **1.55 ·
1.56 · 1.60 · 1.51–1.66 · 1.91 · 1.55 · 1.48** × the budget (≥ 1.4 ×). L33's 1.91 is the price of counting its
hardened bunkers at 0 % in the typical haul (the plan's fit has no `anti-ground`): an expert who brings a bomb or a
mortar takes 1,298 more; part F checks it against the expert's bound. `TypicalHaulTest` fits the final values in
each part. The scales run **0.40–0.56**, inside Act 4's 0.38–0.78. L31's haul includes the escort's pay (40 a
section home, 786 for three); L34 earns more from its wrecks (salvage 1,573 typical) than most levels do. The act
boss is 24 % of L35's budget (the perfect-run cap, Levels 21 and 28's lesson), **457 in Act 1 terms** at the level's
scale 0.56, paid 1,677.

**Threat profile across the act** (the levels' hand tables):

| Level | Directions (by wave) | Traits | Hazards | Boss |
|---|---|---|---|---|
| L29 | front 90 % · sides 10 % | `spread`, `homing` | the turning station (dormant nests wake at t≈60), drifting rocks with riders | none (the Dock 9 halt) |
| L30 | front 77 % · rear 17 % · sides 6 % | `rear`, `piercing` | mine lines (proximity, chained clusters), drifting rocks; no ground | none |
| L31 | front 81 % · sides 11 % · rear 8 % | `spread`, `side`, `homing` | drifting asteroids, homing missiles at the tug | none |
| L32 | front 100 % | `anti-ground`, `forward`, `piercing` | tunnel walls (terrain), Sentinel beams sweeping all round, furnace vents; `IN ROCK` | none |
| L33 | front 78 % · rear 11 % · sides 11 % | `rear`, `side` | generator drain, rail lines, drifting asteroids | none |
| L34 | front 100 % | `anti-ground`, `spread` | the tanks' bursts, drifting asteroids, mining charges on the wrecks (harmless to the ship) | none |
| L35 | front 87 % · sides 10 % · rear 3 % | `piercing`, `anti-ground` | the mass-relay flash (t=61), SAM diamonds and rail lines on the hull, the Sovereign's batteries and core | act boss Iron Sovereign |

Every level keeps at least 60 % of its waves from the front (L30 lowest at 77 %), and every side or rear entry has
its edge warning 3 s ahead (the Minelayer columns, the trains, the leech streams, the rear Dart streams). L31 and
L32 list three traits though the intel panel highlights two (as Levels 13, 14 and 25 did): settled in their parts.
L33 replaces the roster's `beam` by `side` (a latched leech is hit only by rear and side fire). **Act 5's breather
is a breather by threat, not by count**: L34 at 86 a minute, density 2, before the act's density-5 finale.

### Music

Per level, from the [Act 5 music plan](../../audio/music/README.md#act-5-music-draft-round-60) (draft, round 60):
track 12, the Act 5 A theme **"Hollow Rock"** (chosen in round 03; its full length and base/full/motif stems in part
B), plays in L29 (the intro under the approach, base from the canyon, full from the station's turn at t≈60), L30,
L32 (the conveyor breakdown with the lone horn at the forks, full in the refinery) and **L34 stripped** (the base stem
3 dB down, never the full mix; the motif stem once, as the Helix freighter's hold scrolls in). Track 13, the **Act 5
B theme** (two sketches in round 60: a "Gilded Cage", b "Iron Vein"; made in part D), plays in L31, L33 (the motif
muted: the Vrell's field; the Choir's line ducks it −6 dB) and the L35 approach. **Vorne's motif comes into the
open** (about 6 dB under the full mix, not Acts 3–4's 18–20 dB): under his broadcast pages (track 12's motif stem
alone), on in every Ascendancy level, muted in L33, alone after the Sovereign's kill under the closing lines. Rook's
return gets **a new sting** (track 31: the main motif's horn call answering Vorne's, ≈ 5 s, at the Ember's launch);
L35 uses the boss warning (track 22) and the Ascendancy boss theme **"Iron Sovereign"** (track 19, chosen in round
03, its full length in part H); the act-complete fanfare (track 24) plays under the first outro page. The ambiences
are three belt beds (`belt-station` L29 and L35, `belt-open` L30, L31, L33, L34, `belt-tunnel` L32, with the
refinery's furnace roar), in [sfx](../../audio/sfx/README.md#ambience-per-setting); the act's new sounds (mines,
rails, the tug, the leech's drain whine, the rings' grind, the mass relay) are listed in its
[Act 5 sounds](../../audio/sfx/README.md#act-5-sounds-draft-round-60); real-thing sounds from Freesound only
(CC0/CC-BY), no bubbly or popping sounds.

| Level | Theme | Vorne's motif | Sting / boss | Ambience |
|---|---|---|---|---|
| L29 | 12 | on | Rook's return sting (31) | `belt-station` |
| L30 | 12 | on | — | `belt-open` |
| L31 | 13 | on | — | `belt-open`, the tug's engine bed |
| L32 | 12 | on | — | `belt-tunnel`, the furnace roar |
| L33 | 13 | **muted** | — (the Choir's duck) | `belt-open` |
| L34 | 12 (base only, −3 dB) | once, at the Helix hold | — | `belt-open` |
| L35 | 13, then 19 | on; alone after the kill | boss warning (22), boss theme (19) | `belt-station` |

### Voice

Every briefing page and radio line is voiced, as in Acts 1–4 ([voice](../../audio/voice/README.md#act-5-voices-draft-round-60));
the hangar teaser and the intel lines stay text only. The speakers, channels and portraits are in
[characters](../../story/characters/README.md#act-5-speakers-draft). **Vorne is cast now** (auditioned in round 60:
an older man's polished low voice under every cast man, slow, an orator's range; his rage read at the grim row,
never shouted, a build check rejects `shout: true` on his cues before Act 6's end) and speaks on the **enemy
channel** (`filter: enemy`: the radio band plus a steady static bed, no crackle; his two intro pages through the
same filter). **Three new generic voices** (cast in their parts' rounds): **Ceres Hub Control** (L29, L35; the
loyalist dock controller, a tired, steady man), **Harker** (L29; the mutineers' flight lead, enemy channel, lower
and harder than Rook) and the **Bellwether** (L31; the tug's captain, a belt miner in her fifties; her `{section}`
lines expand to bow, cargo and engines).

Rook is **silent in L29 until the rescue** (his barks stay dropped while the slot holds the Bulwark), then speaks
his return line and his quiet line about Harker's wing; from L30 he flies with his lines (`requires: escort`, one
dry joke a level). **The Choir speaks once** (L33, before the swarm: "*…the one who opened… calls to us… we do not
answer…*"), Level 26's treatment, with indifference; it is silent at L35 (the boss is Vorne's).

| Level | Speakers | New speaker |
|---|---|---|
| Intro | Okafor (pages 1, 4), Vorne (pages 2–3, enemy channel) | Vorne (voice) |
| L29 | Okafor, Varga, Ceres Hub Control, Harker (enemy channel), Rook (after the rescue) | Ceres Hub Control, Harker |
| L30 | Okafor, Varga, Rook | — |
| L31 | Okafor, Varga, Rook, the Bellwether | the Bellwether |
| L32 | Okafor, Varga, Rook | — |
| L33 | Okafor, Varga, Rook, the Choir (speaks) | — |
| L34 | Okafor, Varga, Rook | — |
| L35 | Okafor, Varga, Rook, Ceres Hub Control, Vorne (enemy channel) | — |
| Outro | Okafor (pages 1, 4), Varga (page 2), Rook (page 3) | — |

Okafor is a woman (she / her) in every line. **Pronunciation** for the voice step is in the voice README's Act 5
table (Ceres, Vesta, Callisto, Vorne, Sovereign, Harker, Bellwether, Dacosta, RTB); seeds or prompts are pinned per
line, as in Acts 3–4. The intro's four pages and the outro's four are voiced with the act's data (parts B and H).

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The title card's still and the act intro's and outro's images are production art straight away (M9 parts B and H, as
Act 4's). Round 60 proposes the act's concept: the wavering Ceres Wing Stormhawks (Q10 b, step ART-P-world, below),
the belt's per-level mood stills and the Q11 readability frame in [the belt](../../world/belt/README.md#concept-art),
the units in their own documents, the Iron Sovereign in [its document](../../enemies/bosses/iron-sovereign/README.md#concept-art),
the mining tug in [allies](../../allies/mining-tug/README.md#concept-art) and Vorne's production portraits in
[Vorne](../../story/characters/vorne/README.md#concept-art). The wavering craft (chosen in round 60): a PNG
sheet (source render, bank frames, the IFF light off, white and red, a line-up with the player, Rook and the Talons,
key frames) and a 10.5 s GIF of a pair beside a Talon wing in the red-lit dock canyon; generator
`tools/concept/belt_r60.py`; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/wavering-stormhawk-r60-a.png](concept/wavering-stormhawk-r60-a.png) | Wavering Stormhawk A, Ceres Wing ochre — the CDF airframe in grey with sand-ochre panels, the Ascendancy crest painted over the roundel, the white IFF light (sheet) | chosen (round 60) |
| [concept/wavering-stormhawk-r60-a.gif](concept/wavering-stormhawk-r60-a.gif) | Wavering Stormhawk A — a pair enters, blinks, holds fire with a wing-waggle and peels off; a stray hit flashes red (motion) | chosen (round 60) |

Production art, M9 part B (concept round 62, step ART-B): the wavering Ceres Wing Stormhawk from round 60's chosen A.22 a (`tools/art/belt_units.py hawk`, held until its data claims it) and the title card's still (`tools/art/act_stills.py act-5`, from Level 29's production backdrop) with the chrome lettering (`tools/art/act_titles.py`). Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/wavering-stormhawk-final-r62-a.png](concept/wavering-stormhawk-final-r62-a.png) | Wavering Stormhawk final frames (`tools/art/belt_units.py`): 16 headings × 5 banks through the Stormhawk's camera with the IFF light dark, white and red; a line-up with Lancer, Rook and a Talon (sheet) | chosen (round 62, F.5: accepted as final) |
| [concept/wavering-stormhawk-final-r62-a.gif](concept/wavering-stormhawk-final-r62-a.gif) | A pair enters, blinks its white IFF light, holds fire with a wing-waggle and peels off; a stray hit flashes one red | chosen (round 62, F.5: accepted as final) |
| [concept/wavering-stormhawk-capture-r62-a.png](concept/wavering-stormhawk-capture-r62-a.png) | Game capture, Level 29 (step B5): the first wavering pair, `HOLD FIRE 0 / 6`, holding fire and peeling off | chosen (round 62, F.5: accepted as final) |
| [concept/act-5-intro-capture-r62-a.png](concept/act-5-intro-capture-r62-a.png) | Game capture: the Act 5 intro from a `v0.2` save's Continue: the title card, Okafor's pages 1 and 4 at half size, Vorne's intercepted pages 2 and 3 | proposed (round 62, F.9: redo; the title card in the capture is the rejected still, the pages are final) |
| [concept/act-5-intro-capture-r62-a.mp4](concept/act-5-intro-capture-r62-a.mp4) | Game capture with the game's sound: pages 2 and 3, Vorne's voice over the "Hollow Rock" motif stem | proposed (round 62, F.9: redo; the title card in the capture is the rejected still, the pages are final) |
| [concept/release-end-capture-r62-a.png](concept/release-end-capture-r62-a.png) | Game capture: the release's end card after Level 29's win ("ACTS I-IV COMPLETE", Level 30 coming soon) and the high score entry | chosen (round 62, C.5: accepted) |

Round 63 (F.9 redo, step ART-T): three new ideas for the title card's still, models and painted layers only (no tile ground
in perspective), each 960×540 in the belt grade and the Q.11 b livery; generator `tools/concept/act5_still_r63.py`, prompts:
[concept/prompts.md](concept/prompts.md#round-63--the-title-cards-still-redone-f9). Nothing is in `assets/` until the user picks.

| File | What | Status |
|---|---|---|
| [concept/act-5-still-r63-c.png](concept/act-5-still-r63-c.png) | C, the gold wing: four Talons (Q.11 b) banked in echelon gleaming past among the rocks, the sun low at the left beside a huge rim-lit foreground rock, a big sunlit rock top right | chosen (round 63, T.1 c picked: the gold wing, not the recommendation; to production in round 64, ART-F: `concept/act-5-still-final-r64-a.png`) |
| [concept/act-5-still-r63-sheet.png](concept/act-5-still-r63-sheet.png) | Contact sheet: each still at half size and the title card over it (darkened to 55 %, the letterbox, the chrome lettering) | chosen (round 63, T.1 c picked: the sheet of the three ideas, kept for the record) |
| [concept/release-end-capture-r63-a.png](concept/release-end-capture-r63-a.png) | Game capture (step C5): Level 30's debrief (the convoy met, the buoy's secret found) and the release's end card after it ("ACTS I-IV COMPLETE", "ACT V - THE BELT. LEVEL 31 COMING SOON.") | chosen (round 63, accepted) |

Production art, round 64 (step ART-F): the title card's still from round 63's chosen c (`tools/art/act_stills.py act-5`,
round 63's render kit imported unchanged) into `assets/ui/act-5-the-belt-still.png`, the lettering unchanged
(`act_titles.py`); round 62's rejected still is in [concept/rejected/](concept/rejected/README.md). Prompts:
[concept/prompts.md](concept/prompts.md#round-64--production-the-title-cards-still-the-gold-wing).

| File | What | Status |
|---|---|---|
| [concept/act-5-still-final-r64-a.png](concept/act-5-still-final-r64-a.png) | Review sheet: the still (the gold wing: four Talons in the production livery in echelon, each banked its own way, their gold a polished metal with a sheen band; the sun's soft glare by the rim-lit foreground rock), the title card `ACT V / THE BELT` over it, 2× crops of the leader and the glare | chosen (round 64, F.2: accepted as final) |
| [concept/act-5-title-capture-r64-a.png](concept/act-5-title-capture-r64-a.png) | Game capture (step D5; [recipe](concept/prompts.md#release-end-capture-r64-a)): the production title card in the game (Continue from a `v0.2` save at Level 29) and the intro's page 1 | chosen (round 64, accepted) |
| [concept/act-5-title-capture-r64-a.mp4](concept/act-5-title-capture-r64-a.mp4) | Game capture with the game's sound: the title card and the intro's page 1 (Okafor's voice) | chosen (round 64, F.2: accepted as final) |
| [concept/release-end-capture-r64-a.png](concept/release-end-capture-r64-a.png) | Game capture (step D5): Level 31's debrief (`TUGS HOME 3 / 3`, +786, the secondary missed) and the release's end card after it (Level 32 coming soon) | chosen (round 64, accepted) |
| [concept/release-end-capture-r65-a.png](concept/release-end-capture-r65-a.png) | Game capture (step E5; [recipe](concept/prompts.md#release-end-capture-r65-a)): Level 32's debrief and the release's end after it ("LEVEL 33 COMING SOON") | chosen (round 65, accepted with the level) |
| [concept/release-end-capture-r66-a.png](concept/release-end-capture-r66-a.png) | Game capture (step F5; [recipe](concept/prompts.md#release-end-capture-r66-a)): Level 33's debrief and the release's end after it ("LEVEL 34 COMING SOON") | chosen (round 66, accepted with the level, F.1–F.6) |
| [concept/release-end-capture-r67-a.png](concept/release-end-capture-r67-a.png) | Game capture (step G5; [recipe](concept/prompts.md#release-end-capture-r67-a)): Level 34's debrief and the release's end after it ("LEVEL 35 COMING SOON") | proposed |
| [concept/act-end-capture-r68-a.mp4](concept/act-end-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#act-end-capture-r68-a)), clip with sound: the debrief, the act summary, the four outro pages and `ACTS I-V COMPLETE` | proposed |
| [concept/act-end-capture-r68-a.png](concept/act-end-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#act-end-capture-r68-a)), frames: the debrief, the act summary, the four outro pages and `ACTS I-V COMPLETE` | proposed |

## Implementation

- [x] All 7 levels promoted to level documents (drafts written 2026-10-27, decided in round 60) and implemented (M9
      parts B–H). (Part H, steps H2–H4: Level 35, the last; the rounds' art and sound picks for part H in round 68.)
- [x] Act data file: `levels: [29, 35]`, the title card, the four intro pages (pages 2–3 `channel: enemy`, `music:
      motif`) and the four outro pages with their images and expressions, rendered into *Act intro and outro* as
      marked tables (part B for the intro, part H for the outro with `next_act` `{act: ACT VI, name: JOVIAN STORM,
      setting: CALLISTO}`); Act 4's `next_act` removed when Act 5's levels play. (M9 part B, step B2: the act's
      `data.yaml` with `levels`, the title card and the four intro pages, pages 2–3 `channel: enemy`, rendered;
      Act 4's `next_act` kept until the release's end moves (steps B3/B4); the motif under pages 2–3 is the game's.
      Step B3: Act 4's `next_act` removed, the release ends after Level 29's win until part H. Step B4: the intro
      built in the game, Vorne's name plate, the intercepted page's image, the motif stem under pages 2–3; seen in round
      62's capture. M9 part H, step H2: the four outro pages (`music: act-complete`, each voiced) and the `next_act`
      (ACT VI · JOVIAN STORM, CALLISTO) in the data, rendered here; page 3's Vorne `smug`, re-voiced. Step H4: the
      game's act end and release end after L35; recorded in round 68's captures, step H5.)
- [x] The title card's still (`tools/art/act_stills.py act-5` from Level 29's backdrop) and lettering; the eight
      intro and outro briefing images and the fourteen level briefing images (`tools/art/briefing_images.py`). (M9
      part B, step ART-B: the still and the title, the four intro images and Level 29's two, in round 62 F.9–F.10;
      the outro's and Levels 30–35's in their parts; part H, step ART-H2: Level 35's two and the outro's four, in the
      game from step H2, round 68 H.6–H.7.)
- [x] The act's systems (M9 part A and the levels' parts): enemy hex shields and `shield-breaker`, the homing
      diamond, mine lines, air links, topple, rock riding, `laser-line`, the generator drain and its HUD gauge,
      vacuum and the `IN ROCK` limits, the enemy channel on the radio and the intercepted briefing page, Rook's
      rescue and the loaner's release, the mining tug escort, the route-choice forks and the `branch` radio
      trigger, the `{section}` placeholder, the rotating-ring boss. (Parts A–H; the rotating rings steps H1a–H1c and
      H4.)
- [x] `reference_dps` 29–35 and the Act 5 balance plan (`balance-plan.yaml` visits 29–35) before any Act 5 level
      data (M9 part A): `enemies/data.yaml`, `ActHpFactorTest`, `BalanceTest`, `tools/balance.py` exit 0 (step A2: the
      static measure, forward / reference 1.12 · 1.09 · 1.05 · 1.02 · 0.99 · 0.96 · 1.02; each level's part measures
      its own with `levelReport`).
- [x] Every level within the act's density band (`DensityTest`, `PacingTest`, `ActFloorTest`), the beam era's HP
      re-measured in vacuum (`BeamEraHpTest`) and its typical haul within ±5 % of its budget (`TypicalHaulTest`;
      L32's routes ±8 %). (Levels 29–35 green in step H4's and H5's full `check`.)
- [x] The act-end order after L35 (Level 28's) and the release's end moved from L28 to L35; the hangar before L36
      reads `ACT VI - JOVIAN STORM: COMING SOON`. (Step H4, `OutroOnwardTest`; recorded in round 68's captures, step
      H5.)
- [x] A save at the L29 hangar from the `v0.2` release plays the Act 5 intro **once** on Continue (the
      `act-intro:N` flag), not repeating the title card of an act already seen. (M9 part B, step B3: a real v0.2 save,
      `CampaignRouteTest`; the game's Continue as Act 4's.)
- [ ] The act's voices (Vorne, Ceres Hub Control, Harker, the Bellwether, the Choir's line) cast and rendered; the
      act's sounds (Freesound only), track 12's full length and stems, track 13, track 19's full length and Rook's
      sting made.
- [ ] `ActPlaythroughTest` and `BalanceTest` through Act 5 with the Bulwark (L29 before the rescue) and Rook (from
      the rescue; `--escort none` works) (M9 part I); `./gradlew :content:playthroughSave -Plevel=35` writes the save
      at the Level 35 hangar; the act passes the variety checklist as built.
- [ ] The user's playthrough of Act 5, then the `v0.3` release (Acts 1–5).

## Open questions

All approved by the user in [concept round 60](../../concept-rounds/README.md) (2026-10-27); each draft follows the
recommended answer except Q11 (b) and the tug's size (J.6 b):

- The gap analysis's questions: **Q1** enemy shields (a), **Q2** generator drain (a), **Q3** minefields (a),
  **Q4** the mining tug escort (a), **Q5** branching tunnels (a), **Q6** popcorn: the Dart Drone (b), **Q7** the
  Iron Sovereign's top-down rings (a), **Q8** Rook's rescue on the screen (a), **Q9** Vorne's broadcast as the act
  intro (a), **Q10** the wavering mutineers and "Hold fire" (b), **Q11** the Ascendancy look (**b**, gunmetal/charcoal with gold, not the recommendation), **Q12** the
  middle arsenal (c). Each alternative is in the document that drafts it.
- The judgement calls: **J1** the Crawler Tank dropped for the Warden Tank in livery; **J2** `reference_dps`
  120 → 144 with the Ion Beam L4 at the L29 visit; **J3** L33 gets ground (Rail Bunkers), so `NO GROUND` is L30's.
- The build questions: **C1** mine lines in clusters of 4, one shot clears a cluster (a); **C2** a 40 px blast with
  the ring (a); **D1/J.6** the tug **200 × 300 px** (b, not the recommendation: the hull dominates the field); **D2** only attacks aimed at the tug hurt it (a); **J4** the Dart
  Drone built in part C (L30).
- The act's texts: the title card, the four intro pages (Vorne's broadcast) and the four outro pages above.

## Decisions

- 2026-09-30: The Belt is the first act where the Ascendancy fights openly.
- 2026-09-30: Rook is freed at Ceres Hub during L29 and is available again from L30.
- 2026-09-30: Enemy variety pass: new units added to the level rows so the act passes the [variety checklist](../../enemies/README.md#variety-checklist-per-act).
- 2026-10-27 (Act 5 design phase, step m9-d-act; **approved in round 60**): the sketch's roster
  replaced by seven draft level documents (the Contents table); the act README rewritten for the rules Acts 1–4
  established: the intro (title card ACT V · THE BELT · Ceres · August 2185; four pages, Okafor, Vorne's broadcast
  on the enemy channel twice, Okafor; Q9 a) and the outro (four pages: the fall, Varga's Callisto and Vorne's face,
  Rook's line, Okafor's answer), the units in the act with the HP factors from the proposed reference DPS 120–144
  (J2), the density band (≈ 86–114 a minute at the Act 2+ floor) and the hand haul table from the level drafts
  (every level within ±5 %), the threat profile, music (track 12 in L29, L30, L32 and L34 stripped; track 13 in L31,
  L33 and the L35 approach; Vorne's motif in the open; Rook's sting) and voice (Vorne cast; Ceres Hub Control,
  Harker and the Bellwether new). The old open question (sparing the mutineer pilots) became Q10 (b, a small mercy
  inside the linear story).
- 2026-10-27 (same step): **J1**: the roster's Crawler Tank (L34) is gone; L34 flies the Warden Tank in Ascendancy
  livery; the Crawler Tank is named only as the dropped alternative. **No mid-boss** in Act 5 (the pacing rule). The intro's
  page 3 image shows the belt's stations turning red with Ceres still white (the hub turns in Level 29, not
  before). Level 33's `bounty_scale` recomputed to **0.55** with its hardened bunkers at 0 % of the typical haul
  (6,099, −0.0 %; perfect 1.91 ×); Level 35's boss bounty **457** in Act 1 terms at its 0.56 (the boss document
  follows). The Gilded Gunship takes the act HP factor in L31 (88 → 91). The M9 row and its parts A–I are proposed in
  the [roadmap](../../tech/roadmap/README.md#m9-parts) (rounds 61–69).
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): every question, judgement call and
  document approved as drafted (Q.1–Q.10 and Q.12 as recommended, J.1–J.12, D.1–D.19; `design: approved` on the act
  and its seven levels), with **two changes**: **Q11 b**, the Ascendancy's Act 5 look is gunmetal/charcoal hulls with
  gold, not gloss black (it diverges on purpose from the final L28 cruisers; the units' chosen variants are re-rendered
  in gunmetal in production; see [enemies](../../enemies/README.md#factions-and-visual-language)); **J.6 b**, the
  mining tug is **200 × 300 px** (the drafts assumed 160 × 260: [Level 31](level-31-mining-rig-escort/README.md) and
  part D re-measure the lanes). The wavering Stormhawks are variant a (Ceres Wing ochre), the belt moods a (the lighter
  grade); the music is M.1 a "Gilded Cage" and M.2 a "Horn Call". Also decided by the main agent from the part A gap
  check (confirmed in round 61, 2026-10-28): homing shots may lock onto homing diamonds only when no enemy unit is in reach (QA-1).
- 2026-10-28 (M9 part B, step B5; reviewed in round 62, F.9 redo): the Act 5 intro captured in the game from a
  `v0.2` save's Continue (the title card, Okafor's page, Vorne's two intercepted pages with his voice over the motif
  stem); under pages 2–3 the motif stem alone has rests, so a page left waiting after Vorne's voice sits in silence.
  The release's end after Level 29 captured (round 62 C.5).
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the **wavering Stormhawk** (F.5), the six **briefing images** (F.10, in [ui/briefing](../../ui/briefing/README.md#concept-art)), the intel portraits (F.11) and the release's end card (C.5) accepted. **F.9 redo: the Act 5 title card is rejected pending a replacement.** The user: "This looks really strange, almost like a rendering glitch. You should think up something entirely different." A new proposal, a different idea rather than a re-render of the same one, comes in a later art step; until then the current still (`concept/act-5-still-final-r62-a.png`, in `assets/`) stays in the game and its Concept art row stays `proposed`; it moves to `concept/rejected/` with the replacement's pick. Level 29's backdrop (F.7), clamp gun (F.6) and the Act 5 radio portraits (F.8) accepted; the Hub Control and Harker voices are cast (V.1 a, V.2 b), Vorne's pitch is a (V.3).
- 2026-10-28 (user, [round 63](../../concept-rounds/round-63/README.md), closed): **T.1 c**, the gold wing, not the recommendation: the Act 5 title card goes to production from idea c (`tools/art/act_stills.py act-5` rebuilt from it, a later art step, **ART-F**); round 62's still stays in `assets/` and in the game until then; a and b rejected (`concept/rejected/`). The release's end card accepted.
- 2026-10-28 (M9, step ART-F; review in round 64): the title card's still goes to production from round 63's c
  (`act_stills.py act-5`, now writing `assets/ui/act-5-the-belt-still.png`; round 62's sheet to `concept/rejected/`).
  Round 63's weak spots fixed: the four Talons each in their own bank and heading (round 63 flew one pose), their gold the
  production livery's polished metal (less mirror, a sheen band, lit by the low key) instead of flat yellow, the sun's
  glare a soft halo rolled off to white with a stronger dither instead of a clipped, posterized blob.
- 2026-10-28 (user, [round 64](../../concept-rounds/round-64/README.md), closed; page answers): the Act 5 title card's still in production (round 63's T.1 c, the gold wing) accepted as final (**F.2**), with the release's end card after Level 31 ("LEVEL 32 COMING SOON"); the hex shield **F.1** accepted too (see [enemies](../../enemies/README.md#enemy-shields)); Level 31's art, the Bellwether's voice (**V.1 b**, Laurie Anne Walden), track 13 and the sounds are decided in [its Decisions](level-31-mining-rig-escort/README.md#decisions). **C.1 a**: a beam's touch counts on an Act 5 tow (Level 30's buoy, Level 31's claim), opt-in per level; built in the next game step.
- 2026-10-29 (M9 part H, step H2; pending round 68): the act's `outro` (the four approved pages, the text unchanged, voiced)
  and its `next_act` (`ACT VI - JOVIAN STORM. CALLISTO.`) are in the act's data, rendered into *Act intro and outro*;
  Vorne's `smug` and `rage` are expressions of his only (the content checks it), voiced with the neutral and the grim
  rows (never `shout`); the intro's page 3 wears `smug` (re-voiced: the expression is part of the voice key; the old
  take listed as unused).
- 2026-10-29 (user, [round 65](../../concept-rounds/round-65/README.md), closed): the release's end after Level 32 (the debrief, "ACTS I-IV COMPLETE", "ACT V - THE BELT. LEVEL 33 COMING SOON.") captured and accepted; Level 32's art and calls are in its [README](level-32-deep-core/README.md#decisions).
- 2026-10-29 (M9 part H, step H5; pending the user's review in round 68): part H built: Level 35, the Iron Sovereign
  and the act's end (the debrief's act summary, the four outro pages, `ACTS I-V COMPLETE` and Act VI's coming-soon),
  recorded in round 68's captures; the release's end now after Level 35.
