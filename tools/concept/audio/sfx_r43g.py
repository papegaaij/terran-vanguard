#!/usr/bin/env python3
"""Concept round 43 (M7 part G, Level 20 "Hellas Fallback", step AUD-G): the basin rumble and the pursuing Mote
Swarm's band hum and lunge, cut from real recordings in the Freesound database (the user, round 42: "You should
really stop trying to make these sounds yourself. Use the database."), an a/b pair each. Every layer is a Freesound
original (CC0 or CC-BY, in CREDITS.md; fetched with `freesound_fetch.py --download` into
~/.cache/terran-vanguard/freesound/); this script only cuts, slows (a fixed speed or a gliding one), layers, fades,
filters and levels them: nothing is synthesized. No bubbly or popping sources; every layer starts with a ramped
attack. The same tools as sfx_r43.py (Level 19). Pending the user's review in round 43.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  basin-rumble-r43-a   the Hellas basin (Level 20 from the ridge, Level 21 into the fight), a seamless 16.0 s LOOP:
                       an earthquake's earth rumble (deep, with rock grinding and crackle in it)
  basin-rumble-r43-b   a seamless 12.0 s LOOP: an earthquake's rumble with creaks and groans (low-passed at 2.5 kHz to
                       keep its falling dust from crackling), over a deep distant rumble (heavier and slower)
  swarm-hum-r43-a      the pursuing swarm's band hum, a seamless 6.0 s LOOP: a designed swarm drone (a hive of
                       buzzing insects with tones in it), slowed to 0.85, over a bee box's hive hum
  swarm-hum-r43-b      a seamless 6.0 s LOOP: a large swarm of buzzing flies, slowed to 0.8, over blowflies slowed
                       to 0.9 (a denser, rougher buzz)
  swarm-lunge-r43-a    a lunge's 0.6 s tell and dive: the swarm drone of hum a swelling in and gliding up from
                       0.85 to 1.15, a powerful whoosh rushing in at its end; 0.8 s
  swarm-lunge-r43-b    the large fly swarm of hum b swelling in and gliding up from 0.8 to 1.1, a big swoosh
                       rushing in at its end; 0.8 s

Levels like the accepted round-42 and round-43 sounds: the basin rumble at the loudness of the ambience bed it plays
over (`mars-foundry`, -28 LUFS; nearly all of a rumble's energy is below 200 Hz, so the beds' -36 dB band RMS would put
it 6-9 LU over the bed), the hum as a unit's loop (the tank's tracks, -32 dB), the lunge as a tell (the
loudest 100 ms of the 200 Hz-5 kHz band at -20 dB under a -4 dBFS ceiling). Each layer is peak-normalised before
its gain.

Usage: python3 tools/concept/audio/sfx_r43g.py [subject-or-file ...]   e.g. swarm-hum basin-rumble-r43-b
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
from sfx_r43 import original  # noqa: E402
from synth import SR, decode, lufs, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r43g.py"

CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
SOURCES = {
    217657: ("earth rumble.wav", "Reitanna", CC0),
    263461: ("Earthquake_Rumble_Creaks_Groans_Dust.wav", "Cell31_Sound_Productions", BY4),
    203281: ("EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV", "metrostock99", CC0),
    516517: ("Swarm Drone.wav", "deerlord", CC0),
    817927: ("In the bee box2- swarm of bees_stereo", "Nordliecht", CC0),
    207685: ("Large Swarm of Buzzing Flies.wav", "jakobthiesen", BY3),
    324590: ("Blowflies!", "kangaroovindaloo", CC0),
    434869: ("Powerfull Whooshes 2.wav", "Nic3_one", CC0),
    234547: ("Big Swoosh", "Electroviolence", CC0),
}

_decoded = {}


def source(sound):
    if sound not in _decoded:
        _decoded[sound] = decode(original(sound))
    return _decoded[sound]


def filtered(x, hp, lp):
    x = x - x.mean(axis=1, keepdims=True)
    if hp:
        x = np.array([svf(svf(ch, hp, mode="hp"), hp, mode="hp") for ch in x])
    if lp:
        x = np.array([svf(svf(ch, lp), lp) for ch in x])
    return x


def glide(x, s0, s1, n):
    """n samples of x played at a speed gliding linearly from s0 to s1 (a rising pitch above 1)."""
    pos = np.concatenate([[0.0], np.cumsum(np.linspace(s0, s1, n - 1))])
    return np.array([np.interp(pos, np.arange(x.shape[1]), ch) for ch in x])


def L(sound, start, length, gain=0.0, at=0.0, speed=1.0, fin=0.005, fout=0.05, hp=30.0, lp=None, swell=None):
    """A one-shot layer: `length` s (after the speed change) of the original from `start` s, at `speed` (a number,
    or (from, to) for a glide), filtered, faded (raised cosine), swelled (from_db up to 0 over the layer, power),
    peak-normalised, `gain` dB, placed at `at` s."""
    return dict(sound=sound, start=start, length=length, gain=gain, at=at, speed=speed, fin=fin, fout=fout, hp=hp,
                lp=lp, swell=swell)


def cut(layer):
    x = source(layer["sound"])
    n = int(layer["length"] * SR)
    speed = layer["speed"]
    top = max(speed) if isinstance(speed, tuple) else speed
    a = int(layer["start"] * SR)
    x = x[:, a:a + int(n * top) + SR // 10].copy()
    x = glide(x, *speed, n) if isinstance(speed, tuple) else resample(x, speed)[:, :n]
    x = filtered(x, layer["hp"], layer["lp"])
    n = x.shape[1]
    if layer["swell"]:
        from_db, power = layer["swell"]
        x *= undb(from_db * (1 - np.linspace(0, 1, n)) ** power)
    k = max(1, int(layer["fin"] * SR))
    x[:, :k] *= 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, k))
    k = min(n, int(layer["fout"] * SR))
    x[:, n - k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return x / (np.max(np.abs(x)) + 1e-12) * undb(-1.0 + layer["gain"])


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def Loop(sound, start, gain=0.0, speed=1.0, hp=30.0, lp=None):
    """A loop layer: from `start` s of the original, at `speed`, filtered, peak-normalised, `gain` dB."""
    return dict(sound=sound, start=start, gain=gain, speed=speed, hp=hp, lp=lp)


def loop(layers, length, xfade):
    """A seamless loop of `length` s: each layer's `length` + `xfade` s (after its speed change) with the `xfade`
    s after the end cross-faded into the start (equal power; import_sfx.py's make_loop), then summed."""
    out = np.zeros((2, int(length * SR)))
    for layer in layers:
        x = source(layer["sound"])
        a = int(layer["start"] * SR)
        x = x[:, a:a + int((length + xfade) * layer["speed"] * SR) + SR // 10].copy()
        if layer["speed"] != 1.0:
            x = resample(x, layer["speed"])
        x = make_loop(filtered(x, layer["hp"], layer["lp"]), 0.0, length, xfade)
        out += x / (np.max(np.abs(x)) + 1e-12) * undb(-1.0 + layer["gain"])
    return out


# subject -> (kind, level, ceiling dBFS, {variant: (spec, what)}); a loop's spec is (layers, length, xfade). The level
# is the band RMS (dB) for a "loop", the loudness (LUFS) for a "bed", the loudest 100 ms of the band for a "shot".
SOUNDS = {
    "basin-rumble": ("bed", -28.0, -1.0, {
        "a": (([Loop(217657, 3.0, hp=28.0)], 16.0, 1.0),
              "an earthquake's earth rumble, deep, with rock grinding in it"),
        "b": (([Loop(263461, 14.0, hp=28.0, lp=2500.0), Loop(203281, 4.0, gain=-12.0, hp=28.0)], 12.0, 1.0),
              "an earthquake's rumble with creaks and groans, over a deep distant rumble"),
    }),
    "swarm-hum": ("loop", -32.0, -1.0, {
        "a": (([Loop(516517, 40.0, speed=0.85, hp=60.0), Loop(817927, 2.0, gain=-6.0, hp=60.0)], 6.0, 0.5),
              "a designed swarm drone slowed to 0.85, over a bee box's hive hum"),
        "b": (([Loop(207685, 60.0, speed=0.8, hp=60.0), Loop(324590, 10.0, gain=-5.0, speed=0.9, hp=60.0)],
               6.0, 0.5),
              "a large swarm of buzzing flies slowed to 0.8, over blowflies slowed to 0.9"),
    }),
    "swarm-lunge": ("shot", -20.0, -4.0, {
        "a": ([L(516517, 60.0, 0.8, speed=(0.85, 1.15), hp=60.0, fin=0.03, fout=0.2, swell=(-16.0, 1.4)),
               L(434869, 0.45, 0.45, gain=-3.0, at=0.3, fin=0.02, fout=0.2)],
              "the swarm drone swelling in and gliding up, a powerful whoosh rushing in at its end"),
        "b": ([L(207685, 80.0, 0.8, speed=(0.8, 1.1), hp=60.0, fin=0.03, fout=0.2, swell=(-16.0, 1.4)),
               L(234547, 0.1, 0.5, gain=-3.0, at=0.25, fin=0.02, fout=0.25)],
              "the fly swarm swelling in and gliding up, a big swoosh rushing in at its end"),
    }),
}


def sources_of(spec):
    layers = spec[0] if isinstance(spec, tuple) else spec
    return list(dict.fromkeys(layer["sound"] for layer in layers))


def build(subject, v):
    kind, band, ceiling, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-r43-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    if kind in ("loop", "bed"):
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        st = st * undb(band - (band_rms_db(st) if kind == "loop" else lufs(st)[0]))
    else:
        st = level(mix(spec), band, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind in ("loop", "bed"):
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
