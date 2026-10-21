#!/usr/bin/env python3
"""Concept round 44 (M7 part H, Level 21 "Dust Colossus", step AUD-H): the Dust Colossus's sounds and the Revenant
Walker's two new attacks (step WR2: the rail lance and the stomp shockwave), cut from real recordings in the Freesound
database (the user, round 42: "You should really stop trying to make these sounds yourself. Use the database."), an
a/b pair each. Every layer is a Freesound original (CC0 or CC-BY, in CREDITS.md; fetched with
`freesound_fetch.py --download` into ~/.cache/terran-vanguard/freesound/); this script only cuts, slows (a fixed speed
or a gliding one), layers, fades, filters, pans and levels them: nothing is synthesized. No bubbly or popping sources;
every blast starts with a ramped attack (15-40 ms). The same tools as sfx_r43.py and sfx_r43g.py (Levels 19 and 20).
Pending the user's review in round 44.

Outputs (design/audio/sfx/concept/, the variants the user rejected in concept/rejected/; OGG Vorbis q6, 44.1 kHz
stereo):

  colossus-bulge-r44-a      a breach's 2.0 s tell (the sand bulges and cracks): an earthquake's cracking soil and
                            stone swelling in, sand pouring off the bulge on top; 2.1 s
  colossus-bulge-r44-b      a large low rumble's crescendo with a heavy stone grind in it and sand flowing; 2.1 s
  colossus-eruption-r44-a   the rise (and phase 3's eruption under the ship): an earth impact's boom, a deep
                            creature roar rising out of it and an avalanche of rock and sand pouring back; 2.6 s
  colossus-eruption-r44-b   a massive explosion's boom, a deep monster roar and a landslide's roar under it, sand
                            pouring back; 2.6 s
  colossus-suction-r44-a    the suction while a mouth is open, a seamless 4.0 s LOOP: air rushing into a chimney (a
                            deep steady draw) under a sandstorm (the sand streaming)
  colossus-suction-r44-b    a seamless 4.0 s LOOP: a large industrial turbine's fan, slowed to 0.8 (a deep
                            roaring draw), with a monster's inhale under it and sand flowing
  colossus-bite-r44-a       the bite's snap: a massive structure's deep impact with bones crunching (slowed to 0.6,
                            the chitin grinding) and a large stone scraping; 1.0 s
  colossus-bite-r44-b       an earth impact's boom with a bone crack slowed to 0.55 (the jaws) and a heavy stone grind after
                            it; 1.0 s
  colossus-death-r44-a      the `huge` death: a deep roar shuddering, chained eruptions (a large explosion with
                            debris, an earth impact, a massive explosion), then a rock avalanche's rumble subsiding
                            as the crater collapses; 6.0 s
  colossus-death-r44-b      a deep monster roar, a building collapsing and two deep booms over a landslide, an
                            avalanche of rock pouring in as it subsides; 6.0 s
  colossus-ridge-r44-a      the ridge pass (Level 21, t~64-72): a subway train passing under a bridge (a travelling
                            rumble), sand flowing off the ridge, panned left to right; 7.0 s
  colossus-ridge-r44-b      a large low rumble passing, a heavy stone grind and sand pouring, panned right to left;
                            7.0 s
  walker-lance-charge-r44-a the rail lance's 1.0 s charge tell: a power charge rising (its pitch gliding up) with a
                            Tesla coil's sparking swelling under it; 1.05 s
  walker-lance-charge-r44-b a 15 kV high-voltage discharge's buzz swelling and gliding up, the Tesla coil's sparks
                            on top; 1.05 s
  walker-lance-shot-r44-a   the lance's shot: a real railgun (an extremely loud cannon); 1.6 s
  walker-lance-shot-r44-b   a shipboard railgun's heavy boom with a high-voltage discharge's crack on its attack;
                            1.6 s
  walker-shockwave-r44-a    the stomp shockwave: a giant's footfall (the ground slam) and an explosion's shockwave
                            rolling out of it; 1.6 s
  walker-shockwave-r44-b    a giant's step, a loud thump and a massive structure's deep impact rolling out; 1.6 s

Levels like the accepted round-43 sounds (sfx_r43.py): one-shots on the loudest 100 ms of the 200 Hz-5 kHz band under
a -4 dBFS ceiling (the act boss's death -1.5 dBFS, the `huge` rung, as the walker's death), the suction by the whole
loop's band RMS (the walker's flame loop, -28 dB). The ridge pass plays over the basin bed (`basin-rumble`, -28 LUFS)
and is nearly all below 200 Hz, so it is levelled by its loudness (-20 LUFS, 8 LU over the bed) under -4 dBFS. Each
layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r44.py [subject-or-file ...]   e.g. colossus-bite walker-lance-shot-r44-b
       python3 tools/concept/audio/sfx_r44.py --mirror colossus-ridge-r44-a
The mirror option swaps the two channels (the pan runs the other way) and writes the game's copy to assets/sfx/ under
the concept name instead of the concept file (round 44, the user: the ridge pass a, but Level 21's ridge runs from the
right edge to the left, so the game's colossus-ridge-r44-a.ogg is a's render mirrored, panned right to left; the
concept file keeps a's left-to-right pan). importPlaceholders does not copy that file (pipeline/build.gradle.kts).
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
from synth import db, decode, limiter, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
ASSETS = ROOT / "assets" / "sfx"
# The user's round-44 picks (2026-10-20): a each except the lance charge (b); the other variants are written to
# concept/rejected/, where they moved after the pick.
REJECTED = {"colossus-bulge-r44-b", "colossus-eruption-r44-b", "colossus-suction-r44-b", "colossus-bite-r44-b",
            "colossus-death-r44-b", "colossus-ridge-r44-b", "walker-lance-charge-r44-a", "walker-lance-shot-r44-b",
            "walker-shockwave-r44-b"}
SCRIPT = "tools/concept/audio/sfx_r44.py"

# Freesound sound id -> (title, author, licence); the originals are cached as <id>_<name>.<type>.
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
SOURCES = {
    222521: ("Cracking Earthquake (cracking soil, cracking stone)", "uagadugu", CC0),
    199332: ("sand pour on sand FF662.aif", "martinimeniscus", CC0),
    130972: ("Large, Low Rumble.wav", "duckduckpony", BY4),
    760672: ("Guterton - Heavy Stone Grind", "guterton.wav", BY4),
    215006: ("Flowing Sand", "SteveMeli", BY3),
    815336: ("superhero_earth_impact_hit_07062025", "Artninja", BY4),
    345735: ("Deep Roar Echo 2.wav", "noahpardo", CC0),
    162668: ("avl04.wav", "blaukreuz", CC0),
    177556: ("massive explosion", "Hugofski", CC0),
    207746: ("Deep Monster Roar 2.wav", "MisterSegev", BY4),
    543837: ("Landslide.wav", "bikesnbassboi", BY4),
    454020: ("fireplace air rush into chimney steady wind current hum.flac", "kyles", CC0),
    438877: ("G56-22-Sandstorm.wav", "craigsmith", CC0),
    453476: ("motor industrial turbine large fan blades spin sorta.flac", "kyles", CC0),
    132865: ("Monster Inhale.wav", "ecfike", CC0),
    653294: ("Big Structure Collision Heard from Inside", "Osvoldon", CC0),
    445987: ("Horror / Zombie - Biting on bones", "Breviceps", CC0),
    572754: ("Scraping and Clunking Large Stone 1.wav", "F.M.Audio", BY4),
    539968: ("Deep Boom", "Za-Games", CC0),
    369601: ("Bone Crunch/Crack 2 (Mixed)", "EvanBoyerman", BY3),
    345736: ("NPX Deep Roar Echo 1.wav", "noahpardo", CC0),
    674899: ("S17-28 Large explosion with debris.wav", "craigsmith", CC0),
    841540: ("rock_avalanche_w_rumbling_sound_01092026", "Artninja", BY4),
    197770: ("building_collapse04_enclosed_space.wav", "onteca", BY3),
    162670: ("avl02.wav", "blaukreuz", CC0),
    368650: ("Train, Subway, Pass By, Under Bridge Perspective", "Kinoton", CC0),
    257229: ("charging power", "JavierZumer", BY4),
    675760: ("S16-18 Tesla coil sparking and humming.wav", "craigsmith", CC0),
    415962: ("high-voltage-discharge-2.wav", "follytowers", CC0),
    162260: ("Railgun - Extremely LOUD Cannon", "qubodup", CC0),
    155790: ("shipboard_railgun.mp3", "deleted_user_1941307", CC0),
    403252: ("Taser/High Voltage discharge", "The_Chemical_Workshop", CC0),
    458042: ("Giant Walking.wav", "facklere", CC0),
    855245: ("Explosion and Shockwave 1", "qubodup", CC0),
    721601: ("Giant step", "DasWaff", CC0),
    663057: ("loudthump.wav", "777hr1", BY4),
}


def panned(st, start, end):
    """The mix folded to mono and moved across the stereo field from pan `start` to `end` (-1 left, +1 right;
    equal power): the ridge travelling past the ship."""
    mono = st.mean(axis=0)
    p = np.linspace(start, end, mono.size)
    angle = (p + 1) * np.pi / 4
    return np.array([mono * np.cos(angle), mono * np.sin(angle)]) * np.sqrt(2)


def loudness(st, target, ceiling):
    """Levelled to `target` LUFS under the `ceiling` (dBFS); limiting lowers the level a little, so make it up."""
    st = st - st.mean(axis=1, keepdims=True)
    for _ in range(3):
        st = limiter(st * undb(target - lufs(st)[0]), ceiling)
    return st


# subject -> (kind, level, ceiling dBFS, {variant: (spec, what)}); a loop's spec is (layers, length, xfade), a
# "pass" spec is (layers, pan from, pan to). The level is the band RMS (dB) for a "loop", the loudness (LUFS) for a
# "pass", the loudest 100 ms of the band for a "shot".
SOUNDS = {
    "colossus-bulge": ("shot", -20.0, -4.0, {
        "a": ([L(222521, 2.0, 2.1, fin=0.25, fout=0.3, hp=25.0, swell=(-12.0, 1.3)),
               L(199332, 4.5, 2.1, gain=-2.0, at=0.0, fin=0.4, fout=0.3, hp=200.0, lp=6000.0, swell=(-10.0, 1.0))],
              "an earthquake's cracking soil and stone swelling in, sand pouring off the bulge"),
        "b": ([L(130972, 6.2, 2.1, fin=0.25, fout=0.3, hp=25.0, swell=(-10.0, 1.2)),
               L(760672, 0.4, 2.1, gain=-3.0, fin=0.3, fout=0.3, hp=40.0, swell=(-8.0, 1.0)),
               L(215006, 1.0, 2.1, gain=-9.0, fin=0.5, fout=0.3, hp=200.0, lp=6000.0, swell=(-10.0, 1.0))],
              "a large low rumble's crescendo with a heavy stone grind and sand flowing"),
    }),
    "colossus-eruption": ("shot", -17.0, -4.0, {
        "a": ([L(815336, 0.0, 2.2, fin=0.03, fout=1.2, hp=25.0),
               L(345735, 1.4, 2.3, gain=-2.0, at=0.15, speed=0.85, fin=0.15, fout=0.8, hp=40.0),
               L(162668, 0.2, 2.4, gain=-5.0, at=0.2, fin=0.08, fout=1.0, lp=7000.0)],
              "an earth impact's boom, a deep creature roar rising out of it, rock and sand pouring back"),
        "b": ([L(177556, 7.75, 2.4, gain=-2.0, fin=0.03, fout=1.2, hp=25.0),
               L(207746, 1.25, 2.2, gain=-2.0, at=0.1, speed=0.9, fin=0.08, fout=0.8, hp=35.0),
               L(543837, 12.0, 2.5, gain=-6.0, at=0.05, fin=0.1, fout=1.0, hp=30.0),
               L(162670, 3.0, 2.2, gain=-5.0, at=0.3, fin=0.1, fout=1.0, lp=7000.0),
               L(199332, 8.0, 2.2, gain=-5.0, at=0.4, fin=0.2, fout=1.0, hp=200.0, lp=6000.0)],
              "a massive explosion's boom, a deep monster roar and a landslide's roar, sand pouring back"),
    }),
    "colossus-suction": ("loop", -28.0, -1.0, {
        "a": (([Loop(454020, 6.0, hp=28.0), Loop(438877, 20.0, gain=-4.0, hp=120.0)], 4.0, 0.4),
              "air rushing into a chimney (a deep steady draw) under a sandstorm"),
        "b": (([Loop(453476, 10.0, speed=0.8, hp=30.0), Loop(132865, 2.0, gain=-6.0, hp=28.0),
                Loop(215006, 2.0, gain=-10.0, hp=200.0, lp=6000.0)], 4.0, 0.4),
              "a large industrial turbine's fan slowed to 0.8, a monster's inhale under it, sand flowing"),
    }),
    "colossus-bite": ("shot", -18.0, -4.0, {
        "a": ([L(653294, 0.0, 0.9, gain=-3.0, fin=0.02, fout=0.75, hp=25.0),
               L(445987, 0.24, 0.6, gain=0.0, at=0.01, speed=0.6, fin=0.01, fout=0.4, hp=60.0),
               L(572754, 0.05, 0.6, gain=-3.0, at=0.08, speed=0.8, fin=0.02, fout=0.45)],
              "a massive structure's deep impact with bones crunching, slowed to 0.6, and a large stone scraping"),
        "b": ([L(815336, 0.02, 1.0, gain=-3.0, fin=0.02, fout=0.8, hp=25.0),
               L(369601, 0.17, 0.6, gain=0.0, at=0.0, speed=0.55, fin=0.01, fout=0.25, hp=60.0),
               L(760672, 1.0, 0.8, gain=-6.0, at=0.12, fin=0.05, fout=0.6, hp=40.0)],
              "an earth impact's boom with a bone crack slowed to 0.55 and a heavy stone grind after it"),
    }),
    "colossus-death": ("shot", -13.0, -1.5, {
        "a": ([L(345736, 1.5, 2.6, speed=0.85, fin=0.1, fout=0.8, hp=35.0),
               L(674899, 0.0, 3.0, gain=-1.0, at=0.5, fin=0.03, fout=1.2),
               L(815336, 0.0, 2.0, gain=-2.0, at=1.6, speed=0.9, fin=0.03, fout=1.0, hp=25.0),
               L(177556, 7.75, 3.5, gain=0.0, at=2.6, fin=0.03, fout=1.6, hp=25.0),
               L(841540, 12.0, 3.4, gain=-4.0, at=2.6, fin=0.4, fout=1.6, hp=25.0)],
              "a deep roar shuddering, chained eruptions, then a rock avalanche's rumble subsiding"),
        "b": ([L(207746, 1.25, 2.4, speed=0.8, fin=0.08, fout=0.8, hp=35.0),
               L(539968, 0.0, 2.5, gain=-3.0, at=0.4, fin=0.03, fout=1.2, hp=25.0),
               L(197770, 0.0, 3.0, gain=-1.0, at=1.3, fin=0.03, fout=1.2, hp=30.0),
               L(539968, 0.0, 3.0, gain=-2.0, at=2.6, speed=0.85, fin=0.03, fout=1.4, hp=25.0),
               L(543837, 11.0, 4.0, gain=-5.0, at=1.9, fin=0.3, fout=1.8, hp=28.0),
               L(162670, 2.0, 3.4, gain=-4.0, at=2.6, fin=0.3, fout=1.6, lp=7000.0)],
              "a deep monster roar, a building collapsing and two deep booms over a landslide, rock pouring in"),
    }),
    "colossus-ridge": ("pass", -20.0, -4.0, {
        "a": (([L(368650, 6.0, 7.0, fin=1.5, fout=2.5, hp=28.0),
                L(215006, 1.0, 7.0, gain=-12.0, fin=2.0, fout=2.5, hp=200.0, lp=6000.0)], -0.8, 0.8),
              "a subway train passing under a bridge, sand flowing off the ridge, panned left to right"),
        "b": (([L(130972, 4.0, 7.0, fin=1.5, fout=2.5, hp=25.0),
                L(760672, 2.0, 7.0, gain=-6.0, fin=2.0, fout=2.5, hp=40.0),
                L(199332, 10.0, 7.0, gain=-12.0, fin=2.0, fout=2.5, hp=200.0, lp=6000.0)], 0.8, -0.8),
              "a large low rumble passing, a heavy stone grind and sand pouring, panned right to left"),
    }),
    "walker-lance-charge": ("shot", -20.0, -4.0, {
        "a": ([L(257229, 2.6, 1.05, speed=(0.9, 1.25), fin=0.06, fout=0.06, hp=40.0, swell=(-12.0, 1.3)),
               L(675760, 4.0, 1.05, gain=-9.0, fin=0.1, fout=0.06, hp=200.0, swell=(-12.0, 1.0))],
              "a power charge rising, its pitch gliding up, a Tesla coil's sparking swelling under it"),
        "b": ([L(415962, 1.6, 1.05, speed=(0.8, 1.2), fin=0.06, fout=0.06, hp=40.0, swell=(-14.0, 1.3)),
               L(675760, 9.0, 1.05, gain=-8.0, fin=0.1, fout=0.06, hp=200.0, swell=(-12.0, 1.0)),
               L(130972, 7.0, 1.05, gain=-4.0, speed=(1.0, 1.3), fin=0.1, fout=0.06, hp=25.0, swell=(-12.0, 1.2))],
              "a high-voltage discharge's buzz swelling and gliding up, a Tesla coil's sparks on top, a low rumble rising"),
    }),
    "walker-lance-shot": ("shot", -16.0, -4.0, {
        "a": ([L(162260, 0.0, 1.6, fin=0.02, fout=0.7)], "a real railgun (an extremely loud cannon)"),
        "b": ([L(155790, 0.0, 1.6, fin=0.02, fout=0.6, hp=25.0),
               L(403252, 0.79, 0.3, gain=-6.0, at=0.0, fin=0.02, fout=0.2, hp=300.0)],
              "a shipboard railgun's heavy boom with a high-voltage discharge's crack on its attack"),
    }),
    "walker-shockwave": ("shot", -18.0, -4.0, {
        "a": ([L(458042, 4.27, 0.9, fin=0.02, fout=0.4, hp=25.0),
               L(855245, 0.0, 1.55, gain=-2.0, at=0.05, fin=0.03, fout=0.7)],
              "a giant's footfall and an explosion's shockwave rolling out of it"),
        "b": ([L(721601, 0.7, 0.8, fin=0.02, fout=0.35, hp=25.0),
               L(663057, 0.28, 0.9, gain=-2.0, at=0.03, fin=0.02, fout=0.4, hp=25.0),
               L(653294, 0.0, 1.4, gain=-8.0, at=0.08, fin=0.03, fout=1.2, hp=25.0),
               L(197770, 0.0, 1.0, gain=-6.0, at=0.04, fin=0.02, fout=0.8, hp=60.0)],
              "a giant's step, a loud thump and a massive structure's deep impact rolling out, rubble shaken loose"),
    }),
}


# The loudest a one-shot may be (LUFS): sub-heavy mixes reach the band level only through the limiter, so they stop at
# the accepted round-43 sounds' loudness instead (the tells about -16 LUFS, the blasts -14, the `huge` death -14).
CAPS = {"colossus-bulge": -16.0, "walker-lance-charge": -16.0, "colossus-death": -14.0}
CAP = -14.0


def layers_of(spec):
    return spec[0] if isinstance(spec, tuple) else spec


def sources_of(spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(spec)))


def build(subject, v, mirror=False):
    kind, band, ceiling, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-r44-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        st = st * undb(band - band_rms_db(st))
    elif kind == "pass":
        layers, start, end = spec
        st = loudness(panned(mix(layers), start, end), band, ceiling)
    else:
        raw = mix(spec)
        over = db(np.max(np.abs(raw)) * undb(band - short_term_band_db(raw))) - ceiling
        st = level(raw, band, ceiling)
        cap = CAPS.get(subject, CAP)
        if lufs(st)[0] > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
        print(f"  {name}: limiting {max(0.0, over):.1f} dB at the band level, cap {cap:.0f} LUFS")
    if mirror:  # the channels swapped: the pan runs the other way; the game's copy, not the concept file
        st = st[::-1].copy()
        what = f"{what}; mirrored (channels swapped) for the game"
    target = (ASSETS if mirror else OUT / "rejected" if name in REJECTED else OUT) / f"{name}.ogg"
    flag = " --mirror" if mirror else ""
    out = write_ogg(target, st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT}{flag} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")


def main(argv):
    mirror = "--mirror" in argv
    argv = [a for a in argv if a != "--mirror"]
    if mirror and not argv:
        sys.exit("--mirror needs the subjects or files to mirror, e.g. --mirror colossus-ridge-r44-a")
    for subject, (_, _, _, variants) in SOUNDS.items():
        for v in variants:
            if argv and subject not in argv and f"{subject}-r44-{v}" not in argv:
                continue
            build(subject, v, mirror)


if __name__ == "__main__":
    main(sys.argv[1:])
