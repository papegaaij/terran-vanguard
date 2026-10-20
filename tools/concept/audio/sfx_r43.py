#!/usr/bin/env python3
"""Concept round 43 (M7 part F, Level 19 "Foundry of Tharsis", step SND43): the Revenant Walker's and the Warden
Tank's sounds again, this time cut from real recordings in the Freesound database (the user, round 42: "You should
really stop trying to make these sounds yourself. Use the database."), an a/b pair each. Every layer is a Freesound
original (CC0 or CC-BY, in CREDITS.md; fetched with `freesound_fetch.py --download` into
~/.cache/terran-vanguard/freesound/); this script only cuts, slows, layers, fades and levels them: nothing is
synthesized. No bubbly or popping sources; every blast starts with a ramped attack (20-40 ms). Pending the user's
review in round 43.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  walker-hatch-r43-a         a pod's hatch (0.4 s tell): a truck's air brakes releasing (the hydraulic latch) and a
                             large metal door slamming (the armoured lid hitting its stop); 1.0 s
  walker-hatch-r43-b         a submarine hatch opening (its metal clank and creak) over a steam pressure release,
                             a massive structure's deep impact under the clank; 1.0 s
  walker-flame-tell-r43-a    the flamer's 0.6 s tell: "Venting.wav", a huge gas vent opening (hiss over a deep
                             rumble); 0.85 s
  walker-flame-tell-r43-b    an industrial 1000 hp boiler's safety valve opening, slowed to 0.85
  walker-flame-r43-a         the flame cone, a seamless 4.0 s LOOP: a flamethrower's long burst, slowed to 0.85
  walker-flame-r43-b         a seamless 4.0 s LOOP: a flamethrower's roar (a deep, sub-heavy burn)
  vent-tell-r43-a            a furnace vent's 1.0 s tell: a hot-air-balloon burner's roar swelling in (12 dB over
                             the tell)
  vent-tell-r43-b            another balloon burner (a brighter, hissier roar), swelling in the same way
  vent-burst-r43-a           the vent's burst: a flame burst (a deep whoomph) with the end of a gas burner's burn
                             on top, both dying away; 1.6 s
  vent-burst-r43-b           a fire burst swelling and roaring out ("Fire - Effects - Swooshes - Bursts"); 1.6 s
  tank-tracks-r43-a          the tracks, a seamless 4.0 s LOOP: a WW2 T-26 tank driving (tracks and engine)
  tank-tracks-r43-b          a seamless 4.0 s LOOP: a tracked bulldozer driving (deeper, steadier)
  tank-turret-r43-a          the turret slew: a real artillery turret re-aligning (motor and gear whine); 0.8 s
  tank-turret-r43-b          a factory overhead crane rolling (a heavy motor and rumbling gears); 0.9 s
  tank-burst-r43-a           the burst: a real tank cannon firing, three shots 0.15 s apart (the tank's medium
                             burst; 2 on easy, 4 on hard); 1.2 s
  tank-burst-r43-b           a heavy artillery shot, three shots 0.15 s apart; 1.4 s
  walker-vent-hit-r43-a      a hit on the open vents: a big hit on a lift door (deep metal); 0.55 s
  walker-vent-hit-r43-b      a hollow metal impact with a short burst of steam escaping; 0.5 s
  walker-death-r43-a         the `huge` death: a large explosion with debris (an optical-film recording), the hull
                             landing at 1.9 s (a massive structure's impact) with a heavy metal crash; 5.6 s
  walker-death-r43-b         two chained blasts, the hull's final boom at 1.85 s with a car crash's metal and a
                             radiator's metal rumbling; 5.6 s
  walker-wreck-settle-r43-a  the wreck settling: a bunker door's low metallic groan, slowed to 0.85, with an engine
                             cooling (slow ticks) under it; 5.0 s
  walker-wreck-settle-r43-b  a giant metal structure creaking under stress, with the same cooling ticks; 5.0 s

Levels like the accepted round-42 sounds (sfx_r42.py, the tank death and `mars-foundry`): one-shots on the loudest
100 ms of the 200 Hz-5 kHz band under a -4 dBFS ceiling (the death -1.5, like the `huge` rung), loops by the whole
loop's band RMS (one gain, no limiter). Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r43.py [subject-or-file ...]   e.g. tank-burst walker-death-r43-b
Deterministic (no randomness, bit-exact ffmpeg flags).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from import_sfx import band_rms_db, make_loop  # noqa: E402
from sfx_r24 import level, resample  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from synth import SR, decode, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound"
SCRIPT = "tools/concept/audio/sfx_r43.py"

# Freesound sound id -> (title, author, licence); the originals are cached as <id>_<name>.<type>.
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
SOURCES = {
    803052: ("AIRBrst-Samsung Galaxy Smartphone, CU_Truck, Air Brakes, Released_Nicholas Judy_TDC",
             "designerschoice", BY4),
    814976: ("Closing large metal door-001", "AudioPapkin", CC0),
    438732: ("G45-30-Submarine Hatch Opening.wav", "craigsmith", CC0),
    457294: ("Air (or steam) pressure release", "brunoboselli", CC0),
    48710: ("Venting.wav", "Sea Fury", BY3),
    493558: ("steam_safety_valve.wav", "Engineer_815", CC0),
    395039: ("Flamethrower_Weapon_Long_Bursts.wav", "AslakHostaker", CC0),
    396890: ("Flamethrower", "pugaeme", BY3),
    243763: ("gas vuur dakwerken gasbrander luchtballon.wav", "NickPeeters", CC0),
    778358: ("Hot air balloon burner", "YannSauvin", CC0),
    592572: ("Flame Burst", "magnuswaker", CC0),
    421879: ("Fire - Effects - Swooshes - Bursts, evolving", "GregorQuendel", BY4),
    329800: ("t26_moving_01.wav", "greatmganga", CC0),
    541240: ("tank bulldozer tracked vehicle sound effect atmo", "Garuda1982", BY4),
    171657: ("Tank or Tower Turret Moving and Turning", "qubodup", CC0),
    340634: ("Factory_Crane_Roll.wav", "mickyman5000", CC0),
    168707: ("Tank Firing", "qubodup", CC0),
    423108: ("Guns & Explosions Album - Autocannon - Shot 1.wav", "OGsoundFX", BY4),
    329515: ("Big stereo metal hit", "LPA134", CC0),
    508272: ("Old_Truck_Metal_Squeaks-Creaks-Rattle-Impact_Hollow_Impact_Soft_Squeak", "dilsun", CC0),
    90143: ("steam_burst.wav", "pengo_au", CC0),
    674899: ("S17-28 Large explosion with debris.wav", "craigsmith", CC0),
    675462: ("S10-13 Large hard metal crash; optical.wav", "craigsmith", CC0),
    653294: ("Big Structure Collision Heard from Inside", "Osvoldon", CC0),
    459973: ("Huge Explosion", "florianreichelt", CC0),
    177556: ("massive explosion", "Hugofski", CC0),
    592388: ("Car Crash (with Glass)", "magnuswaker", CC0),
    51097: ("Sounds For Earthquakes - Radiator Metal Rumbling.wav", "RutgerMuller", CC0),
    496836: ("Metallic Groan", "hinchinbrook", CC0),
    539514: ("Clicking Engine Cooldown", "CmdRobot", CC0),
    172411: ("Creaking Metal - Slow", "EagleStealthTeam", CC0),
}


def original(sound):
    files = sorted(p for p in CACHE.glob(f"{sound}_*") if not p.name.endswith(".part"))
    if not files:
        sys.exit(f"Freesound sound {sound} is not cached: run tools/concept/audio/freesound_fetch.py --download")
    return files[0]


_decoded = {}


def L(sound, start, length, gain=0.0, at=0.0, speed=1.0, fin=0.005, fout=0.05, hp=30.0, lp=None, swell=None):
    """A layer: `length` s of the original from `start` s, played at `speed`, high-passed (DC and rumble) and
    optionally low-passed, faded in and out (raised cosine), peak-normalised, `gain` dB, placed at `at` s.
    swell=(from_db, power): a gain ramp from from_db up to 0 dB over the whole layer (a roar building up)."""
    return dict(sound=sound, start=start, length=length, gain=gain, at=at, speed=speed, fin=fin, fout=fout, hp=hp,
                lp=lp, swell=swell)


def cut(layer):
    sound = layer["sound"]
    if sound not in _decoded:
        _decoded[sound] = decode(original(sound))
    x = _decoded[sound]
    a = int(layer["start"] * SR)
    x = x[:, a:a + int(layer["length"] * SR)].copy()
    if layer["speed"] != 1.0:
        x = resample(x, layer["speed"])
    x -= x.mean(axis=1, keepdims=True)
    if layer["hp"]:
        x = np.array([svf(svf(ch, layer["hp"], mode="hp"), layer["hp"], mode="hp") for ch in x])
    if layer["lp"]:
        x = np.array([svf(svf(ch, layer["lp"]), layer["lp"]) for ch in x])
    n = x.shape[1]
    if layer["swell"]:
        from_db, power = layer["swell"]
        x *= undb(from_db * (1 - np.linspace(0, 1, n)) ** power)
    k = max(1, int(layer["fin"] * SR))
    x[:, :k] *= 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, k))
    k = min(n, int(layer["fout"] * SR))
    x[:, n - k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return x / (np.max(np.abs(x)) + 1e-12) * undb(-1.0 + layer["gain"])


def mix(layers, tail=0.0):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    n = max(at + x.shape[1] for x, at in parts) + int(tail * SR)
    out = np.zeros((2, n))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def loop(sound, start, length, xfade, speed=1.0, hp=30.0):
    """A seamless loop: `length` s (after the speed change) from `start`, the `xfade` s after its end
    cross-faded into its start (equal power; import_sfx.py's make_loop)."""
    if sound not in _decoded:
        _decoded[sound] = decode(original(sound))
    x = _decoded[sound]
    a = int(start * SR)
    x = x[:, a:a + int((length + xfade) * speed * SR) + SR // 10].copy()
    if speed != 1.0:
        x = resample(x, speed)
    x -= x.mean(axis=1, keepdims=True)
    if hp:
        x = np.array([svf(svf(ch, hp, mode="hp"), hp, mode="hp") for ch in x])
    return make_loop(x, 0.0, length, xfade)


def shots(sound, start, length, times, gains, fout, speeds=None, fin=0.004):
    """A burst: the same shot at each time, at its gain (dB) and speed."""
    speeds = speeds or [1.0] * len(times)
    return [L(sound, start, length, gain=g, at=t, speed=s, fin=fin, fout=fout)
            for t, g, s in zip(times, gains, speeds)]


# subject -> (kind, band level dB, ceiling dBFS, {variant: (layers or loop args, what)})
SOUNDS = {
    "walker-hatch": ("shot", -20.0, -4.0, {
        "a": ([L(803052, 0.0, 0.45, gain=-5.0, fin=0.01, fout=0.25),
               L(814976, 0.0, 0.95, gain=0.0, at=0.1, fin=0.004, fout=0.45)],
              "air brakes releasing and a large metal door slamming"),
        "b": ([L(457294, 0.02, 0.35, gain=-9.0, fin=0.02, fout=0.2),
               L(438732, 2.42, 0.95, gain=0.0, at=0.06, fin=0.006, fout=0.35),
               L(653294, 0.0, 0.6, gain=-6.0, at=0.22, fin=0.01, fout=0.4, hp=25.0)],
              "a submarine hatch's metal clank and creak over a steam pressure release, a deep impact under the clank"),
    }),
    "walker-flame-tell": ("shot", -20.0, -4.0, {
        "a": ([L(48710, 0.12, 0.85, fin=0.04, fout=0.25)], "a huge gas vent opening"),
        "b": ([L(493558, 1.1, 0.72, speed=0.85, fin=0.04, fout=0.25)],
              "a 1000 hp boiler's safety valve opening, slowed to 0.85"),
    }),
    "walker-flame": ("loop", -28.0, -1.0, {
        "a": (dict(sound=395039, start=4.6, length=4.0, xfade=0.3, speed=0.85),
              "a flamethrower's long burst, slowed to 0.85"),
        "b": (dict(sound=396890, start=5.3, length=4.0, xfade=0.3), "a flamethrower's deep roar"),
    }),
    "vent-tell": ("shot", -20.0, -4.0, {
        "a": ([L(243763, 5.0, 1.05, fin=0.03, fout=0.06, swell=(-12.0, 1.6))],
              "a balloon burner's roar swelling in"),
        "b": ([L(778358, 12.74, 1.05, fin=0.03, fout=0.06, swell=(-12.0, 1.6))],
              "a brighter balloon burner's roar swelling in"),
    }),
    "vent-burst": ("shot", -16.0, -4.0, {
        "a": ([L(592572, 0.0, 1.6, fin=0.03, fout=0.7),
               L(243763, 8.5, 1.6, gain=-3.0, fin=0.03, fout=0.6)],
              "a deep flame burst with the end of a gas burner's burn, dying away"),
        "b": ([L(421879, 1.25, 1.6, fin=0.04, fout=0.75)], "a fire burst roaring out"),
    }),
    "tank-tracks": ("loop", -32.0, -1.0, {
        "a": (dict(sound=329800, start=1.5, length=4.0, xfade=0.3), "a T-26 tank driving, tracks and engine"),
        "b": (dict(sound=541240, start=7.4, length=4.0, xfade=0.3), "a tracked bulldozer driving"),
    }),
    "tank-turret": ("shot", -23.0, -4.0, {
        "a": ([L(171657, 0.3, 0.8, fin=0.08, fout=0.15)], "a real artillery turret re-aligning"),
        "b": ([L(340634, 1.25, 0.9, fin=0.08, fout=0.18)], "a factory overhead crane rolling"),
    }),
    "tank-burst": ("shot", -16.0, -4.0, {
        "a": (shots(168707, 0.0, 0.9, [0.0, 0.15, 0.30], [0.0, -0.5, -1.0], 0.35, [1.0, 0.97, 1.02]),
              "a tank cannon firing three shots 0.15 s apart"),
        "b": (shots(423108, 0.0, 1.1, [0.0, 0.15, 0.30], [0.0, -0.5, -1.0], 0.45, [1.0, 0.97, 1.02]),
              "a heavy artillery shot, three shots 0.15 s apart"),
    }),
    "walker-vent-hit": ("shot", -19.0, -4.0, {
        "a": ([L(329515, 0.0, 0.55, fin=0.003, fout=0.25)], "a big hit on a lift door"),
        "b": ([L(508272, 0.13, 0.5, fin=0.003, fout=0.25),
               L(90143, 0.0, 0.4, gain=-9.0, at=0.03, fin=0.02, fout=0.25)],
              "a hollow metal impact with a burst of steam"),
    }),
    "walker-death": ("shot", -13.0, -1.5, {
        "a": ([L(674899, 0.0, 5.6, fin=0.03, fout=2.0),
               L(675462, 0.05, 1.2, gain=-2.0, at=1.85, fin=0.004, fout=0.5),
               L(653294, 0.0, 3.2, gain=-1.0, at=1.9, fin=0.02, fout=1.4, hp=25.0)],
              "a large explosion with debris, the hull landing with a massive impact and a metal crash"),
        "b": ([L(459973, 6.15, 3.8, fin=0.03, fout=1.0),
               L(177556, 7.4, 3.75, gain=-2.0, at=1.85, fin=0.03, fout=1.6),
               L(592388, 0.0, 2.2, gain=-4.0, at=1.85, fin=0.004, fout=0.8),
               L(51097, 0.35, 3.5, gain=-6.0, at=1.95, fin=0.02, fout=1.2)],
              "two chained blasts, the final boom with a car crash's metal and a radiator rumbling"),
    }),
    "walker-wreck-settle": ("shot", -24.0, -4.0, {
        "a": ([L(496836, 3.8, 4.25, speed=0.85, fin=0.3, fout=1.0),
               L(539514, 0.0, 5.0, gain=-14.0, fin=0.5, fout=1.2)],
              "a bunker door's low metallic groan, slowed to 0.85, over an engine cooling"),
        "b": ([L(172411, 1.0, 5.0, fin=0.3, fout=1.2),
               L(539514, 10.0, 5.0, gain=-14.0, fin=0.5, fout=1.2)],
              "a giant metal structure creaking, over an engine cooling"),
    }),
}


def sources_of(spec):
    if isinstance(spec, dict):
        return [spec["sound"]]
    return sorted({layer["sound"] for layer in spec}, key=[layer["sound"] for layer in spec].index)


def build(subject, v):
    kind, band, ceiling, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-r43-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    if kind == "loop":
        st = loop(**spec)
        st = st - st.mean(axis=1, keepdims=True)
        st = st * undb(band - band_rms_db(st))
    else:
        st = level(mix(spec), band, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")


def main(argv):
    for subject, (_, _, _, variants) in SOUNDS.items():
        for v in variants:
            if argv and subject not in argv and f"{subject}-r43-{v}" not in argv:
                continue
            build(subject, v)


if __name__ == "__main__":
    main(sys.argv[1:])
