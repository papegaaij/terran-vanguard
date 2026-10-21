#!/usr/bin/env python3
"""Concept round 46 (step AUD-K): the remaining synthesized ambience beds, rare sounds and player-gear sounds, cut
from real recordings in the Freesound database (the user, round 42: "You should really stop trying to make these
sounds yourself. Use the database."), an a/b pair each. Every layer is a Freesound original (CC0 or CC-BY, in
CREDITS.md; fetched with the Freesound API into ~/.cache/terran-vanguard/freesound/, as `freesound_fetch.py
--download` does); this script only cuts, slows or speeds (a fixed speed or a gliding one), layers, fades, filters
and levels them: nothing is synthesized. No bubbly or popping sources (no water slurps, no bell chimes); every layer
starts with a ramped attack. The same tools as sfx_r43g.py and sfx_r44.py. Pending the user's review in round 46;
the game keeps the old files until the pick.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), an a and a b each:

  canopy-hum-r46-*           Level 14's canopy bed while the canopy lives, a seamless 16 s LOOP
  mars-canyon-r46-*          Level 16's canyon wind with its howl, a seamless 16 s LOOP
  mars-colony-r46-*          Level 17's plains wind, the domes' air plant and a distant rail, a seamless 16 s LOOP
  mars-foundry-r46-*         Level 19's foundry: furnace roar, conveyors, a slow drop forge, the wind; a seamless LOOP
  enemy-leviathan-cry-r46-*  the Leviathan's death cry (Level 03): a real whale call slowed
  driftjelly-pulse-r46-*     the Driftjelly's ring pulse (Level 11): a muffled underwater pressure pulse, no bubbles
  lamprey-drain-r46-*        the Lamprey's shield drain (Level 12), a seamless 2.4 s LOOP: a real electric arc's buzz
  sonde-chime-r46-*          a weather sonde collected (Level 12): a heavy clamp locking on and a radio squelch, no bell
  integrity-alarm-r46-*      the relay's or a dome's integrity alarm (Levels 13, 17): a real ship's alarm
  drone-launch-r46-*         a drone launching (Levels 13, 17): a real quadcopter taking off, slowed (bigger rotors)
  dome-hit-r46-*             a Level 17 dome struck: a thick glass knock, a creak and ice-like stress cracks, no shatter
  dash-r46-*                 the ship's dash: a compressed-air blast with a jet thruster's roar
  overcharge-whoosh-r46-*    Shield Overcharge on: a real charge whine gliding up with a whoosh or sparks
  overcharge-end-r46-*       Shield Overcharge off: the charge whine gliding down and fading
  emp-ring-r46-*             the EMP Burst: a high-voltage arc's crack and buzz over a deep thump
  emp-powerdown-r46-*        a machine stunned by the EMP: a real machine winding down, or a flickering mains hum sagging
  ion-beam-stop-r46-*        the Ion Beam's collapse: a real arc or coil hum gliding down to nothing
  beam-impact-r46-*          a beam's scorch where it hits, a seamless 1.5 s LOOP: real arc or MIG welding
  drone-rebuild-r46-*        the Combat Drone rebuilt: a servo's whir into a mechanical lock
  launch-rail-r46-*          the catapult run at every level start: a launch's rush to a heavy buffer clunk at 3.6 s

Levels like the sounds they replace (their loudest 100 ms of the 200 Hz-5 kHz band under a -4 dBFS ceiling, at most a
little over their loudness, so heavier sources do not stand out of the mix), the beds like the accepted beds (`mars-dust`
-36 dB band RMS, at most `basin-rumble`'s -28 LUFS; the canopy 3 dB under them as before), the loops by their band RMS.
Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r46k.py [subject-or-file ...]   e.g. dash mars-canyon-r46-b
       python3 tools/concept/audio/sfx_r46k.py --docs OUTDIR   also writes the CREDITS.md rows, the sfx README rows and
                                                               the prompts.md entries for the files made into OUTDIR
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
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r46k.py"
R = "r46"

# Freesound sound id -> (title, author, licence); the originals are cached as <id>_<name>.<type>.
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
SOURCES = {
    852881: ("Low wind in a desert canyon 2", "fran_marenco", CC0),
    438866: ("G55-12-Moaning Whistle Wind.wav", "craigsmith", CC0),
    157204: ("howling wind.WAV", "inchadney", BY4),
    438877: ("G56-22-Sandstorm.wav", "craigsmith", CC0),
    266656: ("Pink Lakes Wind Atmos #2", "kangaroovindaloo", CC0),
    671456: ("Arena Underground Ventilation Hum and Rumble", "jamesdrake89", CC0),
    451145: ("freight train distant short and clicky.flac", "kyles", CC0),
    414739: ("Shunting Atmos", "kangaroovindaloo", BY4),
    166014: ("Ambient Industrial Ventilation Soundscape", "amenhotepIV", CC0),
    31566: ("Foundry Furnace.wav", "Walter_Odington", CC0),
    31567: ("Metalurgists At The Foundary.wav", "Walter_Odington", CC0),
    567148: ("Steel mill low frequency drone", "sarana", CC0),
    872936: ("Dark Voice Drone 02 - Loop", "kkenny101", CC0),
    451923: ("room tone large room church basement heavy deep ventilation or furnace hum quiet and clicking that "
             "picks up in middle.flac", "kyles", CC0),
    362914: ("Tuuli sähkölangoissa / Wind humming and howling in the electric wires, power line", "YleArkisto", BY4),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0),
    343682: ("Blue Whale B call @ 5X", "MBARI_MARS", BY4),
    399609: ("Underwater recording of Humpback whales and their calves", "alanmcki", BY4),
    572005: ("Underwater Rumble", "EminYILDIRIM", BY4),
    400793: ("Underwater Impact", "deleted_user_7709760", BY3),
    529620: ("Massive Structure Bend", "Kostas17", BY3),
    19482: ("JacobsLadderLong1.flac", "Halleck", BY4),
    19486: ("JacobsLadderSingle1.flac", "Halleck", BY4),
    675760: ("S16-18 Tesla coil sparking and humming.wav", "craigsmith", CC0),
    814061: ("ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light "
             "Bulb_YMLT_NONE_08982261", "YouMightLikeThis", CC0),
    123253: ("MechanicalClamp.wav", "Skullsmasha", CC0),
    524205: ("Radio Sign Off / Squelch", "JovianSounds", CC0),
    675782: ("S29-08 Navy ship classic danger whistle siren.wav", "craigsmith", CC0),
    104882: ("Submarie dive horn_better.wav", "CGEffex", BY4),
    537241: ("Drone take off", "GeorgeHopkins", BY4),
    683300: ("Dji Mavic Mini 2 take-off UP-DOWN XY mic placement Zoom H5.wav", "Sadiquecat", CC0),
    629994: ("BANGING ON THICK GLASS.wav", "trexlasso", CC0),
    245033: ("Thuds on Window", "zerolagtime", BY4),
    402459: ("Door Close Heavy Metal Glass Slow Creak Seal Theatre", "LampEight", CC0),
    402460: ("Door Close Heavy Metal Glass Medium Creak Seal Theatre", "LampEight", CC0),
    262633: ("Ice Crack 3", "j_p_higgins", CC0),
    262632: ("Ice Crack 4", "j_p_higgins", CC0),
    863981: ("Tire Bead Seater Air Blast", "j_soundeffects", BY4),
    51808: ("jet thruster bassy potential.aiff", "kyles", CC0),
    829068: ("Afterburner (Close, Screamer, Quick Onset)", "JSK_Productions", CC0),
    494044: ("Gas Release", "adgawrhbshbffsfgvsrf", CC0),
    193610: ("Power Charge", "crashoverride6", CC0),
    434869: ("Powerfull Whooshes 2.wav", "Nic3_one", CC0),
    132560: ("Electronic_Powerup.wav", "StephenSaldanha", BY4),
    653294: ("Big Structure Collision Heard from Inside", "Osvoldon", CC0),
    415962: ("high-voltage-discharge-2.wav", "follytowers", CC0),
    403252: ("Taser/High Voltage discharge", "The_Chemical_Workshop", CC0),
    539968: ("Deep Boom", "Za-Games", CC0),
    709258: ("Large machine running and powering down", "ryks88", BY4),
    396511: ("Arc Welding.wav", "kev_durr", BY4),
    264827: ("MIG Welding", "unfa", CC0),
    457294: ("Air (or steam) pressure release", "brunoboselli", CC0),
    438133: ("G16-09-Factory-Overhead Crane.wav", "craigsmith", CC0),
    813053: ("Big robot footstep 002", "AudioPapkin", CC0),
    613158: ("MACHFan_big_industrial_fan_white_noise_Olympus_LS-P4.wav", "DBS_Sounds", CC0),
    721601: ("Giant step", "DasWaff", CC0),
    407322: ("metal heavy dungeon prison door unlatch open close thud rattle and hatch open close slide squeak various "
             "int perspective on offmic.wav", "kyles", CC0),
    516643: ("Squelch.mp3", "LucasDuff", CC0),
    607310: ("Rotating tank turret planetary electric motor", "lorefold", CC0),
    858506: ("M&P15 Bolt Release [No Ring]", "smill.and.welson", BY4),
    739197: ("Sci-Fi Mechanical Door", "CheatinSloth", CC0),
    581083: ("Pressure Hiss", "magnuswaker", CC0),
    640505: ("Eurofighter Typhoon Flyby 005 – Close Proximity", "TimoSchmied", BY4),
    814976: ("Closing large metal door-001", "AudioPapkin", CC0),
    493558: ("steam_safety_valve.wav", "Engineer_815", CC0),
    852991: ("Hydraulic Hiss, TTC bus door", "OttisG", BY4),
    368650: ("Train, Subway, Pass By, Under Bridge Perspective", "Kinoton", CC0),
    452780: ("Heavy Gate Metal 01 (underground bunker of civil defense, Vsetin City, Club Vesmir)", "ALEFF-ATMOS", CC0),
    182359: ("MrM_Gas_Steam_Escape.wav", "mrmccormack", CC0),
}

def Hits(sound, start, length, ats, **kw):
    """A one-shot layer repeated inside a loop at the times `ats` (s), each wholly inside the loop (no seam)."""
    return dict(L(sound, start, length, **kw), ats=ats)


BED, BED_CAP = -36.0, -28.0  # the accepted beds: mars-dust's band RMS, basin-rumble's loudness

# subject -> (kind, level, ceiling dBFS, cap LUFS, what it replaces/where, {variant: (spec, what)})
# kind "bed": a loop levelled by its band RMS, at most `cap` LUFS; "loop": by its band RMS, at most `cap` LUFS;
# "shot": the loudest 100 ms of the band under the ceiling, at most `cap` LUFS. A loop's spec is (layers, length, xfade).
SOUNDS = {
    "canopy-hum": ("bed", BED - 3.0, -1.0, BED_CAP - 3.0,
                   "Level 14 (`Sfx.AMBIENCE_CANOPY`): the canopy's bed over the megacity while the canopy lives", {
        "a": (([Loop(872936, 2.0, speed=0.85, hp=30.0), Loop(451923, 10.0, gain=-5.0, hp=30.0)], 16.0, 1.0),
              "a deep time-stretched voice drone (the living canopy), slowed to 0.85, over a large basement's deep "
              "furnace hum"),
        "b": (([Loop(362914, 10.0, speed=0.7, hp=30.0), Loop(453444, 2.0, gain=-10.0, speed=0.8, hp=40.0)], 16.0, 1.0),
              "wind humming and howling in power lines, slowed to 0.7, over a transformer's hum in a large room"),
    }),
    "mars-canyon": ("bed", BED, -1.0, BED_CAP, "Level 16 (`mars-canyon`): the canyon wind and its howl", {
        "a": (([Loop(852881, 20.0, hp=30.0), Loop(438866, 4.0, gain=-5.0, hp=60.0)], 16.0, 1.0),
              "a low wind in a desert canyon with a moaning whistling wind howling through it"),
        "b": (([Loop(157204, 4.0, hp=120.0), Loop(438877, 30.0, gain=-8.0, hp=400.0, lp=6000.0)], 16.0, 1.0),
              "a howling wind over a sandstorm's grit"),
    }),
    "mars-colony": ("bed", BED, -1.0, BED_CAP,
                    "Level 17 (`mars-colony`): the plains wind, the domes' air plant, a distant rail", {
        "a": (([Loop(266656, 20.0, hp=40.0), Loop(671456, 2.0, gain=-6.0, hp=30.0),
                Loop(451145, 6.0, gain=-12.0, hp=60.0)], 16.0, 1.0),
              "an open plain's wind, an underground ventilation plant's hum and a distant freight train clicking by"),
        "b": (([Loop(438877, 50.0, gain=-2.0, hp=200.0, lp=6000.0), Loop(166014, 20.0, gain=-4.0, hp=50.0),
                Loop(414739, 80.0, gain=-10.0, hp=60.0)], 16.0, 1.0),
              "a thin sand wind, an industrial ventilation hum and a distant freight train's steel clanking"),
    }),
    "mars-foundry": ("bed", BED, -1.0, BED_CAP,
                     "Level 19 (`mars-foundry`): the conveyors, the furnace, a slow hammer, the plateau wind", {
        "a": (([Loop(31566, 0.5, hp=40.0), Loop(438133, 8.0, gain=-3.0, hp=40.0),
                Loop(852881, 20.0, gain=-6.0, hp=30.0)], 16.0, 1.0,
               [Hits(813053, 0.2, 1.3, (1.0, 5.0, 9.0, 13.0), gain=-8.0, fout=0.6, hp=30.0, lp=1500.0)]),
              "a foundry furnace's roar, a factory's overhead crane rumbling (the conveyors), a far heavy hammer every "
              "4 s and a low wind"),
        "b": (([Loop(31567, 100.0, hp=40.0), Loop(613158, 2.0, gain=-6.0, hp=40.0),
                Loop(567148, 2.0, gain=-10.0, hp=20.0), Loop(852881, 60.0, gain=-6.0, hp=30.0)], 16.0, 1.0,
               [Hits(721601, 0.6, 1.4, (3.0, 11.0), gain=-6.0, fout=0.6, hp=30.0, lp=2000.0)]),
              "a foundry's furnace, a big industrial fan (the blowers), a steel mill's deep drone, a low wind and a "
              "far giant hammer every 8 s"),
    }),
    "enemy-leviathan-cry": ("shot", -24.0, -4.0, -20.5, "the Leviathan's death cry (Level 03), under its large burst", {
        "a": ([L(343682, 3.35, 3.4, speed=0.9, fin=0.25, fout=0.8, hp=50.0),
               L(572005, 2.0, 3.0, gain=-8.0, fin=0.4, fout=1.0, hp=25.0)],
              "a blue whale's B call (a deep falling moan), slowed to 0.9, over an underwater rumble"),
        "b": ([L(399609, 21.3, 3.2, speed=0.5, fin=0.2, fout=0.9, hp=60.0, lp=1800.0),
               L(399609, 27.3, 2.0, gain=-5.0, at=1.4, speed=0.5, fin=0.2, fout=0.8, hp=60.0, lp=1800.0)],
              "two humpback whale calls, slowed to 0.5 (deep and mournful)"),
    }),
    "driftjelly-pulse": ("shot", -24.0, -4.0, -27.5, "the Driftjelly's ring pulse (Level 11)", {
        "a": ([L(400793, 0.0, 0.9, fin=0.03, fout=0.5, hp=30.0, lp=3000.0),
               L(572005, 2.0, 0.9, gain=-6.0, fin=0.1, fout=0.5, hp=25.0)],
              "a muffled underwater impact over an underwater rumble"),
        "b": ([L(529620, 1.0, 0.9, speed=0.8, fin=0.05, fout=0.5, hp=30.0),
               L(400793, 0.2, 0.7, gain=-4.0, fin=0.03, fout=0.4, hp=30.0, lp=2500.0)],
              "a massive structure bending (a deep groan), slowed to 0.8, with the underwater impact's tail"),
    }),
    "lamprey-drain": ("loop", -24.0, -1.0, -15.0, "the Lamprey's shield drain (Level 12) while it is latched on", {
        "a": (([Loop(19482, 33.0, speed=0.6, hp=60.0), Loop(453444, 2.0, gain=-6.0, hp=40.0)], 2.4, 0.3),
              "a Jacob's ladder's buzzing arc slowed to 0.6, over a transformer's hum"),
        "b": (([Loop(675760, 2.0, speed=0.7, hp=60.0), Loop(814061, 1.0, gain=-4.0, hp=30.0)], 2.4, 0.3),
              "a Tesla coil sparking and humming, slowed to 0.7, over a loose light fixture's hum and crackle"),
    }),
    "sonde-chime": ("shot", -16.0, -4.0, -12.0, "a weather sonde collected (Level 12)", {
        "a": ([L(123253, 2.18, 1.0, fin=0.005, fout=0.4, hp=30.0),
               L(524205, 0.02, 0.25, gain=-6.0, at=0.05, fin=0.005, fout=0.05, hp=200.0)],
              "a heavy mechanical clamp slamming shut with a radio squelch (the telemetry handed over); no bell"),
        "b": ([L(407322, 2.2, 0.9, fin=0.005, fout=0.5, hp=30.0),
               L(516643, 0.62, 0.4, gain=-6.0, at=0.05, fin=0.01, fout=0.15, hp=200.0)],
              "a heavy dungeon door's latch clanking shut with a radio squelch; no bell"),
    }),
    "integrity-alarm": ("shot", -17.0, -4.0, -13.0, "the integrity alarm at 50 % and 25 % (Level 13's relay, Level 17's "
                        "domes)", {
        "a": ([L(675782, 2.9, 1.7, fin=0.05, fout=0.3, hp=200.0)], "a navy ship's danger whistle siren (a real one)"),
        "b": ([L(104882, 0.0, 1.85, fin=0.02, fout=0.25, hp=150.0)], "a submarine's dive horn, two blasts (a real one)"),
    }),
    "drone-launch": ("shot", -20.5, -4.0, -17.5, "a drone launching (Level 13's relay, Level 17's colony)", {
        "a": ([L(537241, 0.3, 2.2, speed=0.75, fin=0.05, fout=0.8, hp=60.0)],
              "a drone taking off, slowed to 0.75 (bigger rotors)"),
        "b": ([L(683300, 1.3, 2.2, speed=0.75, fin=0.1, fout=0.8, hp=60.0)],
              "a quadcopter's rotors starting and taking off, slowed to 0.75"),
    }),
    "dome-hit": ("shot", -19.5, -4.0, -22.0, "a Level 17 dome struck (`ALLY_HIT`)", {
        "a": ([L(629994, 0.97, 0.6, fin=0.003, fout=0.4, hp=40.0),
               L(245033, 0.22, 0.6, gain=-3.0, fin=0.003, fout=0.4, hp=30.0),
               L(402459, 15.0, 1.0, gain=-8.0, at=0.2, fin=0.1, fout=0.4, hp=200.0),
               L(262633, 0.15, 1.0, gain=-10.0, at=0.1, fin=0.02, fout=0.4, hp=500.0)],
              "a bang on thick glass with a heavy thud behind it, a glass door's creak and ice cracking (the stress "
              "lines); no shatter"),
        "b": ([L(245033, 3.22, 0.7, fin=0.003, fout=0.5, hp=30.0),
               L(402460, 28.0, 1.0, gain=-8.0, at=0.15, fin=0.1, fout=0.4, hp=200.0),
               L(262632, 0.4, 1.1, gain=-10.0, at=0.1, fin=0.05, fout=0.4, hp=500.0),
               L(629994, 1.38, 0.3, gain=-6.0, fin=0.003, fout=0.2, hp=40.0)],
              "a heavy thud on a window, a heavy glass door's creak and a frozen lake's fine cracking; no shatter"),
    }),
    "dash": ("shot", -20.6, -4.0, -16.0, "the ship's dash", {
        "a": ([L(863981, 0.12, 0.4, fin=0.01, fout=0.2, hp=40.0),
               L(51808, 7.0, 0.4, gain=-4.0, fin=0.02, fout=0.2, hp=30.0)],
              "a compressed-air blast with a jet thruster's bassy roar"),
        "b": ([L(829068, 2.3, 0.4, speed=1.2, fin=0.015, fout=0.2, hp=60.0),
               L(494044, 0.3, 0.4, gain=-5.0, fin=0.01, fout=0.2, hp=500.0)],
              "an afterburner's roar, sped to 1.2, with a gas release's hiss"),
    }),
    "overcharge-whoosh": ("shot", -19.0, -4.0, -18.0, "Shield Overcharge's activation", {
        "a": ([L(193610, 4.4, 1.0, speed=(0.6, 1.1), fin=0.1, fout=0.15, hp=100.0, swell=(-12.0, 1.2)),
               L(434869, 0.45, 0.6, gain=-4.0, at=0.35, fin=0.05, fout=0.3)],
              "a camera flash charging, its whine gliding up, a powerful whoosh at its end"),
        "b": ([L(132560, 1.0, 1.0, speed=(0.8, 1.25), fin=0.08, fout=0.15, hp=60.0, swell=(-10.0, 1.2)),
               L(675760, 2.0, 1.0, gain=-8.0, fin=0.2, fout=0.15, hp=1000.0, swell=(-12.0, 1.0))],
              "a phasing power-up gliding up with a Tesla coil's sparks swelling under it"),
    }),
    "overcharge-end": ("shot", -24.0, -4.0, -22.0, "the end of Shield Overcharge's 8 s", {
        "a": ([L(193610, 5.0, 0.9, speed=(1.0, 0.45), fin=0.03, fout=0.5, hp=100.0)],
              "the camera flash's charge whine gliding down and fading"),
        "b": ([L(132560, 2.0, 0.9, speed=(1.1, 0.5), fin=0.03, fout=0.5, hp=60.0),
               L(675760, 5.0, 0.9, gain=-10.0, fin=0.02, fout=0.7, hp=1000.0)],
              "the phasing power-up gliding down with the Tesla coil's sparks dying"),
    }),
    "emp-ring": ("shot", -18.0, -4.0, -17.0, "the EMP Burst's ring", {
        "a": ([L(19482, 19.95, 1.35, fin=0.01, fout=0.7, hp=60.0),
               L(675760, 3.0, 1.2, gain=-6.0, fin=0.01, fout=0.8, hp=500.0),
               L(653294, 0.0, 1.3, gain=-6.0, fin=0.01, fout=0.9, hp=25.0)],
              "a Jacob's ladder's arc buzzing out, a Tesla coil's sparks and a massive structure's deep impact"),
        "b": ([L(415962, 7.2, 1.35, fin=0.01, fout=0.7, hp=30.0),
               L(403252, 0.78, 0.4, gain=-4.0, fin=0.003, fout=0.3, hp=200.0),
               L(539968, 0.0, 1.35, gain=-6.0, fin=0.01, fout=0.8, hp=25.0)],
              "a high-voltage discharge with a taser's crack on its attack over a deep boom"),
    }),
    "emp-powerdown": ("shot", -24.0, -4.0, -20.0, "a machine stunned by the EMP", {
        "a": ([L(709258, 61.0, 0.9, speed=(3.0, 1.0), fin=0.01, fout=0.4, hp=40.0)],
              "a large machine powering down, played fast and gliding down from 3x to 1x"),
        "b": ([L(453444, 2.0, 0.9, speed=(1.0, 0.6), fin=0.01, fout=0.4, hp=40.0),
               L(814061, 2.0, 0.9, gain=-3.0, fin=0.01, fout=0.4, hp=30.0)],
              "a transformer's hum sagging (gliding 1.0 to 0.6) with a loose light fixture's flickering crackle"),
    }),
    "ion-beam-stop": ("shot", -25.0, -4.0, -21.0, "the Ion Beam's collapse when it stops", {
        "a": ([L(675760, 4.0, 0.62, speed=(1.0, 0.35), fin=0.01, fout=0.35, hp=40.0),
               L(709258, 61.0, 0.62, gain=-6.0, speed=(2.0, 0.6), fin=0.01, fout=0.4, hp=30.0)],
              "a Tesla coil's hum and a large machine gliding down together (1.0 to 0.35)"),
        "b": ([L(19482, 20.0, 0.62, speed=(0.9, 0.3), fin=0.01, fout=0.35, hp=40.0),
               L(453444, 4.0, 0.62, gain=-6.0, speed=(1.0, 0.5), fin=0.01, fout=0.4, hp=40.0)],
              "a Jacob's ladder's arc buzz and a transformer's hum gliding down to nothing"),
    }),
    "beam-impact": ("loop", -38.0, -1.0, -26.0, "where any beam hits (a loop while it burns)", {
        "a": (([Loop(396511, 0.2, hp=60.0)], 1.5, 0.25), "arc welding: a dry crackling sizzle over its 120 Hz buzz"),
        "b": (([Loop(264827, 4.0, hp=60.0), Loop(19482, 33.0, gain=-8.0, speed=0.6, hp=60.0)], 1.5, 0.25),
              "MIG welding over a Jacob's ladder's arc buzz slowed to 0.6"),
    }),
    "drone-rebuild": ("shot", -25.0, -4.0, -22.0, "the Combat Drone rebuilt", {
        "a": ([L(607310, 0.1, 0.45, speed=(0.8, 1.2), fin=0.03, fout=0.08, hp=60.0, swell=(-8.0, 1.0)),
               L(858506, 0.05, 0.2, gain=-3.0, at=0.4, fin=0.005, fout=0.12, hp=100.0)],
              "an electric turret motor whirring up (gliding 0.8 to 1.2) into a rifle bolt's metal clack"),
        "b": ([L(739197, 0.35, 0.6, fin=0.01, fout=0.2, hp=60.0)],
              "a sci-fi mechanical door's servo and clunk"),
    }),
    "launch-rail": ("shot", -20.8, -4.0, -13.0, "the catapult run at every level start (the buffer clunk at 3.6 s)", {
        "a": ([L(457294, 6.0, 3.6, gain=-6.0, fin=0.3, fout=0.3, hp=200.0, swell=(-14.0, 1.0)),
               L(640505, 0.5, 3.7, fin=0.3, fout=0.2, hp=30.0),
               L(814976, 0.0, 1.3, at=3.55, fin=0.005, fout=0.8, hp=25.0),
               L(493558, 1.4, 1.3, gain=-10.0, at=3.6, fin=0.05, fout=0.9, hp=200.0)],
              "a pressure hiss building and a fighter jet's close pass (the shuttle's run) into a large metal door "
              "slamming (the buffer) and a steam safety valve venting"),
        "b": ([L(852991, 0.2, 2.0, gain=-6.0, fin=0.1, fout=0.6, hp=200.0),
               L(368650, 5.9, 3.65, fin=1.0, fout=0.15, hp=30.0, swell=(-6.0, 1.0)),
               L(452780, 2.95, 1.3, at=3.45, fin=0.005, fout=0.8, hp=30.0),
               L(182359, 4.2, 1.2, gain=-10.0, at=3.7, fin=0.05, fout=0.9, hp=200.0)],
              "a hydraulic hiss, a subway train rushing past under a bridge (the shuttle's run), a bunker's heavy "
              "metal gate slamming (the buffer) and gas and steam escaping"),
    }),
}


def hits_of(spec):
    return spec[3] if isinstance(spec, tuple) and len(spec) > 3 else []


def layers_of(spec):
    return (list(spec[0]) + hits_of(spec)) if isinstance(spec, tuple) else spec


def looped(spec):
    """The loop's layers cross-faded into a seamless loop, then the hits added where they fall."""
    st = loop(*spec[:3])
    for hit in hits_of(spec):
        one = {k: v for k, v in hit.items() if k != "ats"}
        for at in hit["ats"]:
            x = mix([dict(one, at=at)])
            assert x.shape[1] <= st.shape[1], "a hit runs past the loop's end"
            st[:, :x.shape[1]] += x
    return st


def sources_of(spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(spec)))


def capped(st, cap, ceiling):
    """At most `cap` LUFS (lowered, not limited)."""
    loud = lufs(st)[0]
    return st * undb(cap - loud) if np.isfinite(loud) and loud > cap else st


def build(subject, v):
    kind, band, ceiling, cap, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    if kind in ("loop", "bed"):
        st = looped(spec)
        st = st - st.mean(axis=1, keepdims=True)
        st = capped(st * undb(band - band_rms_db(st)), cap, ceiling)
    else:
        raw = mix(spec)
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind in ("loop", "bed"):
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap, _, _ = SOUNDS[subject]
    if kind == "bed":
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz, the accepted beds), at most {num(cap)} LUFS"
    if kind == "loop":
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz, as the sound it replaces), at most {num(cap)} LUFS"
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} dBFS "
            f"ceiling, at most {num(cap)} LUFS")


def change_text(subject, spec, layer):
    kind = SOUNDS[subject][0]
    parts = ["Freesound original (Freesound API)"]
    speed = layer["speed"]
    if "ats" in layer:
        parts.append(f"cut {num(layer['start'])}–{layer['start'] + layer['length'] * speed:.2f} s")
        if layer["hp"]:
            parts.append(f"{num(layer['hp'])} Hz high-pass")
        if layer["lp"]:
            parts.append(f"{num(layer['lp'])} Hz low-pass")
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        parts.append("placed at " + ", ".join(num(at) for at in layer["ats"]) + f" s in the {num(spec[1])} s loop")
        parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
        parts += ["mixed with the other layers", level_text(subject), "OGG Vorbis q6"]
        return ", ".join(parts)
    if kind in ("loop", "bed"):
        length, xfade = spec[1], spec[2]
        parts.append(f"seamless {num(length)} s loop from {num(layer['start'])} s")
        if speed != 1.0:
            parts.append(f"slowed to {num(speed)}" if speed < 1 else f"sped to {num(speed)}")
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
    if kind in ("loop", "bed"):
        parts.append(f"{num(spec[2])} s equal-power cross-fade")
    else:
        if layer["swell"]:
            parts.append(f"swelling from {num(layer['swell'][0])} dB")
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers_of(spec)) > 1:
        parts.append("mixed with the other layers")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(spec):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(spec)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        kind, _, _, _, use, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in layers_of(spec):
            s = layer["sound"]
            title, author, lic = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        if kind == "bed":
            size = f"seamless {secs:.1f} s loop ({band_rms_db(x):.0f} dB band RMS, {lufs(x)[0]:.0f} LUFS)"
        elif kind == "loop":
            size = f"seamless {secs:.2f} s loop ({band_rms_db(x):.0f} dB)"
        else:
            size = f"{secs:.2f} s ({short_term_band_db(x):.0f} dB)"
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in sources_of(spec))
        size = size.replace("(-", "(−").replace(", -", ", −")
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound ({licences(spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: {what}; {names}, {size} | "
                    f"proposed (round 46) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in sources_of(spec))
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in sources_of(spec))
        label = "Sources" if len(sources_of(spec)) > 1 else "Source"
        if kind in ("loop", "bed"):
            edit = (f"seamless {secs:.2f} s loop, band RMS {band_rms_db(x):.1f} dB, peak {db(np.abs(x).max()):.1f} "
                    f"dBFS, {lufs(x)[0]:.1f} LUFS")
        else:
            edit = (f"{secs:.2f} s, the loudest 100 ms band {short_term_band_db(x):.1f} dB, peak "
                    f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        prompts.append(f"### {name} — {by}\n\nProposed (round 46).\n{label}: {links}. Use: {use}.\n"
                       f"Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. "
                       f"Why: {what}.\nEdit: {edit.replace('-', '−')}.\n")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, (_, _, _, _, _, variants) in SOUNDS.items():
        for v in variants:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
