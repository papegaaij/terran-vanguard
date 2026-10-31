---
title: Act 6 – Jovian Storm
design: approved
implementation: in-progress
art: chosen
depends-on: [../act-5-the-belt, ../../world/jovian, ../../story/twist, ../../story/characters/vorne, ../../enemies/bosses/honour-guard, ../../enemies/bosses/ascendant, ../../player/generator]
updated: 2026-10-31
---

# Act 6 – Jovian Storm

## Summary

Levels 36–42. The Coalition fleet reaches Jupiter to hunt Vorne down, and Aegis Wing flies ahead of it: into the
upper bands to break the fighter cover, over the gilded cloud capital **Aurelia** as its traffic control breaks with
Vorne and the **Vrell turn on the Ascendancy**, down through the storm layer to Io's refineries, past Vorne's three
**Honour Guard** aces to Callisto, into Helix Dynamics' fortress, where Vorne's files name **the Silence** the Vrell
are fleeing, and up to Callisto orbit, where his flagship **Ascendant** breaks apart and a Vrell lifeboat carries him
toward the gate. The enemy is the Ascendancy at home, its elite in gloss black and heavy gold, and its hybrids:
Ascendancy machines with Vrell tissue grown through the seams, which heal, ram and burst.

**Status: approved 2026-10-31 (round 72, D.1)** (drafted in the Act 6 design phase, 2026-10-31). The seven level
documents, the units, both bosses and the systems were drafted in the design phase's first three waves from the gap
analysis's recommended answers (Q1–Q12, the judgement calls J1–J10; a local working note, `m10-dgap-gaps.md`); this
README pulls them together (the act intro and outro, the units and their HP factors, the density and haul table, the
threat profile, the music and the voice) as [Act 5's](../act-5-the-belt/README.md) did, and adds the judgement calls
J11–J14 the drafts raised. There is **no `data.yaml`** under the campaign or the enemies for Act 6 yet: every number
here is a hand table marked draft, taken from the level drafts, and the build (M10,
[roadmap](../../tech/roadmap/README.md#m10-parts)) turns them into data and measures them.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [level-36-cloud-divers](level-36-cloud-divers/README.md) | The dive from orbit into Jupiter's upper bands, air only: foreground cloud banks over the play plane, the regenerating Chimera beside the Talons, the fleet's drop craft marked friendly, helium-3 harvesters (`HARVESTERS`), wind and the first lightning | approved | in-progress | proposed |
| [level-37-fall-of-aurelia](level-37-fall-of-aurelia/README.md) | Aurelia, the cloud capital: evacuation shuttles (`SHUTTLES`), Mirror Interceptors and Graft Turrets, the act's `shield-breaker` level; the Vrell turn at t=100 and the falling city in the last 40 s | approved | not-started | none |
| [level-38-lightning-belt](level-38-lightning-belt/README.md) | The storm layer, every direction, air only: told lightning strikes that hit the enemies too, Harrows bursting into rings (`HARROWS`), the Buzzsaw Drone's return | approved | not-started | none |
| [level-39-io-flyby](level-39-io-flyby/README.md) | Io's refineries, `destroy-targets`: six cracking towers under holds, dry eruptions throwing rocks, Striders guarding the works, the pump stations (`PUMPS`) | approved | not-started | none |
| [level-40-honour-guard](level-40-honour-guard/README.md) | The Callisto approach in vacuum, air only: Vorne's picket line, then the mid-boss Honour Guard (Harker, Vesper, Kestrel) and Rook's rivalry, then the run-out to the citadel's outer line | approved | not-started | none |
| [level-41-callisto-fortress](level-41-callisto-fortress/README.md) | Helix HQ: three Shield Pylon gates and the Halo Platform held, Warden Tanks, Rail Bunkers and grafted turrets, Varga's `FILES` and the Silence named | approved | not-started | none |
| [level-42-ascendant](level-42-ascendant/README.md) | Callisto orbit, air only: the home fleet's screen and two cruisers, the Ascendant bow-on, Vorne's only shout, the lifeboat's escape; the act end and the release's end naming Act VII | approved | not-started | none |

## Design

### Synopsis

A month after Ceres the Coalition fleet arrives at Jupiter under Commander Okafor, on the bridge of the carrier
*Steadfast*. Callisto, Vorne's headquarters, sits behind his home fleet and Aurelia's air-defence grid, and his fleet
burns helium-3 from the cloud harvesters and Io's refineries, so the fleet goes for the sky and the fuel first. Aegis
Wing dives into the upper bands to break the fighter cover, where the first **hybrids** fly: Talon airframes with Vrell
tissue grown through them, which heal. Over **Aurelia** the city's own traffic control breaks with Vorne and asks the
CDF to fly its civilians out; while the wing clears the shuttles' lanes, the Choir declares that the one who opened the
door is no longer needed, Vrell growth bursts over the gold, the Vrell turn on their partners, and the city falls
through the clouds. The wing takes the storm layer, the only covered road, to **Io** and burns the refineries, then
crosses to Callisto, where Vorne's Honour Guard (led by Harker, the Ceres mutineer) holds the lane. In **Helix HQ**
Varga's uplink pulls Vorne's deal out of the data core: the Vrell bought passage into the inner system because they are
fleeing something the files call **the Silence**. Vorne makes his stand in orbit aboard the **Ascendant**, half
warship, half Vrell organism; when its Vrell half erupts through the hull he shouts for the only time, and when it
breaks a lifeboat the Vrell grew for him carries him toward the gate, still believing the deal holds.

### Story beats

The beats are the story documents' ([story](../../story/README.md), [twist](../../story/twist/README.md),
[Vorne](../../story/characters/vorne/README.md#act-6-draft), [Okafor](../../story/characters/okafor/README.md#act-6-draft),
[Varga](../../story/characters/varga/README.md#act-6-draft), [Rook](../../story/characters/rook/README.md#act-6-draft),
[the Choir](../../story/characters/the-choir/README.md#act-6-draft), [the Ascendancy](../../story/factions/jovian-ascendancy/README.md),
[the Vrell](../../story/factions/vrell/README.md)); the lines are the level and boss documents'.

- **The act intro** (Q11 a): Okafor from the *Steadfast*'s bridge (the fleet at Jupiter, then the plan in one page),
  Vorne intercepted on page 3 (courteous, inviting her to Aurelia "as you visited it before"), and her answer on every
  channel: "*Vorne. This is Okafor. I was at Aurelia. I remember. So will you.*"
- **L36**: the hybrids' first flight; Varga names them Chimeras and says once what the healing costs the pilots
  inside. Vorne is silent (the intro carries him).
- **L37**: Aurelia Traffic breaks with Vorne to fly the civilians out; Varga keeps the city's identification codes
  (the IFF spoofer's story); at t=100 **the Vrell turn**: the Choir's line, Vorne's cold order that the Vrell are hostile
  (never shouted), Varga's "*They were never his partners. Only a door.*" From then on chosen Vrell waves hunt the
  Ascendancy's craft (the [three-way war](../../enemies/README.md#the-three-way-war)). Over the falling city Okafor says
  she stood on those decks in 2166, when the CDF fired on them.
- **L38**: the storm road; intel and Rook's weather report.
- **L39**: Io's fuel burns; Vorne's one composed line, without a hint of what is coming.
- **L40**: the Honour Guard, the act's pulp highlight: Harker of Ceres calls Rook out; Rook's last word on him is
  quiet ("*That one's for Ceres, Harker.*"). Varga had an office in Helix HQ, three floors down.
- **L41**: Varga's files: the shipping lists, the contract, the price (passage), and at the Halo's fall the reason:
  "*They were not invading. They were running. Vorne's files call it the Silence.*" Named once, never described. Vorne
  is silent.
- **L42**: Vorne's last stand and his one shout at the eruption ("*No! We had an agreement!*", J6) after the Choir
  claims him; his cold goodbye; the lifeboat escapes toward the gate; Okafor's "*Then we follow him.*" is the radio's
  and the act's last voice.
- **The act outro**: what the act leaves: the Ascendant broken and Vorne gone, the Silence (Varga's beat), Rook in for
  the gate before anyone asks, and Okafor's order to the fleet for the gate (it does not repeat "*Then we follow
  him.*").

### Act intro and outro

Format: the [briefing screen](../../ui/briefing/README.md), as Acts 1–5. Order: the L35 debrief with the act summary →
Act 5's outro (which ends on "*we are coming to Callisto. Rest, refit, rearm.*") → this act's title card → the act intro
below → the L36 mission briefing → the hangar visit before L36 (autosave) → L36. A save at the L36 hangar from the
`v0.3` release plays the intro **once** on Continue (the `act-intro:6` flag, as Acts 3–5's). The intro closes Act 5's
gap: Act 5 said "Callisto", and the act opens in Jupiter's air; page 2 says why. Page 3 is **intercepted** (the enemy
channel's amber frame and steady interference, [briefing](../../ui/briefing/README.md#intercepted-transmissions-draft);
the enemy voice filter) and plays track 14's motif stem alone, the briefing music fading out over 1 s as it opens and
back over 1 s on page 4 ([music](../../audio/music/README.md#act-6-music-draft)). After the Ascendant breaks the act
ends in Level 35's order with the escape in place of the kill ([Level 42](level-42-ascendant/README.md#act-end)): the
escape, the closing lines, the debrief with the act summary `ACT VI COMPLETE · JOVIAN STORM`, the four outro pages with
track 24 *act complete* under page 1, then the release's end, which moves from Level 35 to Level 42 (`v0.4`): `ACTS
I-VI COMPLETE … TO BE CONTINUED / ACT VII - BEYOND THE GATE. THE TETHER GATE. COMING SOON.`, and the hangar before L43
reads `ACT VII - BEYOND THE GATE: COMING SOON`.

**Timeline** (J10, draft): Act 5 ends at Ceres in August 2185; the fleet refits and burns for Jupiter, about three
weeks, and arrives in **September 2185**.

**Title card** (held 3.5 s over the act's still, `ui/act-6-jovian-storm-still.png`, rendered by
`tools/art/act_stills.py act-6` from Level 36's production backdrop as Acts 2–5's were: Jupiter's cream and ochre bands
from above the cloud decks, the fleet's lights on the curve, a far gold glint where Aurelia hangs; its chrome lettering
`ui/act-6-jovian-storm-title.png` from `tools/art/act_titles.py`; M10 part B; round 72 shows this still idea with an
alternative, round 63's lesson). Text (draft):

<!-- data: act-title-card -->
> ACT VI
> JOVIAN STORM
> Jupiter · September 2185
<!-- /data -->

**Act intro** (four pages, each voiced and with its own image in `assets/ui/briefing/`, as Acts 4–5's). Texts (draft,
decided 2026-10-31 (round 72)); three by Commander Okafor, one by Chairman Vorne on the enemy channel. The title card and
the pages live in the act's [data.yaml](data.yaml) (`levels`, `title_card`, `briefing` with `channel: enemy` and
`music: motif` on page 3, and the `next_act` naming Act 7; M10 part B, step B2) and are rendered here. The images are production art
straight away (part B, `tools/art/briefing_images.py`); Vorne's portrait on page 3 is his final production portrait:

| Page | Speaker | Channel | Image |
|---|---|---|---|
| 1 | Commander Okafor (grim) | briefing | `act-6-jupiter`: the *Steadfast*'s bridge, Jupiter's storm bands filling the forward viewports, the fleet's running lights strung along the terminator, Aegis Wing's Stormhawks on the carrier's rails below |
| 2 | Commander Okafor (neutral) | briefing | `act-6-the-plan`: a CDF chart of the Jovian system: Aurelia's air-defence grid as gold rings over the bands, the harvester lanes and Io's refinery lines in amber, Callisto behind the home fleet's screen, the wing's route as a dotted line from the bands to Callisto |
| 3 | Chairman Vorne (neutral) | enemy (intercepted) | `act-6-invitation`: Vorne at a gilded window over Aurelia's domes and balloons, the Ascendancy crest above, the amber frame and the rolling interference bands |
| 4 | Commander Okafor (fierce) | briefing | `act-6-every-channel`: Okafor at the *Steadfast*'s comms board with every band lit; through the window behind her the first Stormhawks drop toward the cloud tops |

<!-- data: act-briefing -->
> **Commander Okafor:** "Aegis, Okafor, from the bridge of the Steadfast. Three weeks out of
> Ceres, and Jupiter fills every window. The whole fleet is here. So is everything Vorne has
> left."
>
> **Commander Okafor:** "Callisto sits behind his home fleet and Aurelia's air-defence grid, and
> that fleet burns helium-3 from the cloud harvesters and Io's refineries. So we break his grid,
> take the storm road to Io and burn his fuel. Then Callisto."
>
> **Vorne:** "Commander Okafor. Welcome to Jupiter. Do come to Aurelia; you know the way. You
> visited it once before, I believe, in sixty-six. The city still remembers you."
>
> **Commander Okafor:** "All channels. Vorne, this is Okafor. I was at Aurelia. I remember. So
> will you. Aegis: you dive first. Mission briefing follows. Aegis Actual out."
<!-- /data -->

Page 2 is the plan the levels follow (the grid L36–L37, the storm road L38, the fuel L39, Callisto L40–L42); page 3
turns the 2166 riots into a courtesy (his document's beat), and page 4 is the story's sample line as her answer, on
every channel as she promised at the end of Act 4. Her memory of 2166 itself is Level 37's, over the falling city, so the
two never repeat.

**Act outro** (four pages after the L42 debrief, each voiced and with its own image; track 24 under page 1). The closing
radio already has Varga's "*The Choir won't keep his deal*", Rook's "*He ran*" and Okafor's "*Then we follow him.*", so
the outro does not repeat them; it carries what the act hands over (the fall, the Silence, Rook, the gate). Texts
(draft, decided 2026-10-31 (round 72)), with the `next_act` that names Act 7 on the release's end (`{act: ACT VII, name: BEYOND THE
GATE, setting: THE TETHER GATE}`):

| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-6-outro-callisto`: Callisto orbit, the Ascendant's broken gold hull drifting apart over the ice, the Coalition carriers closing on the black citadel, the *Steadfast*'s lights in the foreground |
| 2 | Dr. Varga (grim) | `act-6-outro-silence`: Varga's screen: Vorne's files open, the contract with the Vrell sigil beside his name, one file header flagged in red, the 2179 survey's missing log below it |
| 3 | Rook | `act-6-outro-ember`: Rook beside his Ember on the *Steadfast*'s flight deck, helmet under his arm, looking out at a far point of light where the lifeboat's trail ends |
| 4 | Commander Okafor (fierce) | `act-6-outro-gate`: the *Steadfast*'s forward plot: the fleet turning outward from Jupiter, the course line running past Saturn and Uranus to the Tether Gate beyond Neptune |

> **Commander Okafor:** "The Ascendant is scrap over Callisto, and Helix's citadel is ours. Vorne's home
> fleet is broken. He is not: his lifeboat cleared our guns and burned for the gate."
>
> **Dr. Varga:** "I have read his files twice, Commander. The Vrell never came to conquer. They came through
> that gate running, from something the files only call the Silence. And it is still out there."
>
> **Rook:** "Before anyone asks, Commander: I'm going through that gate. Somebody has to watch Lancer's
> back on the far side."
>
> **Commander Okafor:** "All ships of the Steadfast group, this is Okafor. Refit at Callisto, then set course
> for the Tether Gate. Whatever waits beyond it, we go through together. Aegis Actual out."

Page 2 is Varga's beat (the gate is the Vrell's door, and they came through it afraid), read low and level, naming the
Silence a second time but never describing it; page 3 is Rook's one line (the seed of Act 7's "refuses to stay
behind"); page 4 is Okafor's order to the fleet and hands over to [Act 7](../act-7-beyond-the-gate/README.md).

### Settings

[Jovian](../../world/jovian/README.md), one sub-location per level (its *Sub-locations per level* table): the upper
atmosphere (L36), the cloud city Aurelia (L37), the storm layer under the cloud cities and round the Great Red Spot's
rim (L38), Io's refineries (L39), the Callisto approach (L40), Helix HQ in Callisto's ice (L41), Callisto orbit (L42).
The media ([media per level](../../world/jovian/README.md#media-per-level), J2): Jupiter's **air** in L36–L38 (wind,
lightning, smoke and fire), Io's **near-vacuum** in L39, **vacuum** in L40–L42 (`space` is `air`). **No ground** in
L36, L38, L40 and L42 (`NO GROUND` for the Bomb Rack and Rook's Mortar). The moods per level are Q10 a's (sunlit bands,
the gold city then fire and teal, storm-dark blue-grey, Io's black lava and sulphur, Callisto's blue-grey ice and black
space), every haze and glow layer smooth (no ordered dither).

### New mechanics

Each is designed in the level or document that introduces it; the systems are M10's part A (the
[roadmap](../../tech/roadmap/README.md#m10-parts)).

| Level | Mechanic | Where it is designed |
|---|---|---|
| L36 | **Regeneration** (the Chimera heals 1.0 s after the last hit; a held beam or steady fire denies it); **foreground cloud banks** over `air` (Q2 a: bodies under a bank at ≈ 35 %, bullets, the ship, Rook and pickups always above); the **Act 6 shields** (twice Act 5's built ones: Talon 24, leader 48, Gunship and Hornet 24); wind gusts; the act's first **lightning strikes**; the fleet's drop craft marked friendly; the harvesters as level-owned targets | [Level 36](level-36-cloud-divers/README.md), [Chimera](../../enemies/air/chimera/README.md), [regeneration](../../enemies/README.md#regeneration), [enemy shields](../../enemies/README.md#enemy-shields), [Jovian hazards](../../world/jovian/README.md#hazards--set-pieces) |
| L37 | **`reflect`** (Q1 c: non-beam shots bounce back as slow `small` bullets, a beam heats the mirror to a break in ≈ 1 s, a `shield-breaker` hit or the EMP shatters it, the flanks open); **regrowth** (the Graft Turret regrows from its root unless the root dies; J7: it pays once); the evacuation shuttles (marked friendly) and the `SHUTTLES` secondary; **the Vrell turn** and **the three-way war** (Q3 a: scripted prey, generalised from Level 33; a kill by the Vrell pays nothing); **the falling city** (Q4 a, A.13 b: the deck breaks along a jagged cut into two city layers that slow, darken and shrink over ≈ 20 s while the ship flies down beside the spires); `shield-breaker` first strongly rewarded | [Level 37](level-37-fall-of-aurelia/README.md), [reflect](../../enemies/README.md#reflect), [Mirror Interceptor](../../enemies/air/mirror-interceptor/README.md), [Graft Turret](../../enemies/ground/graft-turret/README.md), [the three-way war](../../enemies/README.md#the-three-way-war), [shuttle](../../allies/README.md#evacuation-shuttle), [Jovian](../../world/jovian/README.md) |
| L38 | **Lightning strikes** in a level (J3: a 1.0 s told column 40 px wide, `heavy`, hitting `air` enemies too and paying their kills); the Harrow's **death-burst** on contact and on death (a ring however it dies); the Buzzsaw's return ricocheting between storm cells; every direction | [Level 38](level-38-lightning-belt/README.md), [Harrow](../../enemies/air/harrow/README.md), [Buzzsaw Drone](../../enemies/air/buzzsaw-drone/README.md#act-6-the-return-in-the-storm-draft), [Jovian hazards](../../world/jovian/README.md#hazards--set-pieces) |
| L39 | `destroy-targets` on **six named cracking towers** under Level 9's holds (one that scrolls past alive fails the mission); **dry eruptions** (J3: a 1.2 s tell, shootable rocks thrown up the screen); the Strider's topple counting on a tower; the pump stations as `PUMPS` | [Level 39](level-39-io-flyby/README.md), [Strider](../../enemies/ground/strider/README.md#act-6-ios-refineries-draft), [Jovian hazards](../../world/jovian/README.md#hazards--set-pieces) |
| L40 | The **mid-boss Honour Guard** (Q5 a: staggered, then together; one bar in three segments with call-sign tags; each ace's first entry told by a 2.5 s gold chevron, no edge warning); the aces on the enemy channel | [Level 40](level-40-honour-guard/README.md), [Honour Guard](../../enemies/bosses/honour-guard/README.md), [HUD](../../ui/hud/README.md#act-6-additions-draft) |
| L41 | **Pylon gates**: Shield Pylon domes make what is under them invulnerable and the scroll halts until a gate's pylons fall (`PYLONS n / m`); the **Halo Platform** held at the centre with a checkpoint; the uplink spike's hover for the `FILES` secondary | [Level 41](level-41-callisto-fortress/README.md), [Shield Pylon](../../enemies/ground/shield-pylon/README.md), [Halo Platform](../../enemies/ground/halo-platform/README.md) |
| L42 | The **act boss Ascendant** (Q6 a: bow-on over a halted arena; phase 1 generators and mirror panels, phase 2 the Vrell half's regenerating sacs and Chimera broods, phase 3 the lifeboat that always escapes and pays pro rata to a cap; the **escape bar**); Level 28's cruisers' broadsides in the approach; Vorne's only shout; the act end and the release's end naming Act VII | [Level 42](level-42-ascendant/README.md), [Ascendant](../../enemies/bosses/ascendant/README.md), [HUD](../../ui/hud/README.md#act-6-additions-draft) |

**Equipment new in the act** (Q7 b, the middle arsenal; Q8 a; designed in [weapons](../../player/weapons/README.md),
[shields](../../player/shields/README.md), [armor](../../player/armor/README.md),
[systems](../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft) and the
[generator](../../player/generator/README.md#act-6-hand-plan-draft); all drafts): one new item a visit, from the L36
visit the [Plasma Arc](../../player/weapons/plasma-arc/README.md) (front, area and spread, chains of 3–6), from the
L37 visit the [Reflex Shield](../../player/shields/README.md#reflex-shield-act-6) (mirrors answering mirrors), from the
L38 visit Composite V (160 armour), from the L39 visit the **IFF spoofer** (Aurelia's codes, kept by Varga in L37:
turrets hesitate 0.5 s) and the **Mk V+ "Crucible"** generator (25 MW, 24,000). Time Dilation, the Hunter drone and the
Bastion Shield move to Act 7's design. Rook flies the whole act in the escort slot
([Rook in Act 6](../../player/wingmen/README.md#rook-in-act-6-draft)); every Act 6 level reads `NO WATER` for the Sonar
Pulse, and the Airstrike and the Orbital Lance work everywhere
([Act 6 specials](../../player/specials/README.md#act-6-specials-draft)).

**The hand plan** (the [Act 6 hand plan](../../player/generator/README.md#act-6-hand-plan-draft), medium): the L36
visit raises the Ion Beam to L5, the L37 visit fits the Tesla Coil Pod L2 on the left wing, the L38 and L39 visits
save, the L40 visit buys the Crucible and the IFF spoofer, the L41 visit buys Composite V (J11), the L42 visit raises a
pod to L5 and the Tesla pod to L3. Forward DPS against the proposed reference: 1.19 · 1.14 · 1.11 · 1.08 · 1.05 · 1.02 ·
1.08. The plan flies at **22 of 22 MW from L36 to L40** and has room for one utility only from L41 (24/25, then 23/25):
the Level 30 lesson is met late (judgement call **J11**, below).

### Factions present

The **Jovian Ascendancy** at home (Q9 a): its regular units in Act 5's gunmetal and charcoal with gold (Talons, Gilded
Gunships, Hornets, Dart Drones, Mirror Interceptors, SAM Nests, Sentinel Towers, Rail Bunkers, Striders, Buzzsaws,
Act 3's Warden Tank in its Act 5 livery); its **elite** in gloss black and heavy gold (the Honour Guard, the Ascendant,
the fortress's Shield Pylons, Halo Platform and citadel, Level 28's cruisers); its **hybrids** in gunmetal with teal
Vrell tissue through the seams, brightest at the weak points (the Chimera, the Harrow, the Graft Turret). The **Vrell**
(Mote Swarms, Skitters, Lampreys, Wraiths, Spore Bombers, Needlers, Stingers) fly beside the Ascendancy until Level 37's
turn, then hunt it in chosen waves (the hybrids are their own tissue: never hunted); their bullets still hurt the ship.
Friendly and neutral craft are marked (the friendly marker of round 70): the fleet's drop craft (L36), Aurelia's
Halcyon shuttles (L37), the fleet's lights on the background layers.

### Boss

Mid-boss [Honour Guard](../../enemies/bosses/honour-guard/README.md) (L40; Q5 a: three aces in gold Talons, Harker the
sniper from the front, Vesper the dancer from a side, Kestrel the feinter from the rear, then together; **3 × 1,600 HP**,
par 75; a bounty of **360 in Act 1 terms**, 120 an ace, at Level 40's scale 0.39: 15 % of budget(40)). Act boss
[Ascendant](../../enemies/bosses/ascendant/README.md) (L42; Q6 a: **15,200 HP** in its hull, par 150; the lifeboat's
damage pays up to a cap of 1,400; a bounty of **401 in Act 1 terms** at Level 42's scale 0.64, split 180 / 161 / 60
between phase 1, phase 2 and the lifeboat: 24 % of budget(42)). Act 6 has the mid-boss the campaign's pacing rule asks
of it (Acts 1, 2, 3, 6 and 7); L39's six towers and L41's Halo Platform are its held set pieces.

### Units in the act

New (8, the [variety checklist](../../enemies/README.md#variety-checklist-per-act)'s Act 6 row): the
[Chimera](../../enemies/air/chimera/README.md) (L36), the [Mirror Interceptor](../../enemies/air/mirror-interceptor/README.md)
and the [Graft Turret](../../enemies/ground/graft-turret/README.md) (L37), the [Harrow](../../enemies/air/harrow/README.md)
(L38), the [Honour Guard](../../enemies/bosses/honour-guard/README.md) (L40), the
[Shield Pylon](../../enemies/ground/shield-pylon/README.md) and the [Halo Platform](../../enemies/ground/halo-platform/README.md)
(L41; the round-07 design, J8), the [Ascendant](../../enemies/bosses/ascendant/README.md) (L42). **No new tiny unit**
(J5): the Gunships' Dart Drones and the Vrell's Motes and Skitters carry the count. Returning (17): Talon, Gilded
Gunship, Dart Drone, Hornet, Skitter, Mote Swarm, Lamprey, Wraith, Spore Bomber, Buzzsaw Drone (L38), SAM Nest,
Sentinel Tower, Strider (L39), Needler, Stinger, Rail Bunker and the Warden Tank in its Act 5 livery (L41): 25 in all.
Level-owned, not roster units: the fleet's drop craft and the helium-3 harvesters (L36), the Halcyon shuttles (L37,
allies), the cracking towers and the pump stations (L39), the archive cores and the main core (L41), Level 28's two
cruisers as invulnerable set pieces (L42).

Where the units appear (by the level drafts):

| Unit | L36 | L37 | L38 | L39 | L40 | L41 | L42 |
|---|---|---|---|---|---|---|---|
| Talon (and leader) | ● | ● | ● | ● | ● | ● | ● |
| Chimera | ● | ● | ● | ● | ● | ● | ● |
| Mote Swarm | ● | ● | ● | ● | ● | ● | ● |
| Dart Drone | ● | ● | ● | ● | ● | ● | ● |
| Skitter | | ● | ● | ● | ● | ● | ● |
| Gilded Gunship | ● | ● | | ● | ● | ● | ● |
| Hornet | | ● | ● | ● | ● | ● | ● |
| Lamprey | ● | ● | ● | | | ● | |
| Wraith | ● | | ● | | | | |
| Spore Bomber | ● | | | | | | |
| Mirror Interceptor | | ● | | | ● | | ● |
| Graft Turret | | ● | | | | ● | |
| SAM Nest | | ● | | ● | | ● | |
| Harrow | | | ● | ● | ● | | ● |
| Buzzsaw Drone | | | ● | | | | |
| Needler | | | ● | ● | | | |
| Stinger | | | ● | | | | |
| Strider | | | | ● | | | |
| Sentinel Tower | | | | ● | | ● | |
| Honour Guard | | | | | ● | | |
| Shield Pylon, Halo Platform | | | | | | ● | |
| Warden Tank (Act 5 livery) | | | | | | ● | |
| Rail Bunker | | | | | | ● | |
| Ascendant | | | | | | | ● |

The [Warden Tank](../../enemies/ground/warden-tank/README.md)'s document also plans convoys on Io's refinery roads
(L39) that the Level 39 draft does not fly (its yards are the Striders'); part E's gap check settles it.

Only the returning `medium` and larger units get the act HP factor from the
[balancing basis](../../enemies/README.md#balancing-basis), against the proposed reference DPS **144 · 148 · 152 · 156 ·
160 · 164 · 168** for L36–L42 (judgement call J1: Level 36 holds Level 35's 144 with the Ion Beam raised to L5 at the
L36 visit, then +4 a level; **part A of M10 writes them into `enemies/data.yaml` and measures them with
`levelReport`**). HP at medium; a new unit's first level takes no factor; a shield or a mirror's heat comes on top and
scales with the hull; the shields are the Act 6 weights (twice Act 5's built ones):

| Level | Units with the act HP factor (HP at medium) |
|---|---|
| L36 | Gilded Gunship 88 → ≈ 102 (×144/124; shield ≈ 28), Wraith 16 → 35 (×144/66), Spore Bomber 24 → 108 (×144/32) |
| L37 | Gilded Gunship → ≈ 105 (×148/124; shield ≈ 29), Hornet 96 → ≈ 111 (×148/128; shield ≈ 28); the Mirror Interceptor 120 (heat 130) and the Graft Turret at their first level |
| L38 | Hornet → ≈ 114 (×152/128; shield ≈ 28), Wraith → ≈ 37 (×152/66); the Harrow at its first level |
| L39 | Gilded Gunship → ≈ 111 (×156/124; shield ≈ 30), Hornet → ≈ 117 (×156/128; shield ≈ 29), Sentinel Tower emitter 120 → ≈ 142 (×156/132), Strider → hull 260 · legs 94 · pod 35, 483 whole (×156/132) |
| L40 | Gilded Gunship → ≈ 114 (×160/124; shield 31), Hornet → 120 (×160/128; shield 30), Mirror Interceptor → 130 (×160/148; heat ≈ 140); the Honour Guard's aces 1,600 each |
| L41 | Gilded Gunship → ≈ 116 (×164/124; shield 32), Hornet → 123 (×164/128; shield 31), Warden Tank → hull 114 · turret 36, Rail Bunker 100 → 121 (×164/136), Sentinel Tower → 149 (×164/132); the Shield Pylon 240 and the Halo Platform 1,560 at their first level |
| L42 | Gilded Gunship → ≈ 119 (×168/124; shield 33), Hornet → 126 (×168/128; shield 32), Mirror Interceptor → 136 (×168/148; heat ≈ 148); the Ascendant 15,200 |

The `tiny` units (Mote Swarm, Skitter, Dart Drone) keep 1 HP; the `small` ones take the **beam-era ×2** by level
number (the Chimera 40, the Harrow 16, the Graft Turret 24 and its root 16, the Talon 32 under its shield of 24 (the
leader 48), the Lamprey 6, the SAM Nest 20, the Buzzsaw, the Needler and the Stinger as in Acts 4–5). The air levels'
beam keeps its full reach (no water), so `BeamEraHpTest` holds; part A re-measures it with the Chimera's regeneration.

### Density and threat profile

The act's minimum is **75 enemies a minute** at medium (`DensityTest`; the campaign's Act 6 rule; no breather in the
act, so no 20 % dip). Every draft was written to the **Act 2+ floor** (`ActFloorTest`, `PacingTest`) from the start: a
wave about every 2.5 s, at most three stretches over 2 s with nothing on the screen, ≥ 2.5 enemies on the screen and ≥
10 enemy shots a minute, the firing groups (the Talon and Chimera pairs, the Hornets' salvos, the Mirrors' bursts, the
SAM salvos, the bunkers' lines) doing the shot work and the Darts, Motes and Skitters carrying the count (J5); every
level gives at least 4 s of calm after its last enemy, and no two `medium`-or-larger air hulls overlap (the Act 5
lessons). The density curve is the sketch's **4 · 4 · 5 · 4 · 5 · 5 · 5**. **The band: ≈ 100–118 a minute at medium**
(≈ 100 for the held Io level, ≈ 118 for the fortress), not a target but what each level's cadence gives.

| Level | Rating | Draft | Notes | `bounty_scale` (hand estimate) |
|---|---|---|---|---|
| [L36](level-36-cloud-divers/README.md) | 4 | 325 in 192 s, **≈ 102** | ≈ 30 launched Darts and 136 Motes counted; no ground; the harvesters are level-owned, not enemies | **0.31** |
| [L37](level-37-fall-of-aurelia/README.md) | 4 | 381 in 200 s, **≈ 114** | 35 ground units in nine nests and ≈ 12 launched Darts counted (air alone ≈ 104); the prey waves' bounty at two thirds in the haul | **0.30** |
| [L38](level-38-lightning-belt/README.md) | 5 | 375 in 196 s, **≈ 115** | 68 waves, every direction; no ground; lightning kills pay as the player's | **0.43** |
| [L39](level-39-io-flyby/README.md) | 4 | 320 in 192 s of script time, **≈ 100** | 24 launched Darts and 17 ground units counted; the three holds stretch the clock | **0.41** |
| [L40](level-40-honour-guard/README.md) | 5 | 321 in 170 s of clock, **≈ 113** | 36 launched Darts counted; the duel (≈ 76 s) on top, its aces not counted | **0.39** |
| [L41](level-41-callisto-fortress/README.md) | 5 | 379 in ≈ 192 s (146 s of clock and four halts ≈ 46 s), **≈ 118** | 68 ground targets with the 8 pylons and the Halo; one typical cycle a halt | **0.30** |
| [L42](level-42-ascendant/README.md) | 5 | 171 in the 92 s approach, **≈ 112** | the approach only (12 Darts counted); the boss and its launches come on top, no wave after t=83 | **0.64** |

**The economy.** Budget(n) = 700 × 1.07^(n−1): **7,474 · 7,997 · 8,557 · 9,155 · 9,796 · 10,482 · 11,216** for
L36–L42. Data is in Act 1 terms, the code pays each × the act factor **10.486** (1.6⁵), bounties also × the level's
scale; with the hand scales above the typical hauls come to **7,504 (+0.4 %) · 7,990 (−0.1 %) · 8,640 (+1.0 %) · 9,393
(+2.6 %) · 9,876 (+0.8 %) · 10,361 (−1.2 %) · 11,197 (−0.2 %)**, every one within ±5 %, and the perfect runs at **1.56 ·
1.55 · 1.72 · 1.63 · 1.67 · 1.57 · 1.60** × the budget (≥ 1.4 ×). `TypicalHaulTest` fits the final values in each part.
The scales run **0.30–0.64**, outside the gap analysis's 0.40–0.56 band at both ends, **accepted** as Act 5's built
0.27–0.59 were: L36, L37 and L41 pay richer mixes (Chimeras at 14, Wraiths and Spore Bombers at 25–30, the fortress's 68
ground targets), L42's 92 s approach with no ground pays less than L35's hull guns, so its bounties pay more each. L39's
cracking towers pay 30 so its scale sits inside the band (0.41; 0.39 at 40). The mid-boss is 15 % of L40's budget, the
act boss 24 % of L42's (the perfect-run cap, Levels 21, 28 and 35's lesson); both bounties follow their level's hand
scale and are recomputed in the build.

**Threat profile across the act** (the levels' hand tables; directions by wave unless a level counts by enemy):

| Level | Directions | Traits | Hazards | Boss |
|---|---|---|---|---|
| L36 | front 97 % · sides 3 % | `forward`, `homing` | foreground cloud banks, wind gusts (two spells), three lightning strikes; no ground | none |
| L37 | front 89 % · sides 9 % · rear 2 % | `anti-ground`, `spread`, `shield-breaker` | mirrors' bounced shots, regrowing turrets, the Vrell turn (t=100), the falling city (the last 40 s) | none |
| L38 | front 80 % · sides 14 % · rear 6 % (by enemy) | `homing`, `beam` | lightning strikes, gusts, Harrow burst rings; no ground | none |
| L39 | front 100 % | `anti-ground`, `piercing` | eruptions, Sentinel sweeps, the holds over the works | none (six towers, `destroy-targets`) |
| L40 | front 81 % · sides 10 % · rear 7 % · all edges 2 % | `spread`, `rear` | Harrow burst rings, bounced shots, the aces' sniper line and dives; no ground | mid-boss Honour Guard |
| L41 | front 100 % | `anti-ground`, `area` | rail lines, Sentinel sweeps, SAM homing diamonds, the domes (they protect, they do not hurt) | none (the Halo Platform held) |
| L42 | front 81 % · sides 10 % · rear 6 % · all edges 3 % | `piercing`, `homing` | the cruisers' broadsides, burst rings, bounced shots, the Ascendant's lance, fans, salvos, spiral and the lifeboat's wake; no ground | act boss Ascendant |

Every level keeps at least 60 % of its waves from the front (L38 lowest at 80 % of its enemies), and every side or rear
entry has its edge warning 3 s ahead (the Wraiths' own re-entries excepted; the aces and the Ascendant show their own
tells instead). The roster's directions change in two places: **L37 adds the rear** (the Mirror Interceptors' rear
ambush at t=124) and L42 flies `air` only (`NO GROUND`, its cruisers on `low-air`). The roster's "Warden Tanks in black
and gold" (L41) is met by their Act 5 livery: the regular units keep the gunmetal (Q9 a).

### Music

Per level, from the [Act 6 music plan](../../audio/music/README.md#act-6-music-draft) (draft, round 72; the cues in
its [table](../../audio/music/README.md#cues-per-level-act-6)): track 14, the Act 6 A theme **"Eye of the Storm"**
(chosen in round 03; its full length and base/full/motif stems in part B), plays in L36, L37 to the turn, L39 and L40
(the approach and after the duel). Track 15, the **Act 6 B theme** (two sketches in round 72: heavy, stormy, choir and
distorted bass), plays in L37 from the turn (its `fall` stem over the falling city), L38, L41 and the L42 approach.
**Vorne's motif** plays in the open (as in Act 5) in every level, is **cut** at Level 37's turn to that level's end (the
Choir has spoken), drops out at Level 41's Silence line, and at Level 42 the Choir's music takes his ship from under him.
The duel (L40) has its own **duel theme** (track 32, new) after the mini-boss sting (21); the Ascendant fight plays the
boss warning (22), then **track 19** "Iron Sovereign" in phase 1 and **track 18** "The Choir Descends" from the eruption
(J9), cut by the **escape sting** (track 33, new) at `ESCAPED`; the act-complete fanfare (24) plays under the first
outro page. The ambiences are four Jovian beds and Act 5's open space ([sfx](../../audio/sfx/README.md#ambience-per-setting)):
`jovian-sky` (L36 with the dive's wind, L38 +3 dB in the storm sections), `aurelia` (L37, its burning layer from the
turn), `io-refinery` (L39), `callisto-fortress` (L41), `belt-open` (L40, L42); the act's new sounds (the strike's crack,
the growth's fibrous burst, the balloons tearing, the eruptions' rocks, the towers' fall, the domes, the blast doors,
the uplink, the bosses') are listed in its [Act 6 sounds](../../audio/sfx/README.md#act-6-sounds-draft); real-thing
sounds from Freesound only (CC0/CC-BY), no bubbly or popping sounds.

| Level | Theme | Vorne's motif | Sting / boss | Ambience |
|---|---|---|---|---|
| L36 | 14 | on | — | `jovian-sky`, the dive's wind over section 1 |
| L37 | 14, then 15 (from t=103) | on to t=100, then **cut** | — (the Choir's duck at t=100) | `aurelia`, burning from t=100 |
| L38 | 15 | on | — | `jovian-sky`, +3 dB in the storm sections |
| L39 | 14 | on (the radio's duck under his line) | — | `io-refinery` |
| L40 | 14, the duel theme 32, 14 | on (the duel theme carries it) | mini-boss sting (21), duel theme (32) | `belt-open` |
| L41 | 15 | on; out after the Silence line | — | `callisto-fortress` |
| L42 | 15, then 19, 18 | on to the eruption, then gone | boss warning (22), boss themes 19 → 18, escape sting (33) | `belt-open` |

### Voice

Every briefing page and radio line is voiced, as in Acts 1–5 ([voice](../../audio/voice/README.md#act-6-voices-draft));
the hangar teaser and the intel lines stay text only. The speakers, channels and portraits are in
[characters](../../story/characters/README.md#act-6-speakers-draft). Okafor speaks from the bridge of the carrier
*Steadfast* (J10) and Varga from it too (no new filter). **Vorne** speaks four times, always on the enemy channel: the
intro's page 3, Level 37's cold order after the turn, Level 39's line at Io and his Level 42 lines, **one of them
shouted** (line 5, J6: the voice check's only exception; round 72 auditions the shout row and the fierce row). **The
Choir** speaks twice (L37 at the turn, L42 at the eruption, a different line each time) and never of the Silence.
**New voices** (J4, auditioned in round 72): **Aurelia Traffic** (L37; the city's civil traffic controller, a woman in
her thirties, friendly channel), **Vesper** and **Kestrel** (L40; two of the Honour Guard's aces, enemy channel); the
third ace, **Harker**, is Level 29's cast voice. Rook flies and speaks in every level (one pulp line a level, his
rivalry with Harker at L40, silent over the falling city).

| Level | Speakers | New speaker |
|---|---|---|
| Intro | Okafor (pages 1, 2, 4), Vorne (page 3, enemy channel) | — |
| L36 | Okafor, Varga, Rook | — |
| L37 | Okafor, Varga, Rook, Aurelia Traffic, the Choir, Vorne (enemy channel) | Aurelia Traffic |
| L38 | Okafor, Varga, Rook | — |
| L39 | Okafor, Varga, Rook, Vorne (enemy channel) | — |
| L40 | Okafor, Varga, Rook, Harker, Vesper, Kestrel (enemy channel) | Vesper, Kestrel |
| L41 | Okafor, Varga, Rook | — |
| L42 | Okafor, Varga, Rook, Vorne (enemy channel, the shout), the Choir | — |
| Outro | Okafor (pages 1, 4), Varga (page 2), Rook (page 3) | — |

Okafor is a woman (she / her) in every line. **Pronunciation** for the voice step is in the voice README's Act 6 table
(Aurelia, Halcyon, Io, Tvashtar, Loki, Pele, Prometheus, Callisto, Vesper, Kestrel, Harker, Steadfast); seeds or prompts
are pinned per line, as in Acts 3–5. The intro's four pages and the outro's four are voiced with the act's data (parts
B and H).

## Concept art

The title card's still and the act intro's and outro's images are production art straight away (M10 parts B and H,
as Act 5's). Q10's moods per level with Q2's foreground-bank readability and Q4's falling-city storyboard are in
[Jovian](../../world/jovian/README.md#concept-art), the units in their own documents (step ART-R-A), both bosses in
theirs and the new radio portraits in [characters](../../story/characters/README.md#concept-art).

Concept round 72 (Act 6 design phase, step ART-R-B; **decided 2026-10-31 (round 72)**): the title card's
still, one idea with an alternative (round 63's lesson), each 960×540 with the sheet showing the card over it
(darkened to 55 %, the letterbox, the chrome `ACT VI / JOVIAN STORM`). Generator `tools/concept/act6_r72.py still`;
prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/act-6-still-r72-a.png](concept/act-6-still-r72-a.png) | Still A, "the dive": Jupiter's sunlit bands and the Great Red Spot under the CDF carrier group diving from orbit, Callisto a dark disc beside the limb | chosen (round 72 A.14 a) |
| [concept/act-6-still-final-r74-a.png](concept/act-6-still-final-r74-a.png) | The title card's still, final (M10 part B, `tools/art/act_stills.py act-6`; the still `assets/ui/act-6-jovian-storm-still.png`, the lettering `act_titles.py`'s): A.14 a re-rendered with the fleet's lights along the curve, four drop craft diving ahead and Aurelia's far gold glint; at 1×, the title card over it, 2× crops (sheet) | proposed (round 74) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] All 7 levels promoted to level documents (drafts written 2026-10-31, decided 2026-10-31 (round 72)) and implemented (M10
      parts B–H).
- [ ] Act data file: `levels: [36, 42]`, the title card, the four intro pages (page 3 `channel: enemy`, `music:
      motif`) and the four outro pages with their images and expressions, rendered into *Act intro and outro* as
      marked tables (part B for the intro, part H for the outro with `next_act` `{act: ACT VII, name: BEYOND THE GATE,
      setting: THE TETHER GATE}`); Act 5's `next_act` removed when Act 6's levels play. *Part B done (step B2,
      2026-10-31): [data.yaml](data.yaml) with `levels`, the title card, the four intro pages (page 3 `channel: enemy`,
      `music: motif`: a new page key) and Act 7's `next_act` (the branch's release ends after Level 36, the last level
      built, until part H); Act 5's `next_act` removed; the outro pages are part H's.*
- [ ] The title card's still (`tools/art/act_stills.py act-6` from Level 36's backdrop) and lettering; the eight
      intro and outro briefing images and the fourteen level briefing images (`tools/art/briefing_images.py`).
- [ ] The act's systems (M10 part A and the levels' parts): `reflect` with the arc and Reflex-bolt rows,
      regeneration, regrowth, the death-burst on contact, the Act 6 shields, lightning strikes that hit enemies,
      dry eruptions with rocks, foreground banks over `air`, the pylon dome and gates, the three-way prey rule, the
      falling city's two ground layers and its authored jagged cut line, the friendly marker on the shuttles and the fleet's craft; the Plasma Arc, the
      Reflex Shield, Composite V, the IFF spoofer and the Mk V+ "Crucible".
- [x] **Explosions on the cream band** (the user's note on round 72 A.7, answered: the dark sharp edge was a **mockup artifact**: the explosion frames carry dark low-alpha fringe pixels, the game draws explosions additively (`Effects`, blend `SRC_ALPHA, ONE`), which adds no dark at all, but `tools/concept/enemies_r72.py`'s `boom` pasted the later frames with normal alpha): part A's capture checks how explosions read on Level 36's cream band (additive light can wash out on a bright backdrop) and, with the Plasma Arc's, that its look and the bounced bullets' magenta read apart (step A4, 2026-10-31: captures over round 72's L36 grade and ART-B's L36 tiles; the arc's violet and the magenta read apart; the small burst washes out on the brightest cream, so round 73 asks a / b / c, [explosion-bright-r73](../../art-direction/README.md#concept-art); part B's B5 repeats it in Level 36)
- [ ] **Explosions on the cream band** (decided 2026-10-31, round 73 A.3 b): the kill bursts get the soft soot puff under
      the additive flash (warm dark grey, at most 0.42 opaque, feathered to nothing, rising a little as it fades; see
      [art direction](../../art-direction/README.md#concept-art)). Open build item for part B (step B4): whether it is Act 6
      only or everywhere (Acts 1-5's golden flights stay as recorded, so it is opt-in), and a Level 36 capture that
      the bursts read over the cream and rust bands.
- [x] `reference_dps` 36–42 and the Act 6 balance plan (`balance-plan.yaml` visits 36–42) before any Act 6 level data
      (M10 part A): `enemies/data.yaml`, `ActHpFactorTest`, `BalanceTest`, `tools/balance.py` exit 0.
- [ ] Every level within the act's density band (`DensityTest`, `PacingTest`, `ActFloorTest`), the end calm (≥ 4 s),
      the beam era's HP re-measured with regeneration (`BeamEraHpTest`) and its typical haul within ±5 % of its
      budget (`TypicalHaulTest`).
- [ ] The act-end order after L42 (Level 35's, the escape in place of the kill) and the release's end moved from L35
      to L42; the hangar before L43 reads `ACT VII - BEYOND THE GATE: COMING SOON`.
- [ ] A save at the L36 hangar from the `v0.3` release plays the Act 6 intro **once** on Continue (the
      `act-intro:6` flag), not repeating the title card of an act already seen.
- [ ] The act's voices (Aurelia Traffic, Vesper, Kestrel; Harker, Vorne and his one shout, the Choir's two lines)
      cast and rendered; the act's sounds (Freesound only), track 14's full length and stems, track 15, the duel
      theme (32) and the escape sting (33) made.
- [ ] `ActPlaythroughTest` and `BalanceTest` through Act 6 with Rook (`--escort none` works) (M10 part I);
      `./gradlew :content:playthroughSave -Plevel=42` writes the save at the Level 42 hangar; the act passes the
      variety checklist as built.
- [ ] The user's playthrough of Act 6, then the `v0.4` release (Acts 1–6).

## Open questions

Nothing is open for the user. Build notes (not questions):

- The build notes for the parts' gap checks (not questions for the user): Level 38's secondary needs a `kill_ratio`
  scoped to one enemy, or the built `escapes: harrow` with `allowed: 12` (part D); part A's data rows (Composite V
  from L38, the spoofer, the Crucible's row and the Mk VI's index after it); the Warden Tank at L39 (above, part E);
  the scales outside the band, accepted.
- The act's texts: the title card, the four intro pages and the four outro pages above.

## Decisions

- 2026-09-30: Vorne survives Act 6 and is dealt with in Act 7 (L48).
- 2026-09-30: Enemy variety pass: new units added to the level rows so the act passes the [variety checklist](../../enemies/README.md#variety-checklist-per-act).
- 2026-10-31 (Act 6 design phase, step m10-d-act; **decided 2026-10-31 (round 72)**): the sketch's roster
  replaced by seven draft level documents (the Contents table); the act README rewritten for the rules Acts 1–5
  established: the intro (title card ACT VI · JOVIAN STORM · Jupiter · September 2185; four pages, Okafor twice, Vorne's
  invitation intercepted, Okafor's answer on every channel; Q11 a) and the outro (four pages: the Ascendant broken and
  Vorne gone, Varga's Silence, Rook in for the gate, Okafor's order to the fleet; "*Then we follow him.*" stays the
  radio's, not repeated), the units in the act with the HP factors from the proposed reference DPS 144–168 (J1), the
  density band (≈ 100–118 a minute at the Act 2+ floor) and the hand haul table from the level drafts (every level
  within ±5 %), the threat profile, music (track 14 in L36, L37 to the turn, L39, L40; track 15 from the turn, L38,
  L41, the L42 approach; the duel theme and the escape sting) and voice (Aurelia Traffic, Vesper and Kestrel new).
  The old open question (bullets above the foreground clouds) became Q2 (a: bullets always drawn above).
- 2026-10-31 (same step): the summary's "Vorne escapes into the Tether Gate" is now "**toward** the gate" (he goes
  through it in Act 7) and the sketch's "civilians partly grafted" is gone (Aurelia's civilians are flown out; the
  growth eats the decks, never shown on people). The roster's L37 directions add the rear, L42's layers are `air` only,
  and L41's Warden Tanks keep their Act 5 livery (Q9 a). The scales outside the 0.40–0.56 band (0.30–0.64) are
  accepted as Act 5's built ones were; Level 39's towers pay 30 (scale 0.41). The boss bounties follow their levels'
  scales (the Honour Guard 360, the Ascendant 401). The `next_act` names Act 7 with its setting, `THE TETHER GATE`.
  The ace call sign **Kestrel** is kept although a Level 03 frigate shares the name (trivia). Raised as round 72's
  J11–J14: the power headroom at L38–L39, Level 37's name, the L40/L42 secondaries, Level 40's primary. The M10 row
  and its parts A–I are proposed in the [roadmap](../../tech/roadmap/README.md#m10-parts) (rounds 73–81).
- 2026-10-31 (same step, the cross-document fixes the waves left): the Mirror Interceptor's heat 130 and the `reflect`
  rule's arc and Reflex-bolt rows; the Act 6 variety row with the units the drafts fly (25); the Chimera's line
  abreast; the Reflex Shield's mirror tech from Aurelia's wrecks; `NO GROUND` at L42 in the HUD, Jovian, specials and
  wingmen documents; the bosses' bounties and the Choir's own L42 line; Vorne's L39 line without the hint and his L41
  line dropped (silent there); Varga's line on the hybrid pilots' cost in L36's briefing; the ambience bed names in the
  level drafts (`jovian-sky`, `aurelia`, `io-refinery`, `callisto-fortress`, `belt-open`); Vorne's motif cut at L37's
  turn; Okafor's L42 answer no longer repeats "I remember".
- 2026-10-31 (user, [round 72](../../concept-rounds/round-72/README.md) closed; page answers): the act approved as
  drafted (D.1-D.8, the seven levels and this README; `design: approved`). **Questions Q1-Q12 as drafted**: Q1 c (the
  mirror's `reflect`), Q2 a (the foreground banks over `air`), Q3 a with c's backdrop (the three-way war as scripted
  prey), Q4 a (the falling city at Level 37's end), Q5 a (the Honour Guard staggered, then together), Q6 a (the
  Ascendant bow-on, the lifeboat's escape), Q7 b (the middle arsenal: the Plasma Arc, the Reflex Shield, Composite V,
  the IFF spoofer; Time Dilation, the Hunter drone and the Bastion Shield go to Act 7), Q8 a (the captured 25 MW
  Crucible), Q9 a (gunmetal regulars, a gloss-black elite, teal-seamed hybrids), Q10 a (a mood per level), Q11 a (the
  intro by Okafor with Vorne intercepted on page 3), Q12 a (the Silence named in play at Level 41); the alternatives
  are rejected. **Judgement calls J1-J16 kept**: J11 the Crucible bought at the Level 40 visit with Composite V moved
  to the Level 41 visit (the Level 39 swap rejected), J13 the kill-ratio secondaries of Levels 40 and 42 kept, J14
  Level 40's primary `reach-end` (the `boss` primary rejected), J15 the scales outside the band accepted, J16 the
  Silence's rule and the uplink's feel as drafted. **J12 rename**: Level 37 is **"Fall of Aurelia"**
  (`level-37-fall-of-aurelia`; Act 5's track "Gilded Cage" keeps its name). Art: A.14 a (the title still, "the dive";
  the sheet and b rejected, `art: chosen`), A.13 b (the falling city breaks up: two city layers, an authored jagged
  cut line). Music M.1 a, **M.2 b**, M.3 a, M.4 a; voices V.1-V.5 (see the voice plan). **User's note on A.7** (the
  explosions look strange with a sharp dark edge on light backgrounds): a mockup artifact, see *Implementation*.
- 2026-10-31 (user, [round 73](../../concept-rounds/round-73/README.md) closed; page answers): part A's looks and sounds
  as recommended: the sounds S.1-S.5 (heat, break, regrow, dome hit a; the absorbed tick), the lightning strike a, the
  Reflex bolt a, all game captures and the production art accepted (F.1-F.7), R.1-R.3 accepted, C.1-C.21 accepted;
  **A.3 b** the soot puff under the explosions' flash (Act 6 only or everywhere is part B's call, see *Implementation*);
  **R.4 b** Vorne's shout is the new shouted render lowered to about 104 Hz.
- 2026-10-31 (M10 part B, step B2): the act's [data.yaml](data.yaml) holds the intro (title card and four pages,
  rendered into *Act intro and outro*) and Act 7's `next_act`, moved from Act 5's: with Level 36 built the branch's
  release ends after Level 36 (`CampaignRoute.releaseEnd`: the next level is not built) until part H moves it to
  Level 42. Page 3's motif is a new briefing page key, `music: motif` (the game's half is step B4's).
