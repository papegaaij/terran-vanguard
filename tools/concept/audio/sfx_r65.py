#!/usr/bin/env python3
"""Concept round 65 (M9 part E "Level 32 Deep Core", step AUD-E): Level 32's new sounds, a and b each
(design/audio/sfx/README.md, *Act 5 sounds*; the Level 32 document's *Music & ambience*; the Sentinel Tower's and the
Strider's sound briefs): the Sentinel's charge (its sweep's telegraph), live beam loop, head-turn loop and death, the
Strider's step, torso servo, twin cannons, leg snap and topple fall, the blast door's grind and the mining charge's
blast. Not proposed (used as they are): the tunnel bed (round 60's pick S.19 a, belt-tunnel-r60-a, put into assets/ by
this step), the refinery's furnace roar (Level 19's chosen foundry bed, vent tell and burst), the SAM Nest's hatch,
lock and launch (round 62's picks, also the Strider's pod: the hatch and the shared homing launch), the Sentinel's and
the Strider's `medium` ground explosions.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 65 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, faded,
filtered, looped and levelled; nothing is synthesized. The tools are sfx_r64.py's (copied: preview, build, the --docs
rows).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  sentinel-charge-r65-*  0.8 s: a sweep's telegraph, a rising electrical hum (the full turn's 1.0 s telegraph plays it
                         from 0.2 s in)
  sentinel-beam-r65-*    seamless 3 s LOOPs: the live beam, an arc welder's or a discharge's sizzle
  sentinel-servo-r65-*   seamless 2 s LOOPs: the head turning with the beam, a low gear grind
  sentinel-death-r65-*   ≈ 1.2-1.3 s: over the `medium` rung, a heavy metal crash and the head's impact after it
  strider-step-r65-*     ≈ 0.75-0.8 s: a footfall, a metal stomp with a hydraulic hiss (a: the Revenant Walker's
                         chosen stride's recordings made smaller)
  strider-servo-r65-*    0.5 s: the torso turning, a servo whine
  strider-cannon-r65-*   ≈ 0.7 s: a fan of the twin cannons, a deep doubled gunshot
  strider-snap-r65-*     ≈ 0.4-0.55 s: a leg lost, a metal snap with sparks
  strider-fall-r65-*     ≈ 3.2 s: the topple, a groan for 1.4 s, the crash exactly at 1.4 s (the crush) and a rumble
  door-grind-r65-*       ≈ 2 s: a blast door opening, a heavy rolling grind and a deep clunk as it stops
  charge-blast-r65-*     1.4 s: a mining charge, a quarry blast with a rock crack

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; a loop by its band RMS): the beam as round 64's link hum, the servo a few dB under it, the charge as round
60's rail charge, the step and the cannons as round 62's Talon shot and SAM salvo, the snap and the fall as round 64's
tug loss, the blast at the `medium` rung's weight.

Usage: python3 tools/concept/audio/sfx_r65.py [subject-or-file ...]   e.g. sentinel-beam strider-fall-r65-b
       python3 tools/concept/audio/sfx_r65.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets and
the loop seams.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r65.py"
R = "r65"
ROUND = "round 65"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    33940: ('Rocks hit.wav', 'scarbelly25', CC0, P + "33/33940_298165-hq.ogg"),
    62923: ('welder 2.wav', 'SpeedY', CC0, P + "62/62923_6479-hq.ogg"),
    82682: ('Quarry blasting.wav', 'juskiddink', BY4, P + "82/82682_649468-hq.ogg"),
    85533: ('Breaking lock.aif', 'MAJ061785', BY3, P + "85/85533_1275441-hq.ogg"),
    144315: ('Mechanical Slide (aiff)', 'steaq', CC0, P + "144/144315_1474204-hq.ogg"),
    160797: ('120527 05 Dynamite 1.WAV', 'Evan Cruder', CC0, P + "160/160797_1241124-hq.ogg"),
    161342: ('Howitzer Artillery Gun Shot Sound Effect 02', 'qubodup', CC0, P + "161/161342_71257-hq.ogg"),
    161343: ('Howitzer Artillery Gun Shot Sound Effect 01', 'qubodup', CC0, P + "161/161343_71257-hq.ogg"),
    199803: ('Quake Short.wav', 'TheMinkman', CC0, P + "199/199803_3491501-hq.ogg"),
    222521: ('Cracking Earthquake (cracking soil, cracking stone)', 'uagadugu', CC0, P + "222/222521_255863-hq.ogg"),
    262510: ('Gym Lights Powering Up', 'OBXJohn', CC0, P + "262/262510_3719168-hq.ogg"),
    264014: ('Door_Metal_Groans_Ext.wav', 'Cell31_Sound_Productions', BY4, P + "264/264014_3571756-hq.ogg"),
    315753: ('Running Gear', 'Vurca', CC0, P + "315/315753_3200265-hq.ogg"),
    315943: ('open rolling door 150328_0543 MS.wav', 'klankbeeld', BY4, P + "315/315943_1648170-hq.ogg"),
    336665: ('ELECTRIC_MOTOR.aif', 'jacobsteel', BY4, P + "336/336665_101011-hq.ogg"),
    342235: ('big metallic robot footsteps', 'gladkiy', CC0, P + "342/342235_2364707-hq.ogg"),
    344592: ('Kicking_Dumpster.wav', 'kmyers1316', CC0, P + "344/344592_6125852-hq.ogg"),
    361339: ('Device powering up.wav', 'spoonsandlessspoons', BY4, P + "361/361339_3394347-hq.ogg"),
    442670: ('Power Spark', 'qubodup', BY4, P + "442/442670_71257-hq.ogg"),
    457294: ('Air (or steam) pressure release', 'brunoboselli', CC0, P + "457/457294_300738-hq.ogg"),
    462819: ('HUTCH HD SFX 2014 0143', 'leonelmail', CC0, P + "462/462819_4437257-hq.ogg"),
    490919: ('Stripped Gears in VAV Box', 'itickets', CC0, P + "490/490919_9638973-hq.ogg"),
    496836: ('Metallic Groan', 'hinchinbrook', CC0, P + "496/496836_9847211-hq.ogg"),
    562198: ('snd_metal_smash.wav', 'gristi', CC0, P + "562/562198_12618935-hq.ogg"),
    587443: ('Scrap metal dropping / crashing', 'SamsterBirdies', CC0, P + "587/587443_5487341-hq.ogg"),
    653294: ('Big Structure Collision Heard from Inside', 'Osvoldon', CC0, P + "653/653294_9808878-hq.ogg"),
    702908: ('ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3', 'newlocknew', BY4,
             P + "702/702908_5828667-hq.ogg"),
    813053: ('Big robot footstep 002', 'AudioPapkin', CC0, P + "813/813053_8698658-hq.ogg"),
    824121: ('ServoMotor.wav', 'derjuli', CC0, P + "824/824121_2573247-hq.ogg"),
    850040: ('metal-impact', 'JeffPearson', CC0, P + "850/850040_17113968-hq.ogg"),
    851264: ('KA_Warehouse_Creaking_Door_Roll_Open_ST', 'Kristoffer_Andersson', CC0, P + "851/851264_8407191-hq.ogg"),
    852991: ('Hydraulic Hiss, TTC bus door', 'OttisG', BY4, P + "852/852991_18014982-hq.ogg"),
    854183: ('Autocannon Shot 1', 'qubodup', CC0, P + "854/854183_71257-hq.ogg"),
    854184: ('Autocannon Shot 2', 'qubodup', CC0, P + "854/854184_71257-hq.ogg"),
    855099: ('CircuitBreaker_02', 'Wavewire', BY4, P + "855/855099_17559721-hq.ogg"),
}
SYNTH = {}  # nothing synthesized this round (sfx_r63.py's build() reads it)

_decoded = {}


def preview(sound):
    """The decoded HQ preview, fetched from the CDN into the cache when missing."""
    if sound not in _decoded:
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        _decoded[sound] = decode(path)
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() and loop() read their layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out

# subject -> (kind, level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
L32 = "../../campaign/act-5-the-belt/level-32-deep-core/README.md"
SENTINEL = "../../enemies/ground/sentinel-tower/README.md"
STRIDER = "../../enemies/ground/strider/README.md"
TOPPLE = "../../enemies/README.md#topple"
HAZARDS = "../../world/belt/README.md#hazards--set-pieces"

SOUNDS = {
    "sentinel-charge": ("shot", -27.0, -6.0, -25.0, "Sentinel Tower: the charge (the sweep's telegraph)",
                        "each Sentinel sweep's telegraph (Level 32's side galleries; at most 2 towers sweeping at "
                        "once): the 0.8 s arc telegraph (the crimson arc drawn), and from 0.2 s into the full turn's "
                        "1.0 s telegraph (the reach ring drawn first); the live beam's loop follows at its end",
                        "a rising electrical hum: a transformer or a capacitor bank charging, building to the beam; "
                        "distinct from the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising whine)",
                        f"{SENTINEL}#sounds", {
        "a": ([L(361339, 6.0, 0.8, speed=(1.5, 3.0), fin=0.08, fout=0.03, hp=70.0, lp=6000.0, swell=(-12.0, 1.2)),
               L(702908, 2.0, 0.8, gain=-5.0, fin=0.1, fout=0.03, hp=1500.0, lp=9000.0, swell=(-20.0, 1.6))],
              "a machine powering up, its 40 Hz hum glided up from 1.5 to 3 times its speed, with an electric "
              "discharge's crackle swelling in over its last half: a low 'mmmMMMM' rising into a fizz, 0.8 s"),
        "b": ([L(262510, 6.0, 0.8, speed=(1.0, 1.6), fin=0.08, fout=0.02, hp=100.0, swell=(-10.0, 1.4))],
              "a gym's metal-halide lights powering up: their ballasts' 120 Hz hum gliding up and swelling into the "
              "lamps' strike at its end: a buzzier, more mains-like 'bzzzZZT' than a, 0.8 s"),
    }),
    "sentinel-beam": ("loop", -34.0, -1.0, -29.0, "Sentinel Tower: the live beam (loop)",
                      "one loop while a Sentinel's beam sweeps (1.6 s an arc, 3.0 s a full turn; the nearest tower "
                      "when two sweep), panned to the tower; cut at once when the EMP stuns it",
                      "a harsh sizzle: an arc welder or an electric arc, raw and steady, under the fire; not the "
                      "Mantis's sweep (concept/enemy-mantis-sweep-r23-b.ogg)", f"{SENTINEL}#sounds", {
        "a": (([Loop(62923, 3.0, hp=80.0, lp=9000.0)], 3.0, 0.5),
              "a stick welder's arc burning steadily: a harsh frying sizzle over its 100 Hz buzz, 3.0 s loop"),
        "b": (([Loop(702908, 3.0, hp=300.0, lp=7000.0)], 3.0, 0.5),
              "a steady high-voltage discharge sparking: a brighter, crackling sizzle than a with less buzz under "
              "it, 3.0 s loop"),
    }),
    "sentinel-servo": ("loop", -39.0, -1.0, -31.0, "Sentinel Tower: the head's turn (loop)",
                       "under the beam while a Sentinel's head turns with it (its sweeps: the arc's 1.6 s and the full "
                       "turn's 3.0 s), quieter than the beam; the game may pitch it down for the full turn",
                       "a low servo grind: a heavy turret's gear motor turning, steady", f"{SENTINEL}#sounds", {
        "a": (([Loop(315753, 2.0, speed=0.7, hp=60.0, lp=5000.0)], 2.0, 0.6),
              "a running gear train slowed to 0.7: a low, even mechanical grind with a gear whine, 2.0 s loop"),
        "b": (([Loop(490919, 4.0, speed=0.8, hp=50.0, lp=5000.0)], 2.0, 0.6),
              "stripped gears grinding in a ventilation box, slowed to 0.8: a rougher, rattling grind than a, "
              "2.0 s loop"),
    }),
    "sentinel-death": ("shot", -19.0, -4.0, -17.0, "Sentinel Tower: its death (over the `medium` rung)",
                       "a Sentinel destroyed (four in Level 32; `TOWERS DOWN` counts them), over its `medium` ground "
                       "explosion: the head topples off and the scorched stump stays",
                       "a heavy metal crash with the head falling: a structure breaking, then the head's smaller "
                       "clattering impact", f"{SENTINEL}#sounds", {
        "a": ([L(653294, 0.0, 1.2, fin=0.002, fout=0.6, hp=40.0),
               L(587443, 2.45, 0.8, gain=-3.0, at=0.02, fin=0.002, fout=0.4, hp=120.0),
               L(562198, 0.0, 0.5, gain=-9.0, at=0.5, fin=0.002, fout=0.3, hp=150.0)],
              "a massive structure's collision heard from inside with scrap metal crashing over it, then a metal "
              "smash 0.5 s later (the head landing): 'KRRASH ... klang', 1.2 s"),
        "b": ([L(344592, 0.64, 0.8, speed=0.6, fin=0.002, fout=0.4, hp=40.0),
               L(850040, 0.0, 0.9, gain=-6.0, at=0.4, fin=0.002, fout=0.5, hp=100.0)],
              "a kicked steel dumpster slowed to 0.6, then a resonant metal impact ringing 0.4 s later (the head): a "
              "duller 'DOOMF' with a long bell-like 'dongg' after it, 1.3 s"),
    }),
    "strider-step": ("shot", -25.0, -5.0, -23.0, "Strider: a step",
                     "each footfall of a walking Strider (Level 32's tunnel floors, in pairs: at most two walking; "
                     "32 px/s, an 8-frame walk cycle, two footfalls a cycle), throttled so a pair does not drown "
                     "the music",
                     "a heavy metal stomp with a short hydraulic hiss (a hydraulic press or a car lift); smaller "
                     "than the Revenant Walker's chosen stride (concept/walker-stomp-r46-a.ogg)", f"{STRIDER}#sounds", {
        "a": ([L(813053, 0.15, 0.8, speed=1.25, fin=0.01, fout=0.35, hp=40.0),
               L(852991, 0.1, 0.56, gain=-8.0, at=0.2, speed=1.25, fin=0.04, fout=0.3, hp=300.0)],
              "the Revenant Walker's chosen stride made smaller: the same big robot footstep and bus door's "
              "hydraulic hiss, sped to 1.25 (higher and shorter): 'KLUMP-tsss', 0.8 s"),
        "b": ([L(342235, 9.6, 0.75, speed=1.1, fin=0.005, fout=0.3, hp=40.0),
               L(457294, 0.15, 0.4, gain=-10.0, at=0.18, fin=0.02, fout=0.25, hp=400.0, lp=9000.0)],
              "a big metal footstep (sped to 1.1) with an air pressure release's short hiss: a clangier, deeper "
              "thud than a with a sharper 'pssh', 0.75 s"),
    }),
    "strider-servo": ("shot", -31.0, -8.0, -29.0, "Strider: the torso's turn",
                      "the Strider's torso twisting toward the ship to fire (32 angles), as it starts a turn; "
                      "throttled to once per 0.5 s a Strider (it turns often)",
                      "a servo whine: an electric motor driving the torso round, short and mechanical",
                      f"{STRIDER}#sounds", {
        "a": ([L(336665, 2.2, 0.5, speed=(0.75, 1.0), fin=0.03, fout=0.15, hp=150.0)],
              "an electric motor's whine glided up from 0.75 to its own speed: a gritty, mid 'vwiiir', 0.5 s"),
        "b": ([L(824121, 1.0, 0.5, speed=0.5, fin=0.03, fout=0.15, hp=150.0)],
              "a servo motor slowed to 0.5: a cleaner, higher and thinner 'eeeee' than a, 0.5 s"),
    }),
    "strider-cannon": ("shot", -22.0, -5.0, -21.0, "Strider: the twin cannons",
                       "each fan of the Strider's twin cannons (3 shots 12° apart, every 2.4 s; easy 3.0 s, hard a "
                       "fan of 5 every 2.0 s), after the torso's 0.3 s hold; the shoulder pod's homing salvo plays "
                       "the shared homing launch",
                       "a deep double gunshot: two heavy cannons firing as one doubled boom", f"{STRIDER}#sounds", {
        "a": ([L(854183, 0.0, 0.6, speed=0.85, fin=0.001, fout=0.3, hp=50.0),
               L(854184, 0.0, 0.6, gain=-1.0, at=0.07, speed=0.85, fin=0.001, fout=0.3, hp=50.0)],
              "two autocannon shots slowed to 0.85, 70 ms apart: a hard, cracking 'BA-DAM', 0.67 s"),
        "b": ([L(161343, 0.05, 0.6, speed=1.3, fin=0.002, fout=0.3, hp=40.0),
               L(161342, 0.05, 0.6, gain=-1.0, at=0.08, speed=1.3, fin=0.002, fout=0.3, hp=40.0)],
              "two howitzer shots sped to 1.3, 80 ms apart: a deeper, boomier 'BOOM-BOOM' than a, less crack, "
              "0.68 s"),
    }),
    "strider-snap": ("shot", -21.0, -4.0, -20.0, "Strider: a leg lost",
                     "a Strider's leg destroyed (80 HP; it sparks at the knee and the stagger starts); the fall "
                     "below follows at once",
                     "a metal snap with sparks: a load-bearing strut breaking, dry and sharp; no pop",
                     f"{STRIDER}#sounds", {
        "a": ([L(562198, 0.0, 0.45, fin=0.001, fout=0.25, hp=120.0),
               L(442670, 0.25, 0.5, gain=-7.0, at=0.04, fin=0.01, fout=0.3, hp=800.0)],
              "a metal smash with an electric power spark crackling under its tail: a bright 'KSHANG-tssk', 0.54 s"),
        "b": ([L(85533, 0.98, 0.4, speed=0.8, fin=0.001, fout=0.2, hp=100.0),
               L(855099, 0.0, 0.25, gain=-5.0, at=0.02, fin=0.001, fout=0.15, hp=400.0)],
              "a lock breaking (slowed to 0.8) with a circuit breaker's arc snapping: a tighter, lower 'KRAK-zt' "
              "than a, 0.4 s"),
    }),
    "strider-fall": ("shot", -20.0, -3.0, -18.0, "Strider: the fall (the topple)",
                     "a Strider's topple, played at the leg's loss: the 0.6 s stagger and the 0.8 s fall, the crash "
                     "landing at 1.4 s with the crush (the units it crushes play their own rungs)",
                     "a long metal groan into a heavy crash and rumble: the hull tipping over and slamming down; its "
                     "crash exactly 1.4 s in", TOPPLE, {
        "a": ([L(496836, 4.4, 1.45, speed=0.75, fin=0.15, fout=0.1, hp=60.0, swell=(-8.0, 1.0)),
               L(653294, 0.0, 1.8, at=1.4, fin=0.002, fout=1.0, hp=30.0),
               L(587443, 2.45, 0.8, gain=-4.0, at=1.42, fin=0.002, fout=0.4, hp=120.0)],
              "a metallic groan slowed to 0.75 swelling for 1.4 s, then a massive structure's collision heard from "
              "inside with scrap metal crashing over it, rolling out low: 'grrrooaaan-KRRASH-rrrmm', 3.2 s"),
        "b": ([L(264014, 6.1, 1.45, speed=0.8, fin=0.15, fout=0.1, hp=50.0, swell=(-8.0, 1.0)),
               L(344592, 0.64, 0.9, at=1.4, speed=0.5, fin=0.002, fout=0.5, hp=35.0),
               L(587443, 4.45, 0.8, gain=-5.0, at=1.42, fin=0.002, fout=0.4, hp=120.0),
               L(199803, 3.6, 1.8, gain=-4.0, at=1.45, fin=0.05, fout=1.0, hp=25.0)],
              "a heavy door's metal groan slowed to 0.8 for 1.4 s, then a kicked steel dumpster slowed to 0.5 with "
              "a scrap metal crash and an earthquake's rumble under it: a creakier groan and a longer, lower "
              "rumble than a, 3.25 s"),
    }),
    "door-grind": ("shot", -24.0, -5.0, -22.0, "Blast door: opening",
                   "a blast door's lock destroyed (Level 32: the two richer channels' caches and the hall's secret): "
                   "the door pair slides open and the cache's crate floats out; three times a level at most",
                   "a heavy door grinding open on rails, a deep clunk as it stops",
                   f"{L32}#blast-doors-mining-charges-topples", {
        "a": ([L(315943, 1.6, 1.4, speed=0.85, fin=0.1, fout=0.15, hp=40.0),
               L(462819, 0.08, 0.7, gain=-2.0, at=1.35, fin=0.002, fout=0.35, hp=40.0)],
              "an industrial rolling door opening, slowed to 0.85, then a deep hollow metal impact as it stops: a "
              "low rumbling 'rrrrrrRRR-DOMM', 2.05 s"),
        "b": ([L(851264, 3.0, 1.4, speed=0.7, fin=0.1, fout=0.15, hp=60.0, lp=7000.0),
               L(144315, 0.0, 0.7, gain=-2.0, at=1.35, fin=0.002, fout=0.35, hp=40.0)],
              "a warehouse door creaking as it rolls open, slowed to 0.7, then a mechanical slide's thud: a "
              "screechier, more grinding 'skrrreeeek-THUNK' than a, 2.05 s"),
    }),
    "charge-blast": ("shot", -18.0, -3.0, -16.0, "Mining charge: the blast",
                     "a mining charge shot (1 hit): its 64 px burst (Level 32: 3 in each richer channel, 2 by the "
                     "hall's SAM pair, 4 on the refinery's gantry; later Level 34); a charge does not set off another",
                     "a sharp quarry blast, the `medium` rung's weight with a rock crack in it", HAZARDS, {
        "a": ([L(82682, 6.72, 1.4, fin=0.002, fout=0.7, hp=35.0),
               L(33940, 0.38, 0.6, gain=-5.0, at=0.0, fin=0.002, fout=0.3, hp=150.0)],
              "a quarry blast recorded at the quarry, with rocks striking rock on its front: a deep 'BOOM' with a "
              "stony 'krak' in its attack, 1.4 s"),
        "b": ([L(160797, 0.0, 1.4, fin=0.002, fout=0.7, hp=35.0),
               L(222521, 2.0, 0.8, gain=-5.0, at=0.02, fin=0.01, fout=0.4, hp=200.0)],
              "a dynamite charge going off, with stone cracking under it: a sharper, drier blast than a with a "
              "longer crumbling crack, 1.4 s"),
    }),
}


def layers_of(kind, spec):
    if kind == "synth":
        return []
    return spec[0] if kind == "loop" else spec


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    spec, what = SOUNDS[subject][8][v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 65 ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind == "loop":
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz), at most {num(cap)} LUFS"
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    one_shot = "length" in layer
    if not one_shot:
        span = (spec[1] + spec[2]) * speed
    else:
        span = layer["length"] * (np.mean(speed) if isinstance(speed, tuple) else speed)
    parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
    if isinstance(speed, tuple):
        parts.append(f"played at a speed gliding {num(speed[0])} → {num(speed[1])}")
    elif speed != 1.0:
        parts.append(f"slowed to {num(speed)}" if speed < 1 else f"sped to {num(speed)}")
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
    else:
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers_of(kind, spec)) > 1:
        parts.append("mixed with the other layers")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(kind, spec):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(kind, spec)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        kind, _, _, _, _, use, should, _, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in layers_of(kind, spec):
            s = layer["sound"]
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, kind, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        lv = band_rms_db(x) if kind == "loop" else short_term_band_db(x)
        info[(subject, v)] = (secs, what)
        size = f"{secs:.2f} s{' loop' if kind == 'loop' else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-65/: the same depth as design/audio/sfx)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        kind, _, _, _, title, use, should, part, variants = SOUNDS[subject]
        what = " ".join(f"**{v}** ({info[(subject, v)][0]:.2f} s): {info[(subject, v)][1]}." for v in variants
                        if (subject, v) in info)
        files = ", ".join(f"[{v}](../../audio/sfx/concept/{subject}-{R}-{v}.ogg)" for v in variants)
        rnd.append(f"| S.{k} | [{title} (`{subject}`)]({part}) | **pick a or b**. **Plays:** {use}. **Should sound "
                   f"like:** {should}. {what} | {files} | | **a** / b / redo | |")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    (outdir / "round.md").write_text("\n".join(rnd) + "\n")
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries, {len(rnd)} "
          f"round rows in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[8]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
