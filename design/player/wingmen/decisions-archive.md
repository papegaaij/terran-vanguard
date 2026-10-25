# Wingmen and drones — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Draft uses a separate escort slot (pending user confirmation).
- 2026-09-30: Rook's timeline aligned with story and campaign: escort slot at L08, missing
  L27–L29 (heavy drone only), back from L30. Drone unlock levels added.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Rook flies the variant-C (blended manta) airframe from ship concept round 01, in his own colours; it replaces the placeholder "F-9 Kestrel".
- 2026-09-30: Concept round 02: Rook's craft scheme **A "Ember"** (dark slate, orange/yellow accents) chosen; B "Jade" rejected.
- 2026-10-01: The separate escort slot for Rook is confirmed by the user. Rook's craft uses 5 banking frames like the player.
- 2026-10-01: Rook's AI specified (formations, reactions, targeting, guns derived from player weapons, eject/return, radio bark triggers); Warden heavy drone specified.
- 2026-10-01: Concept round 09: Rook's 5 banking frames chosen.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-06: M5 part A, user decisions: **D2 = a**, Rook's kills pay like the player's (bounty,
  score, chain, objectives; the Airstrike's precedent), rejected b (no score or chain: two kill
  paths, broken chains) and c (only the player's kills pay: hiring him would cost credits);
  **D3 = b**, a hangar repair line for Rook like the player's, unrepaired armour stays missing and
  the launch warns below 50 %, rejected a (always repaired, billed at the next visit: a hidden
  charge) and c (free repairs: one sink less). It replaces "he returns next level at full armour;
  repairs charged in the hangar after the level", which contradicted the saves, retry and hangar
  documents. **D4 = a**, his guns are owned like the player's weapons (an escort inventory: buy at
  60 %, upgrade each L1–L5, fit one, sell at 60 %, undo in the visit), rejected b (one gun at a
  time: switching punished) and c (one Rook level for every gun: departs from the per-gun
  upgrades).
- 2026-10-06: M5 part A defaults (stated with the decisions): save format 3 with `escort`; Rook
  flies only in levels from 08 on (not in Act 1 replays, not on `--level` below 8); the debug
  option `--escort`; the slot always holds Rook in Act 2; the barks are event lines, 8 s apart, and
  a scripted Rook line within the spacing suppresses the bark; his scripted lines still play after
  he ejects, lines that need him flying do not; Lampreys latch only on the player, gusts push him;
  his shots pass through allies; his DPS is printed but not counted against the reference DPS, and
  the balance plan buys his guns and repairs from L08; Rook's production sprite is 42×42 (the
  concept note's 40×40 is corrected); the drones and the Warden are tagged with their acts.
- 2026-10-06: M5 part A details (main-agent choices, for review with concept round 28): his
  repairs cost the difficulty's repair cost per point like the player's (free / 5 / 10 on easy /
  medium / hard) instead of a flat 10 cr per point, as D3 makes it the player's kind of repair line;
  at 0 armour after an ejection he is grounded until any repair; a retry gives him the player's
  armour floor; Trail wins over Wide, and the mirrored slot at the field's edges; enemies never aim
  at him; no overdrive and no Targeting computer bonus for his guns; a pod gun fires as the pod on
  his side; the eject pod drifts to the nearer side edge at 120 px/s; a new campaign puts him on
  the left (Level 08's "I'm on your left"); the bark variants (27 lines), their expressions, the
  replace-by-priority wait and the variant rotation; the numbers, guns and barks in
  [data.yaml](data.yaml) with a nose `muzzle` for his one gun (written as a proposal first and
  created together with its loader, since the loader rejects a data file it does not know).
- 2026-10-06: The user accepted the main agent's four readings of D3 and the M5 part A gaps: Rook's repairs
  cost the difficulty's repair cost per point; at 0 armour after an ejection he is grounded until any
  repair; a retry gives him the player's 50 % armour floor; a new campaign puts him on the left.
- 2026-10-06: M5 part A, Rook in the game (main-agent choices, for review with concept round 28):
  his craft is drawn like the player's (bank frames, two engine flames, runtime shadow, muzzle
  flash at his nose, white hit flash, smoke below 30 %), with the round-09 Ember frames as
  placeholders and a drawn warm glow and pod until the production sprites exist (names under *In
  the game*). The barks: a bark is dropped within 8 s of a timed Rook line *either side* of it (a
  scripted line due soon, or one still on the radio), its variant counts every bark queued in the
  attempt, the kill streak counts the player's and his kills in a 5 s window and starts over after
  ten, an attempt that starts below 30 % armour does not bark about it, and a boss-checkpoint retry
  starts the counts over like a retry. A rear wave's bark fires 1.5 s before its entry, a sides
  wave's at its entry, from the level script's waves.
- 2026-10-06: User decision: Rook **fires whenever the player fires**, at his gun's rate, target or
  not; his cone and target priority only pick his target (a homing gun's initial lock). Before, he
  fired only with a target in his ±15° cone: a Level 02 run gave him 27 volleys against the
  player's 850 in 90 s. A headless Level 02 run now (medium, starter loadout, the test autopilot
  firing throughout, 90 s) gives his L1 Autocannon 425 volleys against the player's 850 (5 a second
  from the end of the launch; his L3 Missiles 255).
  Test `WingmanTest.heFiresWheneverThePlayerFiresEvenWithoutATargetAtHisGunsRate`.
- 2026-10-06: User decisions: his side is a hangar setting outside the visit's undo (see
  [hangar](../../ui/hangar/README.md#decisions)); and the `--escort` debug option is never saved
  (a failed `--escort` run had autosaved him as hired): it now lives beside the campaign's gear and
  only the flights take it (`Campaign.escortFlight`; test
  `EscortTest.theEscortDebugOptionIsNeverSavedNorKept`).
- 2026-10-06: M5 part A, Rook's production sprites (straight to production from the chosen
  round-09 Ember concept, no a/b round; for review in concept round 28): `tools/art/rook.py`
  renders the round-09 model unchanged at 42×42 through the Stormhawk's camera and banking angles
  (±15° and ±30°, the concept's ±14° and ±28° rounded to the player's so the pair banks alike),
  32 colours; a warm 8×14 additive flame loop (3 frames) under the hull at the nozzles' rear ends
  (`pivots/rook.json`); the eject pod as a 16×16 slate capsule with Rook's yellow nose band and a
  red beacon, tumbling clockwise in four 45° steps with the beacon lit in one frame (a blink per
  loop; the beacon is red so it reads apart from the yellow and orange trim). The pod was drawn
  once, not as the a/b concepts the art-track item named. They replace the placeholder frames cut
  from the concept (`importPlaceholders` skips them by their `Source` chunk); the shared atlas
  stays one 2048² page (73 %). In a `--bench` run (Levels 02 and 07, Xvfb) his frames bank, the
  flames sit on the mounts and the HUD's escort icon shows the new frame; the pod could not be
  seen in the game, since no bench run made him eject.
- 2026-10-06: User decision: Rook **reacts to about 70 %** of the bullets he predicts (`dodge.reacts`
  0.7; before, he reacted to every one), each bullet decided once by his own generator (seeded from
  the sortie's seed, its state in the boss checkpoint), so a stray bullet hits him now and then. The
  capture of round 28 showed three more AI faults, fixed with it: a segment chain's members carried
  no rear entry, so a Coilwyrm from the rear or a loop-back never put him in Trail (a chain now counts
  by the edge it entered from last); his slot ignored the air enemies' bodies, so he flew into them
  (closest −18 px; now the 14 px body clearance, his way included: he no longer crosses a body to
  reach a slot beyond it); and at the edges the 40 px minimum could give way (now pushed out along
  the margin). A bullet he reacts to stays decided while he sidesteps it (deciding it afresh at
  every prediction drew again and would have missed one only 0.3⁴ of the time). Measured headless
  (the test autopilot, the plan's fit, his L1 Autocannon, seeds 1–4), hits and armour left before →
  after: Level 02 medium 0–2 → 2–3 (68–72), hard 3–5 (60–68); Level 06 medium 0–5 → 0–4 (64–80),
  hard 1–7 → 4–9 (44–64); Level 07 medium 3–8 (48–68) → 5–11 (36–60), hard 11–19 (2–36) → 10–20
  (0–40) with an eject in 2 of 4 runs (3 of seeds 1–8); never closer than 40.0 px to the player; no
  body contact (all hits are bullets, closest gap +2.9 px); Trail now on Level 06's rear wave
  (134 s) in every run (the two loop-back Coilwyrms died before looping back; the loop-back is
  tested). The share hardly sets his wear: reacting to every bullet, Level 07 hard still ejects in 3
  of 8 runs, as the 0.25 s delay inside the 0.4 s look-ahead leaves him 0.05–0.15 s to clear one; a
  0.6 s look-ahead (with the 70 %) measured no eject in those 8 runs (armour 4–50) and about the same
  medium wear. `reacts` and the clearance stay as they are; the look-ahead is for the user. Tests in
  `WingmanTest`.
- 2026-10-06: The eject, after the round-28 capture (the pod drifted to the nearer edge and passed
  under the player's ship; the eject bark fell under the 8 s spacing like any bark): the pod
  takes the **nearer side edge unless the ship is in the way** (on that side and within **36 px**
  of the pod's line; then the other edge; `Wingman.podSide`, tests in `WingmanPodTest`), is drawn
  **over the explosion after 0.1 s** with a red beacon **glow** and a light smoke **trail**
  (`WingmanLooks.drawOver`); and the **eject bark is urgent**: exempt from the spacing, the scripted
  Rook lines and going stale, it cuts his waiting or playing bark and plays at once (`Barks`,
  `RadioQueue.cancel`; tests in `BarksTest`). Checked in the game on a private Xvfb
  ([rook-eject-capture-r28-b.png](concept/rook-eject-capture-r28-b.png), Level 07 medium with
  scratch data so he ejects: armour 2, hitbox 40, no dodging): the pod clears the explosion's edge
  within 0.1–0.2 s, its beacon glow blinks red and the grey puffs trace its path to the right edge
  away from the ship; "I'm out, I'm out! She's all yours, Lancer!" opens on the radio at once. The
  16×16 pod is small at 1× but reads by its trail and blink, so it was not re-rendered larger.
- 2026-10-06: Look-ahead raised from 0.4 to 0.6 s (user): with 0.4 s he ejected in 3 of 8 hard Level 07
  runs even when reacting to every bullet (the 0.25 s delay left 0.05–0.15 s to move); at 0.6 s the
  agent's 8 hard runs had no ejects (armour 4–50 left) and medium wear stayed about the same.
- 2026-10-06: Concept round 28 closed (user: everything accepted). Rook's production sprites
  (`tools/art/rook.py`: the five banking frames at ±15 / ±30°, the 3-frame flame loop, the 16×16
  eject pod drawn once, not as a/b) approved as **final**, so with his shots in the base weapons'
  final sprites every asset he uses is final: `art: final` (the drones and the Warden are `idea`
  rows with no art yet, for their acts). The captures accepted, the pod after its fixes included.
  The AI approved as built, and the round's question answered: the **flank threat** (the Wide
  formation and target priority 2) is **plain distance**, an enemy within 160 px of his centre
  (a circle, so an enemy just ahead counts too), not "closing from the sides" (the Behaviour row
  reworded; the data's `flank_distance` comment already said so). His 27 barks accepted as
  rendered, the four pinned takes included. The part A decisions checked as carried out. Ticked
  after checking the code and tests: the hire and save field, where he flies, the AI, collisions,
  the guns, his kills, retry and checkpoint, `--escort`, the `WingmenData` loader and validator,
  and his shots in the base weapons' families (the item's "later: art track" no longer applies:
  `WeaponLooks` draws them with the base weapons' final sprites). Still open, so the document stays
  `in-progress`: rendering the guns and barks tables from the data (split off the loader item, no
  part named yet), and the balance item (`BalanceTest` printing his DPS, the balance plan buying his
  guns and repairs from L08, the autopilot and `ActPlaythroughTest` flying with him), not built in
  part A and tagged **later: M5 part I**, whose close-out runs those tests for Act 2 with Rook.
- 2026-10-06: M5 part B, user decision **D4 = a**: Level 08's two Rook lines get their mechanics,
  a radio event **`escort-first-kill`** (his first kill per attempt while he flies, never after an
  eject; once per attempt, the flag in the state hash and the boss checkpoint) and **`{side}`** in
  a radio line (one voiced take per side, played by his side setting). Rejected: (b) no new
  mechanics, the first-kill line on the player's first kill and a neutral "I'm on your wing" (a
  weaker beat), and (c) only `{side}`. Our readings: the event fires on a kill by his own shot or blast
  only; a cue on it counts as a scripted Rook line
  for the barks' spacing; in a level he does not fly in it never plays.
- 2026-10-06: M5 part B built the two mechanics of D4: `escort-first-kill` (the simulation's kill
  by his mount's shot or blast, a set piece's vital part included, while he flies; the flag in the
  state hash and the boss checkpoint; the level screen counts it as a scripted Rook line for the
  barks, withdrawing a bark queued with the same kill) and `{side}` (the voice line list has both
  takes; the level screen shows and plays the one of his side setting, left when he does not fly).
- 2026-10-07: Level 08's capture (round 30) showed Rook beneath the player from t≈141 to the level's
  end, not in a formation slot. Not a stale rear edge (he never was in Trail; no unit kept a rear or
  sides entry): back from the left wall with the player low on the screen (y≈69), his way from the
  mirrored slot to his own passed beneath the player, where the 40 px minimum distance reaches below
  the bottom margin, so the push-out held him in that corner for good. When the side his straight way
  passes the player on has no room inside the margins at 40 px and the other side has, he now steers
  round the player that other way (8 px outside the 40 px, a point 45° ahead on the circle): over
  the player when the way beneath is closed. Otherwise his flight is unchanged (PacingTest's Level
  08 runs with Rook show the same pauses as before). Replayed headless with the
  capture's flight plan, he is back in his slot within a second. Test:
  `WingmanTest.heGoesRoundOverThePlayerToHisOwnSlotWhenTheWayBeneathIsClosed` (fails without the
  detour).
- 2026-10-07: M5 part C (user decisions D7 = a and defaults): Rook skips hardened targets
  (Level 09's Hive Nodes) unless his gun is anti-ground (the Mortar); the launch warning for a
  `required` anti-ground trait counts his Mortar while he is not grounded. His scripted (timed)
  radio lines carry `requires: escort`, so they stay silent while he is grounded.
- 2026-10-07: Rook aims his lobbed gun (user, option a of the Level 09 balance issue): the Mortar
  always landed 200 px ahead of him, so flying in formation beside the player his shells fell about
  170 px past the Hive Node the player was bombing and added next to nothing. Now, with a ground
  target (his pick; hardened ones only with this anti-ground gun) within the lob's 200 px, his
  shells land on it at any distance, their flight time (0.6 s at 200 px) and drawn arc scaled to the
  distance; his pick for the Mortar looks anywhere ahead of him within those 200 px instead of the
  30° cone, which a target beside the player never enters within range. Without a target the
  shells fly as before. The player's own Hammer Mortar keeps its fixed range; his formation, dodge
  and look-ahead are unchanged. Test: `WingmanTest.heLobsHisMortarOntoTheGroundTargetThePlayerIsOver`.
- 2026-10-07: [Concept round 31](../../concept-rounds/round-31/README.md) (user, 2026-10-07): the
  build choice (b) **confirmed**: with a lobbed gun Rook picks ground targets anywhere ahead of him
  within 200 px, not only in his 30° cone (beyond the brief "aim the lobbed gun", kept as built).
- 2026-10-08: M5 part D (Level 10; stated defaults with the user's decisions D1–D12): no change to
  Rook's AI. His Trail and rear bark key to a Wraith ambush's and a flock loop-back's **re-entry**
  at the bottom edge, as a chain's loop-back (their waves enter at the top). Level 10's scripted
  "Six o'clock, Lancer!" at t=50 is dropped (his bark repeats it word for word); his flock line
  plays on the new `first-loop-back` event and counts for the bark spacing. A `WingmanTest` case
  covers Trail near the Wraiths' hold band. The balance plan's L10 visit refits his Autocannon
  (D7 = a, a `fit` action), as the Mortar has nothing to hit there.
- 2026-10-08: M5 part E (Level 11; stated defaults with the user's decisions E1–E10): he skips `sub`
  targets (E2 = a: none of his guns reaches the layer), keeps out of a telegraphed or slamming lane in
  the Kraken's arena, and a slam hits him; otherwise no change to his AI. The balance plan's L11 visit
  refits his Mortar. Our reading, for review in round 33: when the player sits in a telegraphed lane
  Rook waits beside it rather than following him in.
- 2026-10-09: M5 part F (user decision F3 = a and the part's stated defaults): Level 12's wind
  pushes Rook like the ship, adding to his position with no change to his AI; a latched Lamprey is
  out of his reach (his shots pass, his pick skips it); he does not collect sondes; his t=102 line
  is dropped for the rear bark, his t=44 line kept, and the `first-latch` line counts for the bark
  spacing. The balance plan's L12 visit refits his Autocannon (the Mortar has nothing to hit).
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses' recommendations, **accepted in round 34 (2026-10-12)**: in Level 13 Rook does not guard the relay and keeps his Autocannon, his draft rear and side lines give way to the barks; in Level 14 he flies Trail for the maw's Wraiths (H3 = a) and has one event line in phase 3.
- 2026-10-12: M5 part I (close-out): the balance item is built — `BalanceTest`'s sheet and
  `tools/balance.py` print his DPS (12 with the Autocannon L1, 15 with the Mortar L1), and
  `ActPlaythroughTest` flies Acts 1 and 2 as one campaign with him on every difficulty, paying his
  repairs after the ship's. Seen there: he ejects in Level 14's Spire fight on medium and hard and on
  hard also in Levels 10 and 11 (80-point repairs at the visits after), with every level still won at
  the first attempt. The guns and barks tables' rendering is retagged **later: M6** (tooling); with
  the drone items tagged Acts 3–5 the document is `done`.
- 2026-10-12: M6 part D (Q14 = b, accepted in round 35 (2026-10-12): the guns and barks tables
  are rendered from the data (`rook-guns`, `rook-barks`); the guns' Available cell reads the data's
  `act 2` (he joins at L08) instead of the hand-written L08.
- 2026-10-12: Back to `design: review` until [concept round 35](../../concept-rounds/round-35/README.md) closes (C5 and Q14, the rendered tables).
- 2026-10-12 (user, round 35): C5 (the guns' Available cell `act 2`, rendered from the data) and
  Q14 = b (Rook's guns and barks tables and the act outro's page table rendered) accepted;
  `design: approved`.
- 2026-10-12 (overnight design phase): the Act 3 drones specified as draft rows with their own
  sections: the light drone (orbit 36 px at 2.4 s a turn, fire at a convergence point 140 px ahead,
  bullets and contacts cost hits, break and rebuild, walls shrink its orbit; its shop numbers in the
  [Light Drone Bay](../weapons/light-drone-bay/README.md)) and the rear-guard drone (40 px behind,
  fires backwards, 9 → 26 rear DPS, the same block and rebuild; behind stays the screen's bottom in
  the reverse scroll). Accepted in round 36 (2026-10-13); the approved parts of this
  document are unchanged.
- 2026-10-13 (user, round 36): the Act 3 drones approved (D.26): the light drone (orbit 36 px,
  convergence 140 px, block, break and rebuild; the disc drone of G.2 = a) and the rear-guard drone
  (its bay gets a weapons document in part G). M17 = a kept: Rook flies all seven Act 3 levels from
  the escort slot, no new gun; his Level 20 quiet beat plays without `requires: escort` (Q.20d).
- 2026-10-19: Rook makes no bark once the level is won.
- 2026-10-20 (M7 part G, step G2): the rear-guard drone's shop numbers moved to the new [Rear-guard Drone
  Bay](../weapons/rear-guard-drone-bay/README.md) document, linked from here. Rook's rear bark in a
  reversed section (Level 20, where "behind" is the top of the screen) gets three "Up top!" variants
  (`top_lines` of the rear-wave trigger, rendered into the barks table), accepted in round 43 (2026-10-20); it stays silent for the bottom entries there.
