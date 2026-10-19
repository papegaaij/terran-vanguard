#!/usr/bin/env python3
"""Concept round 40 follow-up (step AUD-S): two recorded-and-synthesized proposals for the dome break, after
the user's verdict on `dome-break-r40-a` ("this doesn't sound as a cave in; it needs a rumble"). Recorded
layers are Freesound originals (all CC0, listed in CREDITS.md), cut and mixed here; the sub thump is synthesized.
No bubbly, popping or squelch layer.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo; 2.4 s, loudest 100 ms of the 200 Hz-5 kHz
band at -16 dB and peak at most -4 dBFS like option a):

  dome-break-r40-b.ogg   collapse.wav (metalfortress 611801, 0-2.4 s: a low rumble and a structure coming
                         down, 30 Hz high-pass) + Glass Shatter 2 (Greg_Surr 554564, from 0.15 s, the
                         panes going) + a synthesized 55 -> 38 Hz sub thump under the first 0.8 s
  dome-break-r40-c.ogg   Building Collapse (craigsmith 845932, 0-2.4 s, 2.2 kHz low-pass: heavy rubble crash)
                         + Earth's Crust Depressing (bevibeldesign 366089, 1.0-3.4 s, 180 Hz low-pass,
                         a deep ground rumble under it) + Glass Smash (dwswords 848312, from 0.5 s)

Usage: python3 tools/concept/audio/sfx_r40_dome_break.py [b|c ...]
Needs the cached originals (tools/concept/audio/freesound_fetch.py; ids 611801 554564 845932 366089 848312).
Deterministic.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from sfx_r24 import level  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import edge  # noqa: E402
from synth import SR, decode, exp_decay, pan, sine, svf, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound"
SCRIPT = "tools/concept/audio/sfx_r40_dome_break.py"
LENGTH = 2.4


def original(sound):
    return next(CACHE.glob(f"{sound}_*"))


def layer(out, sound, start, length, at, gain_db, highpass=None, lowpass=None, fadein=0.004, fade=0.6, position=0.0):
    x = decode(original(sound)).mean(axis=0)
    x = x[int(start * SR):int((start + length) * SR)]
    if highpass:
        x = svf(x, np.full(len(x), float(highpass)), mode="hp")
    if lowpass:
        x = svf(x, np.full(len(x), float(lowpass)))
    x = x * np.minimum(1.0, np.arange(len(x)) / (fadein * SR))
    x = x * np.minimum(1.0, np.arange(len(x))[::-1] / (fade * SR))
    x = x / (np.sqrt(np.mean(x[:int(0.5 * SR)] ** 2)) + 1e-12) * 10 ** (gain_db / 20)
    a = int(at * SR)
    m = min(len(x), out.shape[1] - a)
    out[:, a:a + m] += pan(x[:m], position)


def sub_thump(out, at, length):
    n = int(length * SR)
    f = 55.0 * (38.0 / 55.0) ** (np.arange(n) / n)
    x = sine(f, n) * exp_decay(n, 0.3) * np.minimum(1.0, np.arange(n) / (0.01 * SR))
    out[:, int(at * SR):int(at * SR) + n] += pan(x * 1.2, 0.0)


def make_b():
    out = np.zeros((2, int(LENGTH * SR)))
    layer(out, 611801, 0.0, 2.4, 0.0, 0.0, highpass=30, fade=0.9)
    layer(out, 554564, 0.0, 1.8, 0.15, -3.0, highpass=300, fade=0.5, position=0.15)
    sub_thump(out, 0.0, 0.8)
    return out


def make_c():
    out = np.zeros((2, int(LENGTH * SR)))
    layer(out, 845932, 0.0, 2.4, 0.0, 0.0, highpass=30, lowpass=2200, fade=0.9)
    layer(out, 366089, 1.0, 2.4, 0.0, 1.0, highpass=25, lowpass=180, fadein=0.15, fade=0.9)
    layer(out, 848312, 0.0, 1.6, 0.5, -4.0, highpass=300, fade=0.5, position=-0.15)
    return out


VARIANTS = {"b": (make_b, "collapse.wav + glass shatter + sub thump"),
            "c": (make_c, "building collapse + earth rumble + glass smash")}


def main(argv):
    for key, (make, what) in VARIANTS.items():
        if argv and key not in argv:
            continue
        name = f"dome-break-r40-{key}"
        st = level(edge(make(), 0.0005, 0.3), -16.0, -4.0)
        path = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=-4.0,
                         tags={"COMMENT": f"{SCRIPT} {name}: {what}"})
        report(path, what)


if __name__ == "__main__":
    main(sys.argv[1:])
