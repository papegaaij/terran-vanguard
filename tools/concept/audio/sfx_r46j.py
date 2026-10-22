#!/usr/bin/env python3
"""Concept round 46 (step AUD-J): the Ghost Drone's four sounds and the Act 3 enemy cues that were still synthesized
(the audit in briefs/synth-sounds.md, P1 and P1b), cut from real recordings in the Freesound database. The user, Act 3
playthrough: "Ghost drone sounds are really bad. They sound like pling plong on a xylophone." (and round 42: "You
should really stop trying to make these sounds yourself. Use the database."). An a/b pair each, proposed in round 46.
Every layer is a Freesound original (CC0 or CC-BY, in CREDITS.md; fetched with `freesound_fetch.py --download` into
~/.cache/terran-vanguard/freesound/); this script only cuts, slows (a fixed speed or a gliding one), layers, fades,
filters and levels them: nothing is synthesized. No bubbly or popping sources, no pitched pings; every blast starts
with a ramped attack. The same tools as sfx_r43g.py and sfx_r44.py.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo): `<subject>-r46-a.ogg` and `-b.ogg` for each
subject in SOUNDS below (the `what` text of each variant says what it is). The game keeps the old synthesized files
until the user picks.

Levels: each subject at the level of the file it replaces (the game's mix was tuned around those): one-shots on the
loudest 100 ms of the 200 Hz-5 kHz band under a -4 dBFS ceiling with at most 4 dB of limiting (a mix of sharper
transients is levelled lower instead), at most -14 LUFS (the accepted round-43 blasts; the tells -16 LUFS, CAPS);
loops by the whole loop's band RMS, at most about the replaced loop's loudness (LOOP_CAPS: loops heavy below 200 Hz).
Each layer is peak-normalised before its gain. Loop starts were picked for a clean seam (seam_db within 5 dB).

Sources the database had but the Freesound API's daily download limit (500 a day) kept out of this step: jet engine
start-ups (491407, 479490) for a cleaner drop whine, an M16A2 burst (151075) and a Tesla zap (853286).

Usage: python3 tools/concept/audio/sfx_r46j.py [subject-or-file ...]   e.g. ghost-drone-glint devil-spin-r46-b
       python3 tools/concept/audio/sfx_r46j.py --docs OUTDIR   writes the prompts.md section, the README Concept art
                                                              rows and the CREDITS rows (as Markdown) into OUTDIR
Deterministic (no randomness, bit-exact ffmpeg flags).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import L, Loop, loop, mix  # noqa: E402
from synth import SR, db, decode, limiter, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r46j.py"
ROUND = "r46"

# Freesound sound id -> (title, author, licence); the originals are cached as <id>_<name>.<type>.
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
SOURCES = {
    805169: ("Jet Flyby 02", "mrspivey", CC0),
    640505: ("Eurofighter Typhoon Flyby 005 – Close Proximity", "TimoSchmied", BY4),
    740252: ("Relay 2", "JoontheFloof", CC0),
    613884: ("Canon 550D Shutter", "iainmccurdy", BY4),
    858506: ("M&P15 Bolt Release [No Ring]", "smill.and.welson", BY4),
    145206: ("M16 gun 3 round burst", "lensflare8642", CC0),
    482120: ("Rifle Burst", "qubodup", CC0),
    843247: ("Explosion 1 (Burning Car rec by Ñado)", "qubodup", CC0),
    752072: ("Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024", "Artninja", BY4),
    849772: ("Electric Sparks", "mikiko850", BY4),
    165860: ("Parts falling", "tlwmdbt", BY4),
    67617: ("Metal Crash Collision", "qubodup", CC0),
    854351: ("Quadcopter Drone Collision Hitting Wall and Falling", "qubodup", CC0),
    135459: ("QUICK_SMASH_DEBRIS_001.wav", "JoelAudio", BY4),
    338721: ("Inside a Tornado.wav", "RogerBoyX69", BY4),
    438877: ("G56-22-Sandstorm.wav", "craigsmith", CC0),
    557188: ("JM_NATURAL ELEMENTS_Wind 01 - Tornado - Inside a metalic squeaking structure", "Julien_Matthey", CC0),
    215006: ("Flowing Sand", "SteveMeli", BY3),
    483599: ("Instant Wind", "Raclure", CC0),
    578490: ("Heavy stone door opens", "PostProdDog", CC0),
    434869: ("Powerfull Whooshes 2.wav", "Nic3_one", CC0),
    29986: ("stone_on_stone.aif", "thanvannispen", BY4),
    29989: ("stone_on_stone_dragging3.aif", "thanvannispen", BY4),
    340307: ("OsVaDuThDustThrowThud.wav", "The_Sean_", CC0),
    467301: ("Sand slidding out of shovel slowly.wav", "XfiXy8", CC0),
    234547: ("Big Swoosh", "Electroviolence", CC0),
    199332: ("sand pour on sand FF662.aif", "martinimeniscus", CC0),
    199397: ("sand slide on metal FF678.aif", "martinimeniscus", CC0),
    192365: ("Dinosaur Footsteps-01.wav", "jamesrodavidson", BY3),
    753178: ("Monster Stomp Footsteps Sequence", "Vilkas_Sound", BY4),
    350750: ("stonehit1.wav", "aerror", CC0),
    426162: ("M224 Mortar Firing Close 02", "Nanashi", BY4),
    854478: ("120mm Mortar Shell Shot 2", "qubodup", CC0),
    581083: ("Pressure Hiss", "magnuswaker", CC0),
    760672: ("Guterton - Heavy Stone Grind", "guterton.wav", BY4),
    610739: ("Rolling boulder", "gootie3333333", BY4),
    130972: ("Large, Low Rumble.wav", "duckduckpony", BY4),
    179341: ("Gravel Impacts and Falls.wav", "lolamadeus", CC0),
    828623: ("bug legs tapping", "c64cosmin", CC0),
    761559: ("crab walking", "stuniverso", CC0),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0),
    347046: ("765 kilo-volt transmission lines buzzing and crackling", "johnaudiotech", CC0),
    338074: ("Electric_Pylon_Power Line-Buzz 01.wav", "omnisounddesign", BY4),
    385992: ("Welding_2_long_take.wav", "ivolipa", CC0),
    386386: ("Electric welding with tig - Full cycle", "ldezem", CC0),
    93100: ("Whip Crack 01.wav", "CGEffex", BY4),
    151288: ("Wind-down 1.aif", "timgormly", CC0),
    471828: ("24_SeRompeCable.wav", "Lextao", BY4),
    529925: ("Whip Crack.m4a", "SciFiSounds", CC0),
    179245: ("FAN 3 (Slow Stop).wav", "temawas", BY4),
    444491: ("Short Choir", "Breviceps", CC0),
    458144: ("Choir.aif", "DiArchangeli", CC0),
    732443: ("Kargyraa Throat Chant/Singing (Lower Register Vocal Folds)", "Hjaarlmarch", CC0),
    862321: ("26-07-12 - Ethereal Choir Drone - ambient pad", "Valerie-Vivegnis", BY4),
    702771: ("Force Field 01.wav", "LilMati", CC0),
    813053: ("Big robot footstep 002", "AudioPapkin", CC0),
    852991: ("Hydraulic Hiss, TTC bus door", "OttisG", BY4),
    213041: ("mech_step_002.ogg", "pgi", CC0),
    182431: ("Explosive 1 v2 [DOD 130303].flac", "qubodup", CC0),
    487143: ("R29-28-Large Metal Vehicle Crashes.wav", "craigsmith", CC0),
    592572: ("Flame Burst", "magnuswaker", CC0),
    674899: ("S17-28 Large explosion with debris.wav", "craigsmith", CC0),
    421879: ("Fire - Effects - Swooshes - Bursts, evolving", "GregorQuendel", BY4),
}

# subject -> (kind, level, ceiling dBFS, use, brief, {variant: (spec, what)}); a loop's spec is (layers, length,
# xfade). The level is the band RMS (dB) for a "loop", the loudest 100 ms of the band for a "shot".
SOUNDS = {
    "ghost-drone-drop": ("shot", -24.0, -4.0, "a Ghost Drone dropping into its lane (Levels 18, 19)",
                         "a turbine/jet whine descending, built not grown, 0.9 s", {
        "a": ([L(805169, 1.8, 0.9, speed=(1.3, 0.8), fin=0.12, fout=0.3, hp=150.0)],
              "a jet's turbine whine passing, its speed gliding down from 1.3 to 0.8"),
        "b": ([L(640505, 3.4, 0.9, speed=(1.25, 0.8), fin=0.12, fout=0.3, hp=150.0)],
              "a Eurofighter's close flyby, its speed gliding down from 1.25 to 0.8"),
    }),
    "ghost-drone-glint": ("shot", -24.0, -4.0, "a Ghost Drone's 0.3 s eye glint before its burst (the tell)",
                          "a short mechanical/electronic lock click, not a pitched ping", {
        "a": ([L(740252, 0.02, 0.23, fin=0.003, fout=0.04, hp=150.0)],
              "an electric relay latching (two dry clicks)"),
        "b": ([L(613884, 0.0, 0.3, fin=0.003, fout=0.08, hp=150.0),
               L(858506, 0.06, 0.25, gain=-8.0, fin=0.003, fout=0.1, hp=100.0)],
              "a camera shutter's two clicks with a rifle's bolt release under them"),
    }),
    "ghost-drone-burst": ("shot", -21.0, -4.0, "a Ghost Drone's burst (shots 0.15 s apart), once at its first shot",
                          "three dry precise human-gun shots", {
        "a": ([L(145206, 0.19, 0.6, fin=0.004, fout=0.3, hp=40.0)],
              "an M16's three-round burst (shots 0.14 s apart)"),
        "b": ([L(482120, 0.0, 0.29, fin=0.003, fout=0.06, hp=40.0)],
              "an automatic rifle's three-shot burst (shots 0.1 s apart)"),
    }),
    "ghost-drone-death": ("shot", -19.0, -4.0, "a Ghost Drone's death, the `small` rung",
                          "a small machine breaking up: crack, metal, sparks, no fireball", {
        "a": ([L(843247, 0.02, 0.7, gain=-3.0, fin=0.004, fout=0.4, hp=40.0),
               L(752072, 0.06, 0.8, at=0.01, fin=0.004, fout=0.4),
               L(849772, 1.22, 0.4, gain=-6.0, at=0.12, fin=0.005, fout=0.2, hp=500.0),
               L(165860, 0.0, 0.6, gain=-8.0, at=0.3, fin=0.02, fout=0.3)],
              "a dry bang and a heavy metal crunch, a burst of sparks and parts falling"),
        "b": ([L(67617, 0.0, 0.6, gain=-2.0, fin=0.004, fout=0.35, hp=40.0),
               L(854351, 4.18, 0.8, gain=-3.0, at=0.02, fin=0.005, fout=0.4, hp=60.0),
               L(135459, 0.03, 0.8, gain=-5.0, at=0.05, fin=0.004, fout=0.4),
               L(849772, 1.93, 0.35, gain=-8.0, at=0.15, fin=0.005, fout=0.2, hp=500.0)],
              "a metal crash, a real quadcopter hitting a wall and falling, smashed debris and sparks"),
    }),
    "devil-spin": ("loop", -32.0, -1.0, "the Dust Devil's spin, one loop for the nearest (Levels 18, 20, 21)",
                   "a whirling wind roar with grit", {
        "a": (([Loop(338721, 10.0, hp=30.0), Loop(438877, 20.0, gain=-6.0, hp=150.0)], 3.0, 0.4),
              "inside a tornado (a steady whirling roar) with a sandstorm's grit"),
        "b": (([Loop(557188, 24.0, hp=30.0), Loop(215006, 2.0, gain=-10.0, hp=200.0, lp=6000.0)], 3.0, 0.4),
              "a tornado through a squeaking metal structure, sand flowing"),
    }),
    "devil-open": ("shot", -20.0, -4.0, "a Dust Devil's 0.4 s rise and opening (the tell)",
                   "the vanes parting with a gust and a low hollow tone", {
        "a": ([L(483599, 0.8, 0.7, fin=0.05, fout=0.25, hp=40.0, swell=(-10.0, 1.0)),
               L(578490, 2.0, 0.62, gain=-4.0, at=0.08, fin=0.05, fout=0.25, hp=90.0)],
              "a gust swelling in over a heavy stone door grinding open (its hollow resonance)"),
        "b": ([L(434869, 0.55, 0.7, fin=0.05, fout=0.25, hp=40.0),
               L(29986, 2.3, 0.6, gain=-4.0, at=0.05, fin=0.04, fout=0.25, hp=60.0)],
              "a powerful whoosh with stone dragging on stone"),
    }),
    "devil-spiral": ("shot", -22.0, -4.0, "a Dust Devil's grit spiral volleys",
                     "a short turning grit hiss, 0.45 s", {
        "a": ([L(340307, 0.38, 0.45, fin=0.03, fout=0.15, hp=200.0),
               L(467301, 1.0, 0.45, gain=-4.0, fin=0.05, fout=0.15, hp=300.0)],
              "a throw of dust with sand sliding off a shovel"),
        "b": ([L(234547, 0.3, 0.45, fin=0.04, fout=0.15, hp=150.0),
               L(199332, 4.5, 0.45, gain=-3.0, fin=0.05, fout=0.15, hp=300.0, lp=7000.0)],
              "a big swoosh with sand pouring"),
    }),
    "shellback-walk": ("loop", -34.0, -1.0, "the Shellback's walk, one loop for the nearest (Levels 17, 19, 21)",
                       "heavy padded footfalls with shell plates clacking", {
        "a": (([Loop(192365, 14.15, hp=40.0), Loop(350750, 0.0, gain=-8.0, hp=150.0)], 3.26, 0.2),
              "a big creature's footsteps (two to a loop) with stones clacking"),
        "b": (([Loop(753178, 1.95, hp=25.0), Loop(29986, 0.25, gain=-10.0, hp=150.0)], 4.16, 0.15),
              "a monster's heavy stomps (two to a loop) with stone knocking on stone"),
    }),
    "shellback-lob": ("shot", -17.0, -4.0, "a Shellback's mortar lob", "a dull cough thump and a pressure hiss", {
        "a": ([L(426162, 0.0, 0.62, speed=0.85, fin=0.02, fout=0.3, hp=30.0),
               L(581083, 0.05, 0.45, gain=-8.0, at=0.08, fin=0.03, fout=0.25, hp=300.0)],
              "a real M224 mortar firing, slowed to 0.85, with a pressure hiss"),
        "b": ([L(854478, 0.0, 0.62, speed=0.8, fin=0.02, fout=0.3, hp=30.0),
               L(581083, 0.4, 0.4, gain=-10.0, at=0.1, fin=0.03, fout=0.2, hp=300.0)],
              "a 120 mm mortar shot, slowed to 0.8, with a pressure hiss"),
    }),
    "shellback-curl": ("shot", -20.0, -4.0, "a Shellback curling up", "plates grinding shut, a heavy clack", {
        "a": ([L(29989, 1.6, 0.8, fin=0.05, fout=0.15, hp=60.0, swell=(-8.0, 1.0)),
               L(350750, 0.89, 0.3, gain=-2.0, at=0.5, fin=0.005, fout=0.2, hp=60.0)],
              "stone dragging on stone, closing on a heavy stone clack"),
        "b": ([L(760672, 0.4, 0.8, fin=0.05, fout=0.15, hp=40.0, swell=(-8.0, 1.0)),
               L(29986, 0.36, 0.35, gain=-1.0, at=0.5, fin=0.005, fout=0.25, hp=60.0)],
              "a heavy stone grind, closing on a stone-on-stone knock"),
    }),
    "shellback-roll": ("loop", -30.0, -1.0, "a Shellback's roll, one loop for the nearest rolling one",
                       "a rumbling grind that crushes", {
        "a": (([Loop(610739, 0.2, hp=25.0), Loop(179341, 3.4, gain=-8.0, hp=150.0)], 2.5, 0.3),
              "a boulder rolling with gravel falling"),
        "b": (([Loop(130972, 4.0, hp=25.0), Loop(29989, 1.6, gain=-6.0, hp=60.0)], 2.5, 0.3),
              "a large low rumble with stone dragging on stone"),
    }),
    "shellback-uncurl": ("shot", -22.0, -4.0, "a Shellback uncurling",
                         "the plates opening, a looser clack, a short exhale of grit", {
        "a": ([L(29989, 3.6, 0.6, fin=0.03, fout=0.2, hp=60.0),
               L(350750, 0.41, 0.3, gain=-5.0, at=0.3, fin=0.005, fout=0.2, hp=80.0),
               L(199332, 2.0, 0.4, gain=-10.0, at=0.3, fin=0.05, fout=0.2, hp=300.0, lp=6000.0)],
              "stone dragging open, a loose stone clack and sand pouring off"),
        "b": ([L(578490, 5.0, 0.6, fin=0.03, fout=0.2, hp=90.0),
               L(350750, 0.6, 0.3, gain=-5.0, at=0.3, fin=0.005, fout=0.2, hp=80.0),
               L(467301, 2.0, 0.4, gain=-10.0, at=0.3, fin=0.05, fout=0.2, hp=300.0)],
              "a heavy stone door grinding open, a stone clack and sand sliding off"),
    }),
    "crawler-walk": ("loop", -36.0, -1.0, "the Threadcrawler's walk, one loop for the nearest (Level 16)",
                     "a dry many-legged skitter on rock", {
        "a": (([Loop(828623, 9.03, hp=200.0), Loop(179341, 3.4, gain=-12.0, hp=150.0)], 3.0, 0.3),
              "fingers tapping like bug legs (a dense skitter) over gravel"),
        "b": (([Loop(761559, 0.0, speed=0.85, hp=200.0, lp=6000.0), Loop(828623, 11.99, gain=-6.0, hp=200.0)],
               3.0, 0.3),
              "a crab walking, slowed to 0.85, over the tapping legs"),
    }),
    "tendril-hum": ("loop", -38.0, -1.0, "a standing tendril barrier's hum, one loop for the nearest (L16, L06)",
                    "a taut electric-organic drone, no wobble", {
        "a": (([Loop(453444, 5.0, hp=40.0), Loop(347046, 4.71, gain=-8.0, hp=200.0)], 6.0, 0.5),
              "an electric transformer's hum in a large room with high-voltage lines buzzing"),
        "b": (([Loop(338074, 5.0, hp=40.0), Loop(347046, 10.0, gain=-4.0, hp=200.0)], 6.0, 0.5),
              "a power pylon's buzz with high-voltage lines crackling"),
    }),
    "tendril-burn": ("loop", -34.0, -1.0, "a beam burning a tendril, a loop while it burns",
                     "a searing crackle", {
        "a": (([Loop(385992, 6.74, hp=150.0)], 2.0, 0.25), "an electric welder's crackle"),
        "b": (([Loop(386386, 9.2, hp=150.0), Loop(849772, 4.0, gain=-6.0, hp=500.0)], 2.0, 0.25),
              "a TIG welder's hiss with electric sparks"),
    }),
    "tendril-break": ("shot", -17.0, -4.0, "a tendril breaking", "a whip-crack snap and a falling whine", {
        "a": ([L(93100, 0.18, 0.9, fin=0.005, fout=0.4, hp=60.0),
               L(151288, 0.6, 0.85, gain=-6.0, at=0.05, speed=(1.4, 0.6), fin=0.03, fout=0.4, hp=200.0)],
              "a whip crack and a motor winding down, its speed gliding from 1.4 to 0.6"),
        "b": ([L(471828, 0.4, 0.9, fin=0.005, fout=0.5, hp=60.0),
               L(529925, 0.15, 0.35, gain=-4.0, fin=0.005, fout=0.2),
               L(179245, 2.0, 0.85, gain=-6.0, at=0.05, speed=(2.0, 0.8), fin=0.03, fout=0.4, hp=150.0)],
              "a cable snapping with a whip crack, a fan slowing to a stop, its speed gliding from 2 to 0.8"),
    }),
    "burrow-dive": ("shot", -26.0, -4.0, "a Burrower's 0.4 s dive (Levels 15-17, 20, 21)",
                    "a falling sand hiss, short", {
        "a": ([L(467301, 0.4, 0.55, speed=(1.0, 0.75), fin=0.03, fout=0.25, hp=150.0, lp=7000.0)],
              "sand sliding off a shovel, its speed gliding down from 1 to 0.75"),
        "b": ([L(199397, 2.0, 0.55, speed=(1.0, 0.75), fin=0.03, fout=0.25, hp=150.0),
               L(199332, 3.0, 0.5, gain=-6.0, fin=0.03, fout=0.25, hp=200.0, lp=7000.0)],
              "sand sliding on metal, gliding down, with sand pouring"),
    }),
    "herald-song": ("shot", -20.0, -4.0, "the Choir Herald's song with its ring of bullets (L17, L19, L21)",
                    "a short sung phrase in the Choir's colour (not words)", {
        "a": ([L(444491, 0.02, 1.9, speed=0.85, fin=0.08, fout=0.5, hp=80.0)],
              "a short choir's 'aah', slowed to 0.85 (darker)"),
        "b": ([L(458144, 4.4, 1.9, fin=0.1, fout=0.5, hp=60.0),
               L(732443, 3.0, 1.9, gain=-8.0, fin=0.15, fout=0.5, hp=60.0)],
              "a choir's chord changing, a low throat chant under it"),
    }),
    "herald-aura": ("loop", -40.0, -1.0, "the Herald's aura, one loop while any ring is up",
                    "a soft high shimmer", {
        "a": (([Loop(862321, 10.0, hp=1500.0)], 4.0, 0.5), "an ethereal choir drone, high-passed at 1.5 kHz"),
        # b: the chord holds steady from 6.0 to about 9.25 s and then dies away; the loop stays inside it (the first
        # cut, 6.21 s for 4 s, ran into the decay and dipped to about -59 dB at the seam every 4 s, SNDL).
        "b": (([Loop(458144, 6.15, hp=1500.0)], 2.6, 0.5), "a choir's held chord, high-passed at 1.5 kHz"),
    }),
    "aura-absorb": ("shot", -22.0, -4.0, "a ring absorbing a hit, spaced out",
                    "the enemy-shield hit, no glassy ping", {
        "a": ([L(849772, 0.6, 0.35, fin=0.005, fout=0.2, hp=400.0),
               L(752072, 0.06, 0.3, gain=-6.0, fin=0.004, fout=0.2, lp=1500.0)],
              "a burst of electric sparks over a muffled metal punch"),
        "b": ([L(702771, 0.0, 0.4, fin=0.005, fout=0.25, hp=100.0),
               L(849772, 2.48, 0.3, gain=-6.0, fin=0.005, fout=0.2, hp=500.0),
               L(752072, 1.8, 0.3, gain=-8.0, fin=0.004, fout=0.2, lp=1500.0)],
              "a force field's hit with a crackle of sparks over a muffled punch"),
    }),
    "walker-stomp": ("shot", -19.0, -4.0, "a Revenant Walker's footfall (Level 19)",
                     "a dull heavy stomp, a leg clank and a hydraulic hiss", {
        "a": ([L(813053, 0.15, 1.0, fin=0.01, fout=0.4, hp=25.0),
               L(852991, 0.1, 0.7, gain=-8.0, at=0.25, fin=0.05, fout=0.35, hp=300.0)],
              "a big robot's footstep with a bus door's hydraulic hiss"),
        "b": ([L(213041, 0.0, 1.0, fin=0.005, fout=0.4, hp=25.0),
               L(581083, 0.0, 0.6, gain=-9.0, at=0.3, fin=0.04, fout=0.3, hp=300.0)],
              "a mech's step and its servo, a pressure hiss"),
    }),
    "tank-death": ("shot", -17.0, -4.0, "a Warden Tank's death, the `medium` rung (Levels 08, 19)",
                   "a hull explosion with metal debris", {
        "a": ([L(182431, 0.0, 1.8, fin=0.02, fout=0.8, hp=25.0),
               L(487143, 7.0, 1.5, gain=-5.0, at=0.1, fin=0.02, fout=0.7, hp=60.0),
               L(592572, 0.0, 1.4, gain=-7.0, at=0.15, fin=0.05, fout=0.7)],
              "a real military explosive, a large metal vehicle crashing and a flame burst"),
        "b": ([L(674899, 0.0, 1.8, fin=0.02, fout=0.8),
               L(67617, 0.0, 0.6, gain=-4.0, at=0.05, fin=0.005, fout=0.3),
               L(421879, 1.25, 1.5, gain=-7.0, at=0.2, fin=0.04, fout=0.7),
               L(487143, 14.5, 1.3, gain=-7.0, at=0.3, fin=0.02, fout=0.6, hp=60.0)],
              "a large explosion with debris, a metal crash, fire roaring out, metal wreckage"),
    }),
}

# The loudest a one-shot may be (LUFS); the tells stop lower.
CAPS = {"ghost-drone-glint": -16.0, "ghost-drone-drop": -16.0, "devil-open": -16.0, "shellback-lob": -16.0,
        "shellback-curl": -16.0, "shellback-uncurl": -17.0, "walker-stomp": -15.0}
CAP = -14.0
# The most limiting a one-shot gets (dB); a mix with sharper transients is levelled lower instead.
MAX_LIMIT = 4.0
# The loudest a loop may be (LUFS): about the loudness of the synthesized loop it replaces, plus 1 LU.
LOOP_CAPS = {"devil-spin": -23.0, "shellback-walk": -22.0, "shellback-roll": -19.0, "crawler-walk": -26.0,
             "tendril-hum": -27.0, "tendril-burn": -22.0, "herald-aura": -30.0}


def layers_of(spec):
    return spec[0] if isinstance(spec, tuple) else spec


def sources_of(spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(spec)))


def render(subject, v):
    kind, band, ceiling, _, _, variants = SOUNDS[subject]
    spec, _ = variants[v]
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        st = st * undb(band - band_rms_db(st))
        cap = LOOP_CAPS[subject]
        if lufs(st)[0] > cap:  # a loop heavy below 200 Hz: at most about the loudness of the file it replaces
            st = st * undb(cap - lufs(st)[0])
            return st, f"capped at {cap:.0f} LUFS"
        return st, ""
    raw = mix(spec)
    over = db(np.max(np.abs(raw)) * undb(band - short_term_band_db(raw))) - ceiling
    if over > MAX_LIMIT:  # sharp transients (clicks, shots): keep them, at a lower band level
        band -= over - MAX_LIMIT
    st = level(raw, band, ceiling)
    cap = CAPS.get(subject, CAP)
    if lufs(st)[0] > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
        raw = raw - raw.mean(axis=1, keepdims=True)
        for _ in range(3):
            raw = limiter(raw * undb(cap - lufs(raw)[0]), ceiling)
        st = raw
    return st, f"limiting {max(0.0, min(over, MAX_LIMIT)):.1f} dB, band {band:.1f} dB, cap {cap:.0f} LUFS"


def build(subject, v):
    kind, band, ceiling, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{ROUND}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    st, note = render(subject, v)
    if note:
        print(f"  {name}: {note}")
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")


# ---- the documentation (prompts.md section, README rows, CREDITS rows), from the specs and the written files ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}
SHORT = {CC0: "CC0", BY3: "CC-BY 3.0", BY4: "CC-BY 4.0"}


def url(s):
    return f"https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/"


def fmt(x):
    return f"{x:g}"


def layer_changes(layer, kind, subject, length=None, xfade=None):
    _, band, ceiling, _, _, _ = SOUNDS[subject]
    speed = layer["speed"]
    if kind == "loop":
        span = (length + xfade) * speed
        parts = [f"cut {fmt(layer['start'])}–{layer['start'] + span:.2f} s"]
    else:
        top = max(speed) if isinstance(speed, tuple) else speed
        parts = [f"cut {fmt(layer['start'])}–{layer['start'] + layer['length'] * top:.2f} s"]
    if isinstance(speed, tuple):
        parts.append(f"its speed gliding from {fmt(speed[0])} to {fmt(speed[1])}")
    elif speed != 1.0:
        parts.append(f"slowed to {fmt(speed)}" if speed < 1 else f"sped up to {fmt(speed)}")
    if layer.get("hp"):
        parts.append(f"{fmt(layer['hp'])} Hz high-pass")
    if layer.get("lp"):
        parts.append(f"{fmt(layer['lp'])} Hz low-pass")
    if kind != "loop":
        if layer.get("swell"):
            parts.append(f"swelling from {fmt(layer['swell'][0])} dB")
        parts.append(f"{fmt(layer['fin'])} s fade-in, {fmt(layer['fout'])} s fade-out")
    parts.append("peak-normalised" + (f" then {fmt(layer['gain'])} dB" if layer["gain"] else ""))
    if kind != "loop" and layer["at"]:
        parts.append(f"placed at {fmt(layer['at'])} s")
    if kind == "loop":
        parts.append(f"cross-faded ({fmt(xfade)} s, equal power) into a seamless {fmt(length)} s loop")
    parts.append("mixed with the other layers")
    if kind == "loop":
        parts.append(f"levelled to {fmt(band)} dB band RMS (200 Hz–5 kHz), at most {fmt(LOOP_CAPS[subject])} LUFS")
    else:
        cap = CAPS.get(subject, CAP)
        parts.append(f"levelled to {fmt(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a "
                     f"{fmt(ceiling)} dBFS ceiling with at most {fmt(MAX_LIMIT)} dB of limiting (a sharper mix lower), "
                     f"at most {fmt(cap)} LUFS")
    return "Freesound original (Freesound API), " + ", ".join(parts) + ", OGG Vorbis q6"


def docs(out_dir):
    out_dir = Path(out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    prompts = ["## Concept round 46, step AUD-J (the Ghost Drone and the Act 3 enemy cues, from the database)", "",
               "The user, Act 3 playthrough: \"Ghost drone sounds are really bad. They sound like pling plong on a "
               "xylophone.\" Every file is cut from **Freesound originals** (CC0 or CC-BY, each in "
               "[CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r46j.py` (cut, slowed or gliding, "
               "layered, filtered, faded and levelled; nothing synthesized), big and heavy sources, nothing bubbly or "
               "popping, no pitched pings, every blast with a ramped attack. An a/b pair each, proposed (round 46), "
               "pending the user's review; the game keeps the old synthesized files until the pick. Levels: each at "
               "the level of the file it replaces: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band under a "
               "−4 dBFS ceiling with at most 4 dB of limiting (a mix of sharper transients, the clicks and shots, "
               "lower), at most −14 LUFS (the tells −16, the Shellback's plates −16/−17, the stomp −15); loops by "
               "their band RMS, at most about the replaced loop's loudness (+1 LU) where a loop is heavy below 200 Hz.",
               ""]
    rows, credits = [], []
    for subject, (kind, band, ceiling, use, brief, variants) in SOUNDS.items():
        for v, (spec, what) in variants.items():
            name = f"{subject}-{ROUND}-{v}"
            path = OUT / f"{name}.ogg"
            x = decode(path)
            srcs = sources_of(spec)
            dur = x.shape[1] / SR
            lics = list(dict.fromkeys(SHORT[SOURCES[s][2]] for s in srcs))
            titled = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
            lvl = f"{fmt(band)} dB band RMS" if kind == "loop" else f"{fmt(band)} dB"
            measured = band_rms_db(x) if kind == "loop" else short_term_band_db(x)
            loopnote = f", seamless {fmt(spec[1])} s loop" if kind == "loop" else ""
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound ({', '.join(lics)}, "
                        f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: {what}; {titled}, {dur:.2f} s"
                        f"{loopnote} ({measured:.0f} dB) | proposed (round 46) |".replace("(-", "(−"))
            heads = " + ".join(f"\"{SOURCES[s][0]}\" by {SOURCES[s][1]}" for s in srcs)
            links = ", ".join(f"<{url(s)}> ({SOURCES[s][0]} by {SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
            if kind == "loop":
                edit = (f"Edit: a seamless {fmt(spec[1])} s loop, band RMS {band_rms_db(x):.1f} dB, peak "
                        f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS.")
            else:
                edit = (f"Edit: {dur:.2f} s, the loudest 100 ms band {short_term_band_db(x):.1f} dB, peak "
                        f"{db(np.abs(x).max()):.1f} dBFS" + (f", {lufs(x)[0]:.1f} LUFS." if np.isfinite(lufs(x)[0]) else "."))
            prompts += [f"### {name} — {heads}", "", "Proposed (round 46).",
                        f"Source{'s' if len(srcs) > 1 else ''}: {links}. Use: {use}.",
                        f"Brief: {brief}. Why: {what}.", edit.replace("-", "−").replace("−>", "->"), ""]
            layers = layers_of(spec)
            for s in srcs:
                ch = "; ".join(layer_changes(layer, kind, subject, *(spec[1:] if kind == "loop" else ()))
                               for layer in layers if layer["sound"] == s)
                credits.append(f"| design/audio/sfx/concept/{name}.ogg | {SOURCES[s][0]} | {SOURCES[s][1]} | "
                               f"[freesound]({url(s)}) | [{SOURCES[s][2]}]({LICENCE_URL[SOURCES[s][2]]}) | {ch} |")
    (out_dir / "prompts-r46j.md").write_text("\n".join(prompts))
    (out_dir / "readme-rows-r46j.md").write_text("\n".join(rows) + "\n")
    (out_dir / "credits-r46j.md").write_text("\n".join(credits) + "\n")
    print(f"wrote {len(rows)} README rows, {len(credits)} CREDITS rows and the prompts section into {out_dir}")


def main(argv):
    if argv[:1] == ["--docs"]:
        docs(argv[1])
        return
    for subject, (_, _, _, _, _, variants) in SOUNDS.items():
        for v in variants:
            if argv and subject not in argv and f"{subject}-{ROUND}-{v}" not in argv:
                continue
            build(subject, v)


if __name__ == "__main__":
    main(sys.argv[1:])
