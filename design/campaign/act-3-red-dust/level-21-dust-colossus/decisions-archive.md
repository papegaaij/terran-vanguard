# Level 21 – Dust Colossus — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-12 (overnight design phase): promoted from the [Act 3](../README.md) roster to a draft
  level document. Taken as the recommended options, accepted in round 36 (2026-10-13): M7 = a
  (the `buried` layer for the Burrowers and the Colossus's body), M10 = b (the Dust Kite as
  popcorn), M12 = a (a `bounty_scale` per level), M14 = a (the Choir's first words at the
  Colossus's death), M17 = a (Rook flies, one dry joke), M18 = a (reference DPS 100), M19 (hand
  tables, no data). Our proposals: the six sections (rim, inner wall, dune sea, lime hollow, the
  arena, settling dust) and speeds; the arena crawling at 20 px/s on a seamless tile with the
  clock halting at its end; the ridge pass as the arena's lesson; 107 enemies (≈ 66 a minute,
  Level 14's level, not the L15–L17 drafts' 85–90) with side and rear waves; the survey-crawler
  secret; the kill-ratio secondary without a radio line; the death's five lines as `CLOSING`
  lines and a 32 s settling-dust section; Vorne's motif muted; Level 14's act-end order with the
  release's end moved to Level 21; `bounty_scale` ≈ 0.80 and the boss at 300 in Act 1 terms
  (22.7 % of the budget, the perfect-run bound) instead of the spec's 790 at 0.40.
- 2026-10-12 (overnight, harmonised with the act README): Ares Warden merged into Ares Control, the
  colony's one voice across the act (his `CLOSING` dinner line and the speakers' note; accepted in round 36 (2026-10-13)); the density notes point to the act's band of 60–72 a minute (≈ 66 and
  the scale ≈ 0.80 kept); the music already matches the act (track 9 for the approach, Vorne's
  motif muted).
- 2026-10-13 (user, round 36): approved (D.8), M10, M14 and M17 kept. Q.21 all yes: the boss's
  share 22.7 % (300 in Act 1 terms at scale 0.80), the arena's crawl as a seamless tile with the clock
  halting at t=245, the 65 % kill-ratio secondary, Vorne's motif muted, the five closing lines (≈ 34
  s, the debrief waits), ≈ 66 enemies a minute. The Colossus is the "Sinkhole colony" (B.2 = b).
- 2026-10-20 (user, round 43, H.1-H.7, so part H starts decided; see [round 43](../../../concept-rounds/round-43/README.md#parts-hs-questions)): **H.1** yes: count-neutral swaps as Level 20 (Kite V-wings
  become swarms, 3-4 Kite groups become Needler/Stinger firing groups, about 107 enemies and 66 a minute), more only if
  the measured floor fails. **H.2** keep Rook's Autocannon at Level 21. **H.3** yes: drop the t=101 Stinger V-wing (or fold
  its four into earlier groups) so the screen clears while the Choir sings, no incoming box (the warning stays at
  t=105), using the third allowed gap. **H.4** phase 1's calibration band is 0.8-1.2x the Kraken's 11.4 per 10 s with the
  random-dodge harness. **H.5** yes: the boss keeps its 22.7 % share (bounty = 0.227 x 2,709 / (2.56 x scale), about 320
  at 0.75) while the perfect run stays at least 1.4x. **H.6** the arena tile repeats at 512 px and section 6's tiles start
  at the next seam (at most 20 s); the collapsed crater and the furrow are props placed at the kill. **H.7** all yes:
  (a) the ease 140 to 20 over the arena's first 2 s (t=105-107), (b) the suction pulls no pickups, (c) debris stops the
  player's shots, not the enemy's bullets, (d) the release's end reads "ACTS I-III COMPLETE" and "ACT IV - DEEP WATER.
  EUROPA. COMING SOON.", Continue "...AT THE END OF ACT III", the next act's name from a `next_act` key in Act 3's data,
  (e) the Level 3 strip's boss mark is the boss's silhouette at its time, drawn for Level 14 too, (f) the ridge pass as a
  level key `ridge {t, seconds, from, to, depth}` read by `RidgeLooks` (Q.4: Level 20's track moved into its data), (g)
  the usual 5 s launch from the bottom edge over the rim's CDF line, (h) a phase-2 point is fixed when its edge warning
  starts (3 s before the rise), the front one at its tell.
- 2026-10-20 (step PH2, M7 part H; accepted in round 44 (2026-10-20)): the level's `data.yaml` from the approved
  tables with round 43's answers: the arena a section of speed 20 with a 2 s `ramp` that crawls because its boss
  breaches; the waves count-neutral (96 + 10 = 106, the t=101 V-wing folded in), the ridge pass on the `ridge` key
  (t=64, 8 s, right to left, 150 px down), the usual 5 s launch, `bounty_scale` 0.75 and the boss 320 (paper values for
  step H4). Defaults: the survey crawler's slide 20 HP on every difficulty (hard 28 needs a trigger's hard key), no
  no-surface zones yet (the simulation refuses them in a level with an arena), the basin rumble from t=64 with a 30 s
  swell, cut at the kill.
- 2026-10-20 (step H4, M7 part H; accepted in round 44 (2026-10-20)): the level flown whole by the autopilot
  (`Level21Test`, `ColossusPlay`) and measured with the level report. The fight: 101.7 s at medium at the spec's HP,
  the par 102; the haul: `bounty_scale` 0.74 (typical +0.7 %, perfect 1.43 ×), the boss 325 for H.5's 22.7 %; the beam
  era's small TTK 1.42 × Level 10's (ok); the floor (1.78) and the pacing (5 gaps at medium) fail with the
  count-neutral waves: not reworked here (an open question), measured but not asserted by `PacingTest` and
  `ActFloorTest`.
- 2026-10-20 (steps HA and HB, M7 part H; accepted in round 44 (2026-10-20)): the game side: the crawling backdrop
  (the arena's 512 px tile, the settled floor from the next seam, up to about 27 s after the kill against H.6's 20), the
  Colossus's looks, a 2 px shake on the ridge pass (also Level 20's: the game's first shake), the boss's silhouette on
  the Level 3 strip for every boss level, the release's end texts from `next_act`, the nine round-44 sounds as
  provisional picks (all *a*, the ridge pass *b*), the basin rumble's swell and cut and the death's own sound. Asked in
  round 44: the wave rework (Q.1), phase 1's rings (Q.2), the par (Q.3), the erupted mouth's safe spot (Q.4), the
  crawler's hard key (Q.5), the nests' zones (Q.6), the Choir's take (Q.7) and the small defaults (Q.8).
- 2026-10-20 (user, round 44, closed): the level's rows, as recommended except K.b and K.c. **F.a, F.b** the backdrop and the props approved as final; **V.a-V.i** the nine voices accepted; **K.a, K.d** the approach and the act end accepted; **K.b change**: "What are those turrets popping up out of the ground? I think they should be part of the Colossus, but then they should be placed on its spine, not like this in the sand. They have the same moving issue as the creature itself (see below).  I think the creature also needs something extras. The few small blasts don't do such a large creature justice. Think of something to add." **K.c change**: "The ground moves underneath the Colossus. When the Colossus is out of the ground, the background cannot move, or the Colossus must move together with the ground." Planned (steps SIM-K, ART-K, GK, L21R; the K.b and K.c clips are re-recorded after them): the crawl halts while the Colossus is out of the ground, the vents become organs on its spine, a death throe is added (see the [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md#decisions)). **K.e** accept (the death's look, after GFX3); **K.f** (a)-(e) the autopilot's run in numbers accepted; **K.g** keep the HUD's kills against the 65 % target ("KILLS n / 70"); **K.h** count the secondary as the data says (the boss and its debris not counted: 106 enemies, the target 69; a small sim/content fix planned for part I). **B.a-B.f** every build choice stands as built (the breach machinery, the vents, the data, the measured fight, the game side, the sound and the act end). **Q.1** rework the waves to the rules (about 150-170 units at medium, more firing groups as Level 20, the two Kite swarms held longer, measured to `PacingTest` and `ActFloorTest`, the haul re-scaled; planned, not started); **Q.5** add the hard-HP key to a trigger (hard's crawler 28 HP; planned); **Q.6** leave the nests without `no_surface` zones; **Q.7** the Choir's speech stays one take (the joins and the layering go to the pool of nice-to-haves); **Q.8** (a)-(d) all yes. The sounds: the nine Colossus sounds S.a-S.f **a** (the ridge pass mirrored for this level's right-to-left track), S.j no mouth's death sound.
- 2026-10-20 (step L21R, round 44's Q.1, Q.5 and K.h built; pending the user's review): the waves reworked to the
  Act 2+ rules as Level 20's (see *Waves*): 23 firing groups between the anchors, Needler rings held over the
  t=9.5 and t=30 Kite swarms, a slow Stinger stream with the t=64 Kites through the ridge pass; 168 enemies at medium
  (2.55 on the screen, 2 stretches over 2 s; easy 2 over 3 s), L21 out of `PacingTest.PENDING_WAVES`. Tried and
  dropped: Stinger swarms (a `swarm` needs a flock; only Kites and Motes have one), a Needler ring and a Needler pincer
  through the ridge pass (killed by t≈66.6). The haul re-scaled to 0.50 (+1.1 %), the boss's bounty 480 (H.5's 22.7 %).
  Hard: the new pairs single, the t=92.75 and t=95.75 pairs left out and the ridge stream 2 Stingers, since the plan's
  fit without Rook died at t≈104.8 on every retry of one seed. The crawler's hard 28 HP is the destructible's `hard:
  {hp}` (a new key: data, `GroundTargetsBlock.Placements.hp`, `GroundRules`, the record's checks). The secondary leaves
  the boss out (`boss_counted: false`, SIM-K): 168 enemies, the target 110. The level's timing with SIM-K's crawl halt
  re-checked (the fight ends ≈ t=213 at medium, before the arena's t=245).
- 2026-10-20 (step SIM-K, round 44 K.c): the arena's crawl is 0 from the Colossus's rise to the end of its dive (eased over 0.5 s), through the tell and the marker too (`BreachArena.groundStill`), so the arena effectively never crawls in the fight: only in the intro, about 0.5 s in phase 3's gaps and after the kill. The secondary no longer counts the boss (`boss_counted: false`).
- 2026-10-20 (step GK): the ground holds still under a breach (`LevelRenderer.drawnScroll`), the radio's cut phase line is withdrawn at the kill; the greenhouse pad of Level 17 is drawn and claimed.
- 2026-10-20 (step L21R): 168 enemies at medium (easy 143, hard 152), floor 2.55, 2 gaps over 2 s, fight 104 s (par 104), phase 3 from 45 % (about 3 eruptions), `bounty_scale` 0.50, boss bounty 480.
- 2026-10-20 (step R45): the Colossus's fight in play (round 45 B rows), the spine and the death throe (A rows) and hard's Act 3 economy (C.1) are questions in [round 45](../../../concept-rounds/round-45/README.md).
