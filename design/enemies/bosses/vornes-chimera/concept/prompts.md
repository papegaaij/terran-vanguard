# Vorne's Chimera – concept prompts

Round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; pending the user's review). Generator:
`tools/concept/act7_r84.py chimera` (the body an SDF render on the frozen `render` package, grown from round 72's chosen lifeboat; the
Ascendant's production parts as grafts; Level 48's citadel drawn in the same file).

## vornes-chimera-r84-a

"Swollen pod". **Prompt:** a late-90s pre-rendered CGI boss sprite seen from straight above, facing down: Vorne's
Vrell lifeboat (a round teal-black seed-pod with a gold escape capsule half swallowed in it) grown three times its
size into a flying chimera of glossy teal-black tissue with teal seams; two swollen tissue shoulders lipping over two
grafted gold Ascendancy gun batteries with silver-gold crescent mirror panels in front of them; the Ascendant's gold
ribs fanned over its back from a gold missile rack on the spine; gold bay doors in the belly; a short tail of three
tendrils; the gold dulled and veined with teal; the capsule's canopy the one warm amber spot; strong top-left rim
light; sickly gold and teal. Phase 2: the gold parts burst, three teal tissue sacs with lime cores in the torn gold
sockets. Phase 3: the capsule's canopy torn open, a dark silhouette grown into the seat against warm cockpit light,
a faint gold hex shimmer over it.
**Negative prompt:** photorealism, modern PBR, flat vector, cartoon, text, watermark, clipped glow.

## vornes-chimera-r84-b

"Thorned graft". As a, but the pod narrower and spikier: the gold batteries held out on two bone-white thorned arms
with hooked claws past them, plates of the Ascendant's gold hull embedded in the tissue like armour, a long thorned
tail with bone spines; more gold (about a third of the silhouette in phase 1).

## vornes-chimera-final-r93-a

**Vorne's Chimera, production** (round 93, M11 part F, step ART-F, 2026-11-06; pending the user's review). Files:
`vornes-chimera-final-r93-a.png` (sheet) and `.gif` (a scripted pass at 1×). Generator: `python3
tools/art/act7_citadel.py chimera` (the body: `chimera_model("b")` from `tools/concept/act7_r84.py`, unchanged, with
more embedded gold plates and torn sockets; the grafts: `tools/art/ascendant.py`'s production frames, graded).

- **Prompt:** as [vornes-chimera-r84-b](#vornes-chimera-r84-b), production quality, straight top-down, 200×210 px in
  play: the lifeboat's seed-pod grown into a narrow, spiky craft of glossy teal-black tissue with teal (#00ff9a) seams,
  the gold escape capsule half swallowed in its centre, plates of the Ascendant's gold hull embedded like armour, the
  gold Ascendancy batteries with their silver-gold crescent mirror panels held out on bone-white thorned arms, the
  missile rack on the spine, the belly bay's gold doors, a long thorned tail; the gold dulled, a step greener and
  veined with teal (sickly gold and teal), about 35 % of the silhouette gold in phase 1; strong top-left rim light.
  Phase 2: the three dead gold sockets torn open, bent gold shards round them, tissue sacs with lime cores bursting
  out. Phase 3: the canopy open, a dark silhouette grown into the seat against warm cockpit light, a faint gold hex
  shimmer over it. Banking: rolled about its long axis ±12°, the light fixed.
- **Effects:** every tell and glow a smooth additive light fading to 0 inside its canvas: the nose's gold-white glow,
  the ports' gold flashes, the centre's lime-teal pulse, the far flank's teal flare, a teal glow on the bottom edge
  where it will rise, the fusion's teal light leaking from every gold seam then bursting from the sockets, the capsule
  bursting gold-white with torn teal fibres (never a screen flash).
- **Negative prompt:** photorealism, modern PBR, flat vector, cartoon, text, watermark, ordered dither, clipped glow.

## chimera-fight-r93-a

Captures of the game itself (round 93, M11 part F, step EF5, 2026-11-07), not generated art: one real-time run of Level
48 with sound, the balance plan's fit and Rook's Mortar L1, made as [Level 48's
captures](../../../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/prompts.md#game-captures-r93-a). Files:
`chimera-fight-r93-a.mp4` (the whole fight, the whole screen with the HUD, real time, with sound: level real seconds
183.5–267, from the rise through Vorne's last line and the burst to the clock running on) and the clips below (the play
field). Seen in this run (video seconds of the fight file, then real level seconds): the arrival and the rise out of
the well 2.5 (186.0), in place 6.5 (190.0); phase 1 to 24.0 (207.5): the Dart stream, the lance (≈ 204), the gold fan;
the fusion 24.0–26.0; phase 2 to 56.2 (239.7) with three rear sweeps (tells at ≈ 214, 225, 234; out of the top edge,
the bottom-edge glow, the climb up the far side); phase 3 to K at ≈ 70.3 (253.8: the bar gone, the shots fading); the
hang while line 9 plays ≈ 70.3–74.8; the burst at 74.8 (258.3); the clock runs on at ≈ 78 (≈ 262).

## rear-sweep-capture-r93-a

Round 93, a capture of the game (step EF5, [how](#chimera-fight-r93-a), with sound): the first rear sweep (t=213.5–220.5):
the far flank's teal flare and Rook's call, the drop out of the top edge, the glow at the bottom edge, the climb up the
side the ship is not on, firing its spore pairs, and back to its station.

## fusion-capture-r93-a

Round 93, a capture of the game (step EF5, [how](#chimera-fight-r93-a), with sound): the end of phase 1 (t=205–212.5): the
last gold part dies, the fusion's green light, the tissue closing over the dead gold and the three sacs growing out of
the sockets, the pain line and the shout.

## capsule-capture-r93-a

Round 93, a capture of the game (step EF5, [how](#chimera-fight-r93-a), with sound): the start of phase 3 (t=238–245):
the last sac dies at the end of a rear sweep, the capsule tears open while the Chimera climbs back to its station on
the right, its gold shield, phase 3's playbook.

## death-capture-r93-a

Round 93, a capture of the game (step EF5, [how](#chimera-fight-r93-a), with sound): the death (t=252–264): the capsule at
0 (K ≈ 253.8): the bar gone, the shots in flight fading, the Chimera hanging and convulsing while the tissue closes
while Vorne's line 9 plays, the burst at 258.3 cutting it, track 34 cut at K, the bed alone.
